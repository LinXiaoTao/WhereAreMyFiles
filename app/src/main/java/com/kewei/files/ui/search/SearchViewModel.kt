package com.kewei.files.ui.search

import android.app.Application
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class SearchViewModel(private val app: Application) : AndroidViewModel(app) {

    private val defaultProjection = arrayOf(
        "_id",
        "date_modified",
        "_display_name",
        "mime_type"
    )
    private val _historyLiveData = MutableLiveData<List<HistoryModel>>()
    val historyLiveData = _historyLiveData

    fun queryHistory() {
        // 查询 24 小时内新增/修改图片、视频、文件
        viewModelScope.launch {
            val imagesModelList = internalQueryHistory(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf("width", "height")
            ) { cursor ->
                val widthCol = cursor.getColumnIndexOrThrow("width")
                val heightCol = cursor.getColumnIndexOrThrow("height")
                return@internalQueryHistory HistoryImageModel(
                    width = cursor.getIntOrNull(widthCol) ?: -1,
                    height = cursor.getIntOrNull(heightCol) ?: -1
                )
            }
            val videoModelList = internalQueryHistory(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf("width", "height")
            ) { cursor ->
                val widthCol = cursor.getColumnIndexOrThrow("width")
                val heightCol = cursor.getColumnIndexOrThrow("height")
                return@internalQueryHistory HistoryVideoModel(
                    width = cursor.getIntOrNull(widthCol) ?: -1,
                    height = cursor.getIntOrNull(heightCol) ?: -1
                )
            }
            val historyModelList = imagesModelList + videoModelList
            Logger.d("Query history model list: $historyModelList")
            _historyLiveData.value =
                historyModelList.sortedWith { o1, o2 -> (o2.dateModified - o1.dateModified).toInt() }
        }
    }


    private suspend fun internalQueryHistory(
        uri: Uri,
        projection: Array<String>,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        callback: ((Cursor) -> Any)? = null,
    ): List<HistoryModel> {
        val selection = "date_modified >= ?"
        // 查询 24 小时内的更新
        val now = Calendar.getInstance()
        now.add(Calendar.DATE, -1)
        Logger.d(
            "Query history time: ${
                SimpleDateFormat(
                    "yyyyMMdd HH:mm:ss",
                    Locale.getDefault()
                ).format(now.time)
            }"
        )
        val selectionArgs = arrayOf((now.timeInMillis / 1000).toString())
        return withContext(dispatcher) {
            val query = app.contentResolver.query(
                uri,
                defaultProjection + projection,
                selection,
                selectionArgs,
                ""
            )
            val result = mutableListOf<HistoryModel>()
            query?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow("_id")
                val dataModifiedCol = cursor.getColumnIndexOrThrow("date_modified")
                val displayNameCol = cursor.getColumnIndexOrThrow("_display_name")
                val mimeTypeCol = cursor.getColumnIndexOrThrow("mime_type")
                while (cursor.moveToNext()) {
                    val id = cursor.getLongOrNull(idCol)
                    val dataModified = cursor.getLongOrNull(dataModifiedCol)
                    val displayName = cursor.getStringOrNull(displayNameCol)
                    val mimeType = cursor.getStringOrNull(mimeTypeCol)
                    if (id == null) {
                        Logger.w("queryImagesHistory id is null")
                        continue
                    }
                    val model = HistoryModel(
                        id = id,
                        displayName = displayName ?: "",
                        dateModified = dataModified ?: 0L,
                        mineType = mimeType ?: "",
                        extension = callback?.invoke(cursor),
                        contentUri = ContentUris.withAppendedId(uri, id)
                    )
                    result.add(model)
                }
            }
            return@withContext result
        }
    }

}