package app.sunportal.model

import com.google.gson.annotations.SerializedName

data class HistoryResponse(
    val ok: Boolean,
    val period: String,
    val from: String,
    val to: String,
    @SerializedName("step_minutes") val stepMinutes: Int,
    val points: List<HistoryPoint>,
    val summary: HistorySummary
)

data class HistoryPoint(
    val ts: String,
    val pv: Float? = null,
    val load: Float? = null,
    val battery: Float? = null,
    val grid: Float? = null,
    val soc: Float? = null
)

data class HistorySummary(
    @SerializedName("pv_kwh") val pvKwh: Float,
    @SerializedName("load_kwh") val loadKwh: Float,
    @SerializedName("grid_import_kwh") val gridImportKwh: Float,
    @SerializedName("grid_export_kwh") val gridExportKwh: Float,
    @SerializedName("battery_charged_kwh") val batteryChargedKwh: Float,
    @SerializedName("battery_discharged_kwh") val batteryDischargedKwh: Float
)