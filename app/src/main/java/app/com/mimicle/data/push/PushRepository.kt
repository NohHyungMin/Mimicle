package app.com.mimicle.data.push

interface PushRepository {
    suspend fun setPushInfo(
        param: HashMap<String, String>
    ): PushInfo
}