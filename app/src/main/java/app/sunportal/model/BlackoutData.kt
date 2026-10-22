package app.sunportal.model

import com.google.gson.annotations.SerializedName

data class BlackoutResponse(
    val ok: Boolean = false,
    @SerializedName("updated_at") val updatedAt: String? = null,
    val address: String? = null,
    @SerializedName("group_info") val groupInfo: String? = null,
    val schedule: List<BlackoutDay>? = emptyList()
)

data class BlackoutDay(
    val date: String,
    val slots: List<BlackoutSlot>? = emptyList()
)

data class BlackoutSlot(
    val start: String,
    val end: String,
    val status: String? = null
)