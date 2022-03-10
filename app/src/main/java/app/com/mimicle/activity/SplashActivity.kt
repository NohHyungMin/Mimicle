package app.com.mimicle.activity

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import app.com.mimicle.BuildConfig
import app.com.mimicle.MimicleAppApplication.Companion.TAG
import app.com.mimicle.R
import app.com.mimicle.api.RetrofitBuilder
import app.com.mimicle.common.storage.AppPreference
import app.com.mimicle.model.AppMetaData
import app.com.mimicle.model.PushInfo
import app.com.mimicle.util.GoogleUtil
import app.com.mimicle.util.MapUtils.goStore
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import java.lang.Exception


class SplashActivity : AppCompatActivity() {
    private var adid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        checkIntent()
        getAppMeta()
        //checkPermission()
        getAdid()
    }

    private fun checkIntent() {
        val intent = intent
        val callUrl:String?
        if (intent != null) {
            callUrl = intent.getStringExtra("url")
            if(callUrl != null) {
                AppPreference.setLandingUrl(callUrl)
                //Toast.makeText(baseContext, callUrl, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getAdid() {
        GoogleUtil.GAIDTask(baseContext, object : GoogleUtil.GAIDCallback {
            override fun onSucces(result: String?) {
                if (result != null) {
                    adid = result
                    if(adid != null)
                        AppPreference.setAdid(adid!!)
                }
                myToken()
            }

            override fun onFail(e: Exception) {
                myToken()
            }

        }).execute()
    }

    private fun getAppMeta() {
        val osType = "aos"
        val versionCode = BuildConfig.VERSION_CODE.toString()
        RetrofitBuilder.api.getMeta(osType, versionCode).enqueue(object: Callback<AppMetaData> {
            override fun onResponse(call: Call<AppMetaData>, response: Response<AppMetaData>) {
                if(response.isSuccessful) {
                    var appMeta: AppMetaData = response.body()!!
                    AppPreference.setMainUrl(appMeta.data.mainurl.toString())
                    if(appMeta.data.vcode.toInt() > versionCode.toInt()){
                        if(appMeta.data.forcedyn.uppercase() == "Y"){
                            val builder = AlertDialog.Builder(this@SplashActivity)
                            builder.setTitle("")
                            builder.setMessage(appMeta.data.strupdate)
                            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                                goStore(this@SplashActivity);
                                finish()
                            }
                            builder.show()
                        }else{
                            val builder = AlertDialog.Builder(this@SplashActivity)
                            builder.setTitle("")
                            builder.setMessage(appMeta.data.strupdate)
                            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                                goStore(this@SplashActivity);
                                finish()
                            }
                            builder.setNegativeButton(android.R.string.no) { dialog, which ->
                                delayHandler.sendMessageDelayed(Message(), 1000)
                            }
                            builder.show()
                        }
                    }else{
                        delayHandler.sendMessageDelayed(Message(), 1000)
                    }

                    Log.d("test", appMeta.data.mainurl.toString())
                }
            }

            override fun onFailure(call: Call<AppMetaData>, t: Throwable) {
            }
        })
    }

    private fun setPushInfo(token: String) {
        val osType = "aos"
        val versionCode = BuildConfig.VERSION_CODE.toString()
        var memno = ""
        if(AppPreference.getMemNo() != null)
            memno = AppPreference.getMemNo()!!//"1000001"
        var pushkey = ""
        if(token != null)
            pushkey = token!!
        var uuid = ""
        if(adid != null)
            uuid = adid!!

        RetrofitBuilder.api.setPushInfo(osType, versionCode, pushkey, uuid, memno).enqueue(object: Callback<PushInfo> {
            override fun onResponse(call: Call<PushInfo>, response: Response<PushInfo>) {
                if(response.isSuccessful) {
                    var pushInfo: PushInfo = response.body()!!

                    Log.d("test", pushInfo.memno.toString())
                }
            }

            override fun onFailure(call: Call<PushInfo>, t: Throwable) {
            }
        })
    }

    //권한체크
    private fun checkPermission() {
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                getAppMeta()
            }
            override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
                finish()
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setRationaleMessage("앱을 이용하기 위해서는 접근 권한이 필요합니다")
            .setDeniedMessage("앱에서 요구하는 권한설정이 필요합니다...\n [설정] > [권한] 에서 사용으로 활성화해주세요.")
            .setPermissions(
//                    Manifest.permission.READ_PHONE_STATE,
//                    Manifest.permission.READ_CALL_LOG,  // 안드로이드 9.0 에서는 이것도 추가하라고 되어 있음.
//                    Manifest.permission.CALL_PHONE,  // 전화걸기 및 관리
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).check()
    }


    private var delayHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val intent = Intent(this@SplashActivity, WebViewerActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun myToken() {
        //쓰레드 사용할것
        Thread(Runnable {
            try {
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if(!task.isSuccessful){
                        Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                        return@OnCompleteListener
                    }
                    val token = task.result.toString()
                    if(token != null)
                        AppPreference.setPushToken(token)
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
                    FirebaseMessaging.getInstance().subscribeToTopic("all");
                    setPushInfo(token)
                })
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }).start()
    }
}

