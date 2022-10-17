package app.com.mimicle.data.push

import app.com.mimicle.data.splash.AppMetaData
import com.skydoves.sandwich.ApiResponse

interface PushRepository {
    suspend fun setPushInfo(
        param: HashMap<String, String>
    ): ApiResponse<PushInfo>
}