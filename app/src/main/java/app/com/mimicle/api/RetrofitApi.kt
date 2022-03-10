package app.com.mimicle.api

import app.com.mimicle.model.AppMetaData
import app.com.mimicle.model.PushInfo
import retrofit2.Call
import retrofit2.http.*

interface RetrofitApi {
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
    fun getMeta(
        @Field("ostype") osType : String,
        @Field("vcode") versionCode : String
    ) : Call<AppMetaData>

    //푸시 정보 전송
    @FormUrlEncoded
    @POST("/com/set-push-info.php")
    fun setPushInfo(
        @Field("ostype") osType : String,
        @Field("vcode") versionCode : String,
        @Field("pushkey") pushKey : String,
        @Field("uuid") uuid : String?,
        @Field("memno") memNo : String
    ) : Call<PushInfo>
}