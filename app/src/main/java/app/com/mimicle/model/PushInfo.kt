package app.com.mimicle.model

data class PushInfo(
    var result: String = "",
    var memno : String = "",
    var pushyn : String = "",
    var data: PushInfoItem
)

data class PushInfoItem(
    var memno : String = "",
    var pushyn : String = "",
)