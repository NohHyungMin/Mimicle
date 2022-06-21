package app.com.mimicle.data.splash

import app.com.mimicle.api.ApiInterface
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppMetaRepositoryImpl @Inject constructor(private val apiInterface: ApiInterface) :
    AppMetaRepository {
    override suspend fun getMeta(
        param: HashMap<String, String>
    ) = apiInterface.getMeta(param["osType"]!!, param["versionCode"]!!)

}