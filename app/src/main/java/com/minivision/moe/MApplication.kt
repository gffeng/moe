package com.minivision.moe

import android.app.Application
import android.content.Context
import com.minivision.parameter.util.CrashUtil
import com.minivision.parameter.util.IAppManager
import com.minivision.parameter.util.LogBuilder
import com.minivision.parameter.util.LogUtil
import kotlin.properties.Delegates

/**
 *
 * @author gf
 * @date 2021/4/27
 */
class MApplication : Application(), IAppManager {
    companion object {
        const val TAG = "Application"
        private var context: Context by Delegates.notNull<Context>()

        fun instance(): Context {
            return context
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        initLog()
        LogUtil.i(TAG, "onCreate")
    }

    private fun initLog() {
        val builder = LogBuilder().withContext(context)
            .setTag("PFK")
            .setOnLine(false)
        LogUtil.setLogBuilder(builder)
        CrashUtil.getInstance().init(context)
        CrashUtil.getInstance().setAppManager(this)
    }

    override fun restartApp() {
        TODO("Not yet implemented")
    }

    override fun exitApp() {
        TODO("Not yet implemented")
    }
}