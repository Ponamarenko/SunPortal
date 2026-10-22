package app.sunportal.model

import androidx.annotation.DrawableRes
import app.sunportal.R

/**
 * Тип домовладіння — изометрические иконки Svitgrid (assets/art/households/, PNG
 * из их APK, продублированы в res/drawable-nodpi/svit_household_*.png).
 *
 * Выбирается в настройках и показывается в узле «Дім» на схеме потоков.
 */
data class HouseholdType(
    val id: String,
    @DrawableRes val iconRes: Int,
    val title: String
)

/** Все доступные типы домовладения (в порядке отображения в настройках). */
val HOUSEHOLD_TYPES: List<HouseholdType> = listOf(
    HouseholdType("apartment", R.drawable.svit_household_apartment, "Квартира"),
    HouseholdType("cabin", R.drawable.svit_household_cabin, "Будинок"),
    HouseholdType("cottage", R.drawable.svit_household_cottage, "Дача"),
    HouseholdType("farm", R.drawable.svit_household_farm, "Ферма"),
    HouseholdType("greenhouse", R.drawable.svit_household_greenhouse, "Теплиця"),
    HouseholdType("shop", R.drawable.svit_household_shop, "Магазин"),
    HouseholdType("solar_array", R.drawable.svit_household_solar_array, "Сонячна станція"),
    HouseholdType("workshop", R.drawable.svit_household_workshop, "Майстерня")
)

/** Тип домовладения по умолчанию. */
const val DEFAULT_HOUSEHOLD_ID = "cottage"

/** Найти тип домовладения по id (с запасным вариантом). */
fun householdById(id: String?): HouseholdType =
    HOUSEHOLD_TYPES.firstOrNull { it.id == id }
        ?: HOUSEHOLD_TYPES.first { it.id == DEFAULT_HOUSEHOLD_ID }
