package app.com.mimicle

import android.app.Application
import app.com.mimicle.api.ApiClient
import app.com.mimicle.api.ApiInterface

/**
 * 어플리케이션 메인컨텍스트
 * Created by nhm on 2021-09-08.
 */
class MimicleAppApplication : Application() {

    companion object {
        val apiInterface: ApiInterface = ApiClient.getApiClient().create(ApiInterface::class.java)
        val TAG = MimicleAppApplication::class.java.name
        lateinit var instance: MimicleAppApplication
            private set
    }


    override fun onCreate() {
        super.onCreate()
        instance = this
//        inject()
    }

    val context
        get() = applicationContext

//    private fun inject() {
//        apiInterface = ApiClient.getApiClient().create(ApiInterface::class.java)
//    }
}