package app.sunportal.model

data class TouData(
    val ok: Boolean,
    val days_mask: Int,
    val days: List<String>,
    val enabled: Boolean,
    val slots: List<TouSlot>,
    val updated: String
)

data class TouSlot(
    val index: Int,
    val time: String,
    val time_raw: Int,
    val power: Int,
    val voltage: Int,
    val soc: Int,
    val grid: Boolean,
    val gen: Boolean
)
