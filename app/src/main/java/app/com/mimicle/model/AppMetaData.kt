package app.com.mimicle.model

data class AppMetaData(
    var result: String = "",
    var data: AppMetaItem
)

data class AppMetaItem(
    var vname : String = "",
    var vcode : String = "",
    var forcedyn: String = "",
    var strupdate: String = "",
    var mainurl: String = ""
)