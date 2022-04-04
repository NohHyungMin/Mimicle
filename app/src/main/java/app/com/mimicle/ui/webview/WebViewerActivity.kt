package app.com.mimicle.ui.webview

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
//import android.location.Geocoder
//import android.location.Location
//import android.location.LocationListener
//import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.viewModels
import app.com.mimicle.BuildConfig
import app.com.mimicle.R
import app.com.mimicle.common.storage.AppPreference
import app.com.mimicle.data.push.PushInfo
import app.com.mimicle.data.push.PushRepository
import app.com.mimicle.ui.dialog.LoadingDialog
import app.com.mimicle.webinterface.WebAppInterface
import java.net.URISyntaxException
import java.util.*
import androidx.lifecycle.Observer
import app.com.mimicle.base.BaseActivity
import app.com.mimicle.databinding.ActivityWebviewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebViewerActivity : BaseActivity<ActivityWebviewBinding>(R.layout.activity_webview), SensorEventListener {
    private var web_content: WebView? = null
    private var click: Button? = null
    private var light_text: TextView? = null
    private var mLoadingDialog: Dialog? = null
    private val alertIsp: AlertDialog? = null
    private var mCurrentUrl: String? = null

    //조도 센서
    lateinit var sensorManager: SensorManager
    var lightSensor : Sensor? = null

    private var mContext: Context? = null
    lateinit var mWebViewPop : WebView
    lateinit var mContainer : FrameLayout

    private lateinit var pushRepository: PushRepository
    private val viewModel: WebViewerModel by viewModels()
//    {
//        object : ViewModelProvider.Factory {
//            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//                return WebViewerModel(pushRepository) as T
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = baseContext
        binding.vm = viewModel

        initViewModelCallback()

        mLoadingDialog = LoadingDialog(this)
        (mLoadingDialog as LoadingDialog).setCancelable(true)

        init()
        mCurrentUrl = null
        //브릿지 테스트코드
//        click = findViewById<Button>(R.id.click)
//        click!!.setOnClickListener{
//            web_content!!.loadUrl("javascript:test()")
//        }
        //조도 테스트
//        light_text = findViewById<TextView>(R.id.click)
        
        setLightSensor()
    }

    private fun initViewModelCallback() {
        with(viewModel) {
            pushInfo.observe(this@WebViewerActivity, Observer {
                var pushInfo: PushInfo = it
                Log.d("test", pushInfo.memno.toString())
            })
        }
    }

    //조도 센서 초기화
    private fun setLightSensor(){
        sensorManager = mContext!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (lightSensor == null){
//            Toast.makeText(mContext, "No Light Sensor Found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
//        locationManager.removeUpdates(locationListener)
        //if(mLoadingDialog.isShowing())
        mLoadingDialog?.hide()
        mLoadingDialog?.dismiss()
        mLoadingDialog = null
        super.onDestroy()
    }

    private fun init() {
        mContainer = findViewById<FrameLayout>(R.id.webview_frame)
        web_content = findViewById<WebView>(R.id.web_content)

        val webSettings = web_content!!.settings
        web_content!!.addJavascriptInterface(WebAppInterface(this), "Bridge")
        webSettings.javaScriptEnabled = true // JAVA Script Enable
        webSettings.builtInZoomControls = false // Zoom controller On
        webSettings.setSupportZoom(true) // Zoom Support on
        webSettings.useWideViewPort = true //
        webSettings.loadWithOverviewMode = true
        webSettings.pluginState = WebSettings.PluginState.ON // Plug In 허용
        webSettings.setNeedInitialFocus(false)
        webSettings.domStorageEnabled = true // session storeage on
        webSettings.allowFileAccess = true
        webSettings.databaseEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        //나중에 풀어주기
//        webSettings.setGeolocationEnabled(true)
//        webSettings.setGeolocationDatabasePath(filesDir.path)
        webSettings.loadsImagesAutomatically = true

        web_content!!.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        web_content!!.isScrollbarFadingEnabled = true
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            //기기에 따라서 동작할수도있는걸 확인
            webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH)

            //최신 SDK 에서는 Deprecated 이나 아직 성능상에서는 유용하다
            webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN

            //부드러운 전환 또한 아직 동작
            webSettings.setEnableSmoothTransition(true)
        }
        webSettings.setSupportMultipleWindows(true) // Multiple window on
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        {
            }
            webSettings.textZoom = 100
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            web_content!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            web_content!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        web_content!!.isLongClickable = false
        web_content!!.isHapticFeedbackEnabled = false
        web_content!!.setOnLongClickListener { true }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(web_content, true)
        }
        web_content!!.webViewClient = WebClient()
        web_content!!.webChromeClient = BillyWebChromeClent()
        //        String url = "http://www.targetserver.tld/";
// m
//        Map<String, String> extraHeaders = new HashMap<String, String>();
//        extraHeaders.put("Referer", "http://www.referer.tld/login.html");
//        web_content!!.loadUrl("https://www.mimicle.kr/alpha/welcome.html?memberIdx=" + PrefHelper.getPrefValue(PrefHelper.USER_IDX))
//        web_content!!.loadUrl("file:///android_asset/signup/signup.html")
        //web_content!!.loadUrl("https://app.mimicle.kr/interface.html")
        if(AppPreference(baseContext).getLandingUrl() == null || AppPreference(baseContext).getLandingUrl().equals("")) {
            web_content!!.loadUrl(AppPreference(baseContext).getMainUrl()!!)
            Log.d("nhm", AppPreference(baseContext).getMainUrl()?:"");
            //web_content!!.loadUrl("https://app.mimicle.kr/sample/sns_test.html")

        }else{
            web_content!!.loadUrl(AppPreference(baseContext).getLandingUrl()!!)
            AppPreference(baseContext).setLandingUrl("")
        }
//        web_content!!.loadUrl("file:///android_asset/main.html")

        //web_content.loadUrl("http://14.63.167.192/golfshow/test.php");


        //좌표 정보가져오기
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//            locationManager.requestLocationUpdates(
//                LocationManager.GPS_PROVIDER,
//                3000L,
//                1f,
//                locationListener
//        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig)
    }

    internal inner class BillyWebChromeClent : WebChromeClient() {
        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
            return super.onJsAlert(view, url, message, result)
        }

        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
            mWebViewPop = WebView(this@WebViewerActivity).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.setSupportMultipleWindows(true)
            }
            mContainer.addView(mWebViewPop)
            mWebViewPop.webChromeClient = object : WebChromeClient() {
                override fun onCloseWindow(window: WebView?) {
                    mContainer!!.removeView(window)
                    window!!.destroy()
                }
            }
            (resultMsg?.obj as WebView.WebViewTransport).webView = mWebViewPop
            resultMsg.sendToTarget()
            return true
        }
    }

    internal inner class WebClient : WebViewClient() {
//        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//            if (url != null) {
//                if (url.startsWith("https://")) {
////                    Log.e("url", "스키마curent$mCurrentUrl")
////                    Log.e("url", "스키마nownow$url")
////                    if (mCurrentUrl != null && url != null && url == mCurrentUrl) {
////                        Log.e("url", "goback")
////                        web_content!!.goBack()
////                        if (url.equals("http://billyapp.kr/m/shop/orderform.php", ignoreCase = true)) {
////                            web_content!!.goBack()
////                        }
////                    } else {
////                        Log.e("url", "go$url")
//                    if (view != null) {
//                        if (url != null) {
//                            view.loadUrl(url)
//                        }
//                    }
////                        mCurrentUrl = url
////                    }
//                }else {
//                    if (url.startsWith("sms:")) {
//                        //Intent intent = new Intent( Intent.ACTION_SENDTO, Uri.parse(url));
//                        try {
//                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                            startActivity(intent)
//                        } catch (e: Exception) {
//                        }
//                        return true
//                    } else if (url.startsWith("tel:")) {
//                        //Intent intent = new Intent( Intent.ACTION_DIAL, Uri.parse(url));
//                        try {
//                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                            startActivity(intent)
//                        } catch (e: Exception) {
//                        }
//                        return true
//                    } else if (url.startsWith("mailto:")) {
//                        // Intent intent = new Intent( Intent.ACTION_SENDTO, Uri.parse(url));
//                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                        startActivity(intent)
//                        return true
//                    } else if(url.startsWith("kakaolink:")){
//                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                        startActivity(intent)
//                    }
//                }
//            }
//            return true
//        }

        override fun onLoadResource(view: WebView?, url: String?) {
            // TODO Auto-generated method stub
            super.onLoadResource(view, url)
            web_content!!.loadUrl("javascript:test()")
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            //Log.e("url", "스키마reqes" + request.getUrl().toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                var url = request?.url.toString()
                if (request?.url?.scheme == "intent") {
                    try {
                        Log.d("TAG scheme", intent.getPackage().toString())
                        val intent = Intent.parseUri(request.url.toString(), Intent.URI_INTENT_SCHEME)
                        startActivity(intent)
                        // 실행 가능한 앱이 있으면 앱 실행
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                            Log.d("TAG", "ACTIVITY: ${intent.`package`}")
                            return true
                        }

                        // Fallback URL이 있으면 현재 웹뷰에 로딩
                        val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                        if (fallbackUrl != null) {
                            view?.loadUrl(fallbackUrl)
                            Log.d("TAG FALLBACK", "FALLBACK: $fallbackUrl")
                            return true
                        }

                        Log.e("TAG", "Could not parse anythings")

                    } catch (e: URISyntaxException) {
                        Log.e("TAG", "Invalid intent request", e)
                    }
                }
                if (url.startsWith("https://")) {
//                    if (mCurrentUrl != null && url != null && url == mCurrentUrl) {
//                        Log.e("url", "goback")
//                        web_content!!.goBack()
//                        if (url.equals("http://billyapp.kr/m/shop/orderform.php", ignoreCase = true)) {
//                            web_content!!.goBack()
//                        }
//                    } else {
//                        Log.e("url", "go$url")
                        view?.loadUrl(url)
//                        mCurrentUrl = url
//                    }
                } else {
                    if (url.startsWith("sms:")) {
                        //Intent intent = new Intent( Intent.ACTION_SENDTO, Uri.parse(url));
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    } else if (url.startsWith("tel:")) {
                        //Intent intent = new Intent( Intent.ACTION_DIAL, Uri.parse(url));
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    } else if (url.startsWith("mailto:")) {
                        // Intent intent = new Intent( Intent.ACTION_SENDTO, Uri.parse(url));
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    }else if(url.startsWith("kakaolink:")){
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    }else if(url.startsWith("link:")){
                        url = url.removePrefix("link:")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    }
                }
                return true
            }
            return false
            //view.loadUrl(request.getUrl().toString());
            //
            // return false;
        }

        /**
         * 웹페이지 로딩이 시작할 때 처리
         */
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (mLoadingDialog != null) //if(mLoadingDialog.isShowing())
                mLoadingDialog!!.show()
        }

        /**
         * 웹페이지 로딩중 에러가 발생했을때 처리
         */
        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            view!!.loadData("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>" +
                    "</head><body></body></html>", "text/html", "utf-8")
            web_content!!.goBack()
            if (mLoadingDialog != null) //if(mLoadingDialog.isShowing())
                mLoadingDialog!!.hide()
        }

        /**
         * 웹페이지 로딩이 끝났을 때 처리
         */
        override fun onPageFinished(view: WebView?, url: String?) {
            if (mLoadingDialog != null) //if(mLoadingDialog.isShowing())
                mLoadingDialog!!.hide()
        } //        private boolean load(WebView view, String url){
        //            Log.e("url", "스키마" + url);
        //
        //                view.loadUrl(url);
        //                return true;
        //
        //        }
    }

    protected fun createDialog(id: Int): Dialog? { //ShowDialog
        when (id) {
            DIALOG_PROGRESS_WEBVIEW -> {
                val dialog = ProgressDialog(this)
                dialog.setMessage("로딩중입니다. \n잠시만 기다려주세요.")
                dialog.isIndeterminate = true
                dialog.setCancelable(true)
                return dialog
            }
            DIALOG_PROGRESS_MESSAGE -> {
            }
        }
        return alertIsp
    } //end onCreateDialog


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // 백 키를 터치한 경우
        val list = web_content!!.copyBackForwardList()
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 이전 페이지를 볼 수 있다면 이전 페이지를 보여줌
            if (web_content!!.canGoBack()) {
                web_content!!.goBack()
                Log.e("url", "gobackkey")
                //web_content.goBackOrForward(0);
                //web_content.goBackOrForward((list.getCurrentIndex()-2));
                //web_content.clearHistory();
                return false
            } else {
                val builder = androidx.appcompat.app.AlertDialog.Builder(this@WebViewerActivity)
                builder.setTitle("")
                builder.setMessage("종료하시겠습니까?")
                builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                    finish()
                }
                builder.setNegativeButton(android.R.string.no) { dialog, which ->
                }
                builder.show()
                return false
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        private const val DIALOG_PROGRESS_WEBVIEW = 0
        private const val DIALOG_PROGRESS_MESSAGE = 1
        private const val DIALOG_ISP = 2
        private const val DIALOG_CARDAPP = 3
        private const val DIALOG_CARDNM = ""
    }


    //조도 센서
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == lightSensor){
//            Log.e("url", "Light : ${event?.values?.get(0)}")
//            light_text!!.setText("밝기 : ${event?.values?.get(0)}")
        }
    }

    fun setPushInfo() {
        val osType = "aos"
        val versionCode = BuildConfig.VERSION_CODE.toString()
        var memno = ""
        if(AppPreference(baseContext).getMemNo() != null)
            memno = AppPreference(baseContext).getMemNo()!!//"1000001"
        var pushkey = ""
        if(AppPreference(baseContext).getPushToken() != null)
            pushkey = AppPreference(baseContext).getPushToken()!!
        var uuid = ""
        if(AppPreference(baseContext).getAdid() != null)
            uuid = AppPreference(baseContext).getAdid()!!

        var param = HashMap<String, String>()
        param[" osType"] = osType
        param["versionCode"] = versionCode
        param["pushkey"] = pushkey
        param["uuid"] = uuid
        param["memno"] = memno
        viewModel.setPushInfo(param)
    }


    //위치 가져오기
//    private val locationManager by lazy {
//        getSystemService(Context.LOCATION_SERVICE) as LocationManager
//    }
//
//    val locationListener = object : LocationListener {
//        override fun onLocationChanged(location: Location) {
//            location?.let {
//                val position = LatLng(it.latitude, it.longitude)
//                Log.e("lat and long", "${position.latitude} and ${position.longitude}")
//                getAddress(position)
//            }
//        }
//
//        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
//        override fun onProviderEnabled(provider: String) {}
//        override fun onProviderDisabled(provider: String) {}
//    }


//    private fun getAddress(position: LatLng) {
//        val geoCoder = Geocoder(this@WebViewerActivity, Locale.getDefault())
//        val address =
//            geoCoder.getFromLocation(position.latitude, position.longitude, 1).first()
//                .getAddressLine(0)
////        Toast.makeText(mContext, "주소 : ${address}", Toast.LENGTH_SHORT).show()
//        Log.e("Address", address)
//    }
}

