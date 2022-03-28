package app.com.mimicle.data.splash

import app.com.mimicle.api.ApiInterface

class AppMetaRepositoryImpl(private val apiInterface: ApiInterface) : AppMetaRepository {
    override suspend fun getMeta(
        param: HashMap<String, String>
    ) = apiInterface.getMeta(param["osType"] ?:"", param["versionCode"] ?:"")

}