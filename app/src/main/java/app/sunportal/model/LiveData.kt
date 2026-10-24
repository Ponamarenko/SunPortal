package app.sunportal.model

data class LiveData(
    val timestamp: Long,
    val p_sun: Float,
    val p_load: Float,
    val p_batt: Float,
    val batt_soc: Float,
    val p_grid: Float,
    val p_string1: Float,
    val p_string2: Float,
    val p_string3: Float,
    val battery_voltage: Float,
    val battery_current: Float,
    val battery_temp: Float,
    val battery_remaining_kwh: Float,
    val pv_today: Float,
    val pv_total: Float,
    val pv_peak_today: Float? = 0f,
    val p_gen: Float? = 0f,                // генератор (Вт)
    val p_smart_load: Float? = 0f,         // смарт-нагрузка (Вт)
    val grid_voltage_l1: Float? = null,    // напряжение сети, V (если API отдаёт)
    val battery_charge_total: Float? = null,  // Total energy charged, kW hour
    val battery_discharge_total: Float? = null,  // Total energy discharged, kW hour
    val pv1_voltage: Float = 0f,       // PV1 voltage, V
    val pv1_current: Float = 0f,       // PV1 current, A
    val pv2_voltage: Float = 0f,       // PV2 voltage, V
    val pv2_current: Float = 0f,       // PV2 current, A
    val pv3_voltage: Float = 0f,       // PV3 voltage, V
    val pv3_current: Float = 0f,       // PV3 current, A
    val pv1_power: Float = 0f,         // PV1 power, W
    val pv2_power: Float = 0f,         // PV2 power, W
    val pv3_power: Float = 0f          // PV3 power, W
)
