package com.kewei.files.coil

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import androidx.annotation.VisibleForTesting
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.size.Size
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class CustomContentUriFetcher(private val context: Context) : Fetcher<Uri> {
    override fun handles(data: Uri) = data.scheme == ContentResolver.SCHEME_CONTENT

    override fun key(data: Uri) = data.toString()

    override suspend fun fetch(
        pool: BitmapPool,
        data: Uri,
        size: Size,
        options: Options
    ): FetchResult {
        when (val mineType = context.contentResolver.getType(data)) {
            "video/mp4" -> {
                val bitmap = MediaStore.Video.Thumbnails.getThumbnail(
                    context.contentResolver,
                    ContentUris.parseId(data),
                    MediaStore.Video.Thumbnails.MINI_KIND,
                    BitmapFactory.Options().apply {
                    }
                )
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                val inputStream = ByteArrayInputStream(outputStream.toByteArray())
                return SourceResult(
                    source = inputStream.source().buffer(),
                    mimeType = mineType,
                    dataSource = DataSource.DISK
                )
            }
            else -> {
                val inputStream = if (isContactPhotoUri(data)) {
                    // Modified from ContactsContract.Contacts.openContactPhotoInputStream.
                    val stream: InputStream? =
                        context.contentResolver.openAssetFileDescriptor(data, "r")
                            ?.createInputStream()
                    checkNotNull(stream) { "Unable to find a contact photo associated with '$data'." }
                } else {
                    val stream: InputStream? = context.contentResolver.openInputStream(data)
                    checkNotNull(stream) { "Unable to open '$data'." }
                }

                return SourceResult(
                    source = inputStream.source().buffer(),
                    mimeType = mineType,
                    dataSource = DataSource.DISK
                )
            }
        }
    }

    /** Contact photos are a special case of content uris that must be loaded using [ContentResolver.openAssetFileDescriptor]. */
    @VisibleForTesting
    internal fun isContactPhotoUri(data: Uri): Boolean {
        return data.authority == ContactsContract.AUTHORITY && data.lastPathSegment == ContactsContract.Contacts.Photo.DISPLAY_PHOTO
    }
}