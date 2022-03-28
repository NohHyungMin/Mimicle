package app.com.mimicle.data.splash


interface AppMetaRepository {
    suspend fun getMeta(
        param: HashMap<String, String>
    ):AppMetaData
}