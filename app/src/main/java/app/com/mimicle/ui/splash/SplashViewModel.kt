package app.com.mimicle.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.com.mimicle.data.push.PushInfo
import app.com.mimicle.data.push.PushRepository
import app.com.mimicle.data.splash.AppMetaData
import app.com.mimicle.data.splash.AppMetaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashViewModel(private val pushRepository: PushRepository,
                      private val appMetaRepository: AppMetaRepository) : ViewModel(){

    private val _pushInfo = MutableLiveData<PushInfo>()
    val pushInfo: LiveData<PushInfo>
        get() = _pushInfo

    private val _appMetaData = MutableLiveData<AppMetaData>()
    val appMetaData: LiveData<AppMetaData>
        get() = _appMetaData

    fun setPushInfo(param: HashMap<String, String>) = viewModelScope.launch(Dispatchers.IO){
        val responseData = pushRepository.setPushInfo(param)

        withContext(Dispatchers.Main) {
            _pushInfo.value = responseData
        }
    }

    fun getAppMeta(param: HashMap<String, String>) = viewModelScope.launch(Dispatchers.IO){
        val responseData = appMetaRepository.getMeta(param)

        withContext(Dispatchers.Main) {
            _appMetaData.value = responseData
        }
    }
}