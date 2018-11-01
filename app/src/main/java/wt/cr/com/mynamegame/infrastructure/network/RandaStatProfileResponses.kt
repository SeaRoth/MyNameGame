package wt.cr.com.mynamegame.infrastructure.network

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
        @SerializedName("status_code") var statusCode: Int = 0,
        @SerializedName("error")       var error: String = "",
        @SerializedName("limit")       var limit: Int = 0,
        @SerializedName("data")        var data: List<WTProfile>
)

data class WTProfile(
        @SerializedName("id")             var id: String = "",
        @SerializedName("firstName")      var firstName: String = "",
        @SerializedName("lastName")       var lastName: String = "",
        @SerializedName("headshot")       var headshot: WTHeadshot,
        @SerializedName("socialLinks")    var socialLinks: List<WTSocialLink>
)

data class WTHeadshot(
        @SerializedName("url")      var url: String = ""
)

data class WTSocialLink(
        @SerializedName("type")         var type: String = "",
        @SerializedName("callToAction") var callToAction: String = "",
        @SerializedName("url")          var url: String = ""
)