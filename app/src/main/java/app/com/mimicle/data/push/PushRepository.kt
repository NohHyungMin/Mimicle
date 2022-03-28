package app.com.mimicle.data.push

import retrofit2.Response

interface PushRepository {
    suspend fun setPushInfo(
        param: HashMap<String, String>
    ): PushInfo
}