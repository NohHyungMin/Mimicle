package app.com.mimicle.data.push

import app.com.mimicle.api.ApiInterface

class PushRepositoryImpl(private val apiInterface: ApiInterface) : PushRepository {
    override suspend fun setPushInfo(
        param: HashMap<String, String>
    ) = apiInterface.setPushInfo(
        param["osType"] ?:"", param["versionCode"] ?:"",
        param["pushKey"] ?:"", param["uuid"], param["memNo"] ?:"")
}