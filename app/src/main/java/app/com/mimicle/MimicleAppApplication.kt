package app.com.mimicle

import android.app.Application


/**
 * 어플리케이션 메인컨텍스트
 * Created by tjdud on 2016-12-26.
 */
class MimicleAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        val TAG = MimicleAppApplication::class.java.name
        lateinit var instance: MimicleAppApplication
            private set
    }

    val context
        get() = applicationContext
}