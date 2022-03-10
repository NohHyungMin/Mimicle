package app.com.mimicle.webinterface

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import app.com.mimicle.BuildConfig
import app.com.mimicle.api.RetrofitBuilder
import app.com.mimicle.common.storage.AppPreference
import app.com.mimicle.model.PushInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WebAppInterface(private val mContext: Activity) {
    /** Show a toast from the web page  */
    @JavascriptInterface
    fun showToast(toast: String) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
//        web_content!!.post {
//            if (web_content!!.canGoBack() && web_content!!.url != "https://booking.pregolfshow.com/golfclubs") {
//                web_content!!.goBack()
//            } else {
//            }
//        }
    }

    //알림 설정
    @JavascriptInterface
    fun getNoti() {
        AppPreference.getNotiType()
    }

    @JavascriptInterface
    fun saveNoti(idxNoti: String) {
        AppPreference.setNotiType(idxNoti)
    }

    //회원번호
    @JavascriptInterface
    fun getMemno() {
        AppPreference.getMemNo()
    }

    @JavascriptInterface
    fun setMemno(memNo: String) {
        AppPreference.setMemNo(memNo)
        setPushInfo()
    }

    @JavascriptInterface
    fun goExitPop() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(mContext)
        builder.setTitle("")
        builder.setMessage("종료하시겠습니까?")
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            mContext.finish()
        }
        builder.setNegativeButton(android.R.string.no) { dialog, which ->
        }
        builder.show()
    }

    @JavascriptInterface
    fun exit() {
        mContext.finish()
    }

    @JavascriptInterface
    fun callSnsSheet(content: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        mContext.startActivity(shareIntent)

    }

    private fun setPushInfo() {
        val osType = "aos"
        val versionCode = BuildConfig.VERSION_CODE.toString()
        var memno = ""
        if(AppPreference.getMemNo() != null)
            memno = AppPreference.getMemNo()!!//"1000001"
        var pushkey = ""
        if(AppPreference.getPushToken() != null)
            pushkey = AppPreference.getPushToken()!!
        var uuid = ""
        if(AppPreference.getAdid() != null)
            uuid = AppPreference.getAdid()!!

        RetrofitBuilder.api.setPushInfo(osType, versionCode, pushkey, uuid, memno).enqueue(object:
            Callback<PushInfo> {
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

}