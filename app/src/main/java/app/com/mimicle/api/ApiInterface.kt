package app.com.mimicle.api

import app.com.mimicle.data.push.PushInfo
import app.com.mimicle.data.splash.AppMetaData
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {
//    @FormUrlEncoded
//    @POST("/api/users")
//    fun requestList(
//        @FieldMap param: HashMap<String, String>
//    ) : Call<ArrayList<AppMetaData>>

//    @GET("/api/version")
//    fun getVersion() : Call<AppMetaData>

    //앱 정보 받기
    @FormUrlEncoded
    @POST("/com/appmeta.php")
    suspend fun getMeta(
        @Field("ostype") osType : String,
        @Field("vcode") versionCode : String
    ) : AppMetaData

    //푸시 정보 전송
    @FormUrlEncoded
    @POST("/com/set-push-info.php")
    suspend fun setPushInfo(
        @Field("ostype") osType : String,
        @Field("vcode") versionCode : String,
        @Field("pushkey") pushKey : String,
        @Field("uuid") uuid : String?,
        @Field("memno") memNo : String
    ) : PushInfo
}