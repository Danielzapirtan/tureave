package com.tureave

import android.content.Context
import org.json.JSONObject
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class DayStatus(val label: String, val hours: Int) {
    WORKED("Lucrat", 12), PART("Lucrat parțial", 6), LEAVE("Concediu", 8), FREE("Liber", 0)
}

data class DayInfo(val date: LocalDate, val cycle: String, val status: DayStatus, val hours: Int, val holiday: String? = null)

object Planner {
    val users = listOf("ljc1q", "xxtoo", "fras0", "l3hb4")
    private val holidays = mapOf(
        "01-01" to "Anul Nou", "01-02" to "A doua zi de Anul Nou",
        "01-24" to "Unirea Principatelor", "05-01" to "Ziua Muncii",
        "06-01" to "Ziua Copilului", "08-15" to "Adormirea Maicii Domnului",
        "11-30" to "Sfântul Andrei", "12-01" to "Ziua Națională",
        "12-25" to "Crăciunul", "12-26" to "A doua zi de Crăciun"
    )
    private val prefs = listOf("worked", "part", "leave", "free")

    fun cycle(date: LocalDate, user: String): String {
        val userIndex = users.indexOf(user).takeIf { it >= 0 } ?: users.indexOf("fras0")
        val ref = LocalDate.of(2026, 8, 10).plusDays((userIndex + 1).toLong())
        return when (Math.floorMod(ChronoUnit.DAYS.between(ref, date).toInt(), 4)) {
            0 -> "Zi"
            1 -> "Noapte"
            else -> "Liber"
        }

    }

    fun holiday(date: LocalDate): String? = holidays["%02d-%02d".format(date.monthValue, date.dayOfMonth)]

    fun info(date: LocalDate, user: String, overrides: Map<String, Pair<DayStatus, Int>>): DayInfo {
        val key = date.toString()
        val cycle = cycle(date, user)
        val override = overrides[key]
        val status = override?.first ?: if (cycle == "Liber") DayStatus.FREE else DayStatus.WORKED
        return DayInfo(date, cycle, status, override?.second ?: status.hours, holiday(date))
    }

    fun readOverrides(context: Context): MutableMap<String, Pair<DayStatus, Int>> {
        val result = mutableMapOf<String, Pair<DayStatus, Int>>()
        val json = context.getSharedPreferences("planner", Context.MODE_PRIVATE).getString("overrides", "{}") ?: "{}"
        val obj = JSONObject(json)
        obj.keys().forEach { key ->
            val item = obj.getJSONObject(key)
            result[key] = DayStatus.valueOf(item.getString("status")) to item.optInt("hours", 0)
        }
        return result
    }

    fun saveOverrides(context: Context, values: Map<String, Pair<DayStatus, Int>>) {
        val obj = JSONObject()
        values.forEach { (key, value) ->
            obj.put(key, JSONObject().put("status", value.first.name).put("hours", value.second))
        }
        context.getSharedPreferences("planner", Context.MODE_PRIVATE).edit().putString("overrides", obj.toString()).apply()
    }

    fun month(year: Int, month: Int, user: String, overrides: Map<String, Pair<DayStatus, Int>>): List<DayInfo> {
        val first = LocalDate.of(year, month, 1)
        return (1..first.lengthOfMonth()).map { info(first.withDayOfMonth(it), user, overrides) }
    }
}
