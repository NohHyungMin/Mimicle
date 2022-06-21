package app.com.mimicle.data.push

import app.com.mimicle.api.ApiInterface
import javax.inject.Inject
import javax.inject.Singleton

class PushRepositoryImpl @Inject constructor(private val apiInterface: ApiInterface) :
    PushRepository {
    override suspend fun setPushInfo(
        param: HashMap<String, String>
    ) = apiInterface.setPushInfo(
        param["osType"]!!, param["versionCode"]!!,
        param["pushkey"]!!, param["uuid"]!!, param["memno"]!!)
}