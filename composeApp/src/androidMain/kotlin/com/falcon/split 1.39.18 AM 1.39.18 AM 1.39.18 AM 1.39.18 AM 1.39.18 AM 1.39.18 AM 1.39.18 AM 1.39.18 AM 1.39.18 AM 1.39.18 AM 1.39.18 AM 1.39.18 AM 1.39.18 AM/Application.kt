package com.falcon.split

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.falcon.split.di.initKoin

open class Application: Application(),  Application.ActivityLifecycleCallbacks {
    override fun onCreate() {
        super.onCreate()
        initKoin()
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
//        TODO("Not yet implemented")
    }

    override fun onActivityStarted(p0: Activity) {
//        TODO("Not yet implemented")
    }

    override fun onActivityResumed(p0: Activity) {
//        TODO("Not yet implemented")
    }

    override fun onActivityPaused(p0: Activity) {
//        TODO("Not yet implemented")
    }

    override fun onActivityStopped(p0: Activity) {
//        TODO("Not yet implemented")
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
//        TODO("Not yet implemented")
    }

    override fun onActivityDestroyed(p0: Activity) {
//        TODO("Not yet implemented")
    }
}