package com.kewei.files

import android.app.Application
import android.util.Log
import coil.Coil
import coil.ImageLoader
import com.kewei.files.coil.CustomContentUriFetcher
import com.kewei.files.util.RuntimeContext
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        RuntimeContext.appContext = this
        initLogger()
        initCoil()
    }

    private fun initLogger() {
        val formatStrategy = PrettyFormatStrategy.newBuilder()
            .tag("FilesApp")
        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy.build()) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
    }

    private fun initCoil() {
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .componentRegistry {
                    add(CustomContentUriFetcher(this@App))
                }
                .logger(object : coil.util.Logger {
                    override var level: Int
                        get() = Log.DEBUG
                        set(value) {}

                    override fun log(
                        tag: String,
                        priority: Int,
                        message: String?,
                        throwable: Throwable?
                    ) {
                        Logger.log(priority, tag, message, throwable)
                    }
                })
                .build()
        )
    }
}