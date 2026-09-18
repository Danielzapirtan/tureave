package com.tureave

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

class MainActivity : Activity() {
    private val orange = Color.rgb(232, 98, 58)
    private val surface = Color.rgb(244, 241, 234)
    private lateinit var monthTitle: TextView
    private lateinit var userSpinner: Spinner
    private lateinit var grid: LinearLayout
    private lateinit var stats: LinearLayout
    private var shown = YearMonth.now().plusMonths(1)
    private var selectedUser = "fras0"
    private val overrides by lazy { Planner.readOverrides(this) }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        build()
    }

    private fun build() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(28))
            setBackgroundColor(Color.rgb(18, 24, 28))
        }
        root.addView(TextView(this).apply {
            text = "PLANIFICATOR"
            textSize = 12f
            setTextColor(orange)
            letterSpacing = .12f
        })
        root.addView(TextView(this).apply {
            text = "Ture și concedii"
            textSize = 28f
            setTextColor(Color.WHITE)
            setPadding(0, dp(2), 0, dp(18))
        })
        val nav = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        nav.addView(button("‹") { changeMonth(-1) })
        monthTitle = TextView(this).apply {
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        nav.addView(monthTitle, LinearLayout.LayoutParams(0, dp(48), 1f))
        nav.addView(button("›") { changeMonth(1) })
        root.addView(nav)
        val userRow = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, 0)
        }
        userRow.addView(TextView(this).apply {
            text = "Utilizator:"
            textSize = 16f
            setTextColor(Color.WHITE)
        })
        userSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                Planner.users
            )
            selectedUser = getSharedPreferences("planner", MODE_PRIVATE)
                .getString("user", "fras0") ?: "fras0"
            setSelection(Planner.users.indexOf(selectedUser).coerceAtLeast(0))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedUser = Planner.users[position]
                    getSharedPreferences("planner", MODE_PRIVATE).edit()
                        .putString("user", selectedUser).apply()
                    render()
                }
            }
        }
        userRow.addView(userSpinner, LinearLayout.LayoutParams(0, dp(52), 1f))
        root.addView(userRow)
        stats = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(16), 0, dp(14))
        }
        root.addView(stats)
        root.addView(TextView(this).apply {
            text = "Apasă o zi pentru a schimba statusul"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 0, 0, dp(12))
        })
        grid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(grid)
        root.addView(TextView(this).apply {
            text = "Ciclu: portocaliu = tură de zi  •  albastru = tură de noapte  •  gri = liber"
            textSize = 13f
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(16), 0, 0)
        })
        root.addView(TextView(this).apply {
            text = "Status: verde = lucrat  •  mov = concediu  •  crem = liber"
            textSize = 13f
            setTextColor(Color.LTGRAY)
        })
        scroll.addView(root)
        setContentView(scroll)
        render()
    }

    private fun render() {
        monthTitle.text = "${shown.month.getDisplayName(java.time.format.TextStyle.FULL, Locale("ro"))} ${shown.year}"
        stats.removeAllViews()
        val days = Planner.month(shown.year, shown.monthValue, selectedUser, overrides)
        val workDays = days.count { it.status == DayStatus.WORKED }
        val partial = days.count { it.status == DayStatus.PART }
        val leave = days.count { it.status == DayStatus.LEAVE }
        val hours = days.sumOf { it.hours }
        stats.addView(statRow("ORE REALIZATE", "${hours}h", "$workDays zile lucrate  •  $partial parțiale"))
        stats.addView(statRow("CONCEDIU / LIBER", "$leave / ${days.count { it.status == DayStatus.FREE }}", "zile în luna selectată"))
        grid.removeAllViews()
        val header = LinearLayout(this).apply { setBackgroundColor(Color.rgb(231, 226, 214)) }
        listOf("Lun", "Mar", "Mie", "Joi", "Vin", "Sâm", "Dum").forEach {
            header.addView(cellText(it, 15f, Color.DKGRAY, true), weightParams())
        }
        grid.addView(header)
        val firstOffset = (LocalDate.of(shown.year, shown.monthValue, 1).dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
        var row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        repeat(firstOffset) { row.addView(dayCell(null), weightParams()) }
        days.forEachIndexed { index, day ->
            row.addView(dayCell(day), weightParams())
            if ((firstOffset + index + 1) % 7 == 0) {
                grid.addView(row)
                row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            }
        }
        if (row.childCount > 0) {
            while (row.childCount < 7) row.addView(dayCell(null), weightParams())
            grid.addView(row)
        }
    }

    private fun dayCell(info: DayInfo?): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(2), dp(7), dp(2), dp(7))
            minimumHeight = dp(86)
            setBackgroundColor(if (info == null) Color.rgb(237, 234, 224) else backgroundFor(info))
        }
        if (info != null) {
            box.addView(cellText(info.date.dayOfMonth.toString(), 21f, Color.rgb(18, 24, 28), true))
            box.addView(cellText(info.status.label, 11f, Color.DKGRAY, false))
            box.addView(cellText("${info.hours}h", 12f, Color.DKGRAY, true))
            box.setOnClickListener { edit(info) }
            box.contentDescription = "${info.date}, ${info.status.label}, ${info.hours} ore"
        }
        return box
    }

    private fun backgroundFor(info: DayInfo): Int = when {
        info.holiday != null -> Color.rgb(244, 227, 219)
        info.status == DayStatus.LEAVE -> Color.rgb(225, 214, 244)
        info.status == DayStatus.FREE -> Color.rgb(218, 212, 196)
        info.cycle == "Noapte" -> Color.rgb(214, 222, 245)
        else -> Color.rgb(220, 242, 232)
    }

    private fun edit(info: DayInfo) {
        val options = DayStatus.values().map { "${it.label} (${if (it == DayStatus.PART) "ore variabile" else "${it.hours}h"})" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("${info.date.dayOfMonth} ${info.date.month.getDisplayName(java.time.format.TextStyle.FULL, Locale("ro"))}")
            .setSingleChoiceItems(options, info.status.ordinal) { dialog, which ->
                val status = DayStatus.values()[which]
                if (status == DayStatus.PART) {
                    dialog.dismiss()
                    editPartial(info)
                } else {
                    overrides[info.date.toString()] = status to status.hours
                    Planner.saveOverrides(this, overrides)
                    dialog.dismiss()
                    render()
                }
            }
            .setNeutralButton("Revino la ciclul implicit") { _, _ ->
                overrides.remove(info.date.toString())
                Planner.saveOverrides(this, overrides)
                render()
            }
            .setNegativeButton("Anulează", null)
            .show()
    }

    private fun editPartial(info: DayInfo) {
        val input = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(info.hours.toString())
            hint = "Ore (1-11)"
        }
        AlertDialog.Builder(this).setTitle("Ore lucrate parțial").setView(input)
            .setNegativeButton("Anulează", null)
            .setPositiveButton("Salvează") { _, _ ->
                val hours = input.text.toString().toIntOrNull()?.coerceIn(1, 11) ?: 1
                overrides[info.date.toString()] = DayStatus.PART to hours
                Planner.saveOverrides(this, overrides)
                render()
            }.show()
    }

    private fun statRow(label: String, value: String, sub: String): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(14), dp(10), dp(14), dp(10))
            setBackgroundColor(surface)
            addView(cellText(label, 12f, Color.DKGRAY, true), LinearLayout.LayoutParams(0, -2, 1f))
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.END
                addView(cellText(value, 24f, Color.rgb(18, 24, 28), true))
                addView(cellText(sub, 11f, Color.DKGRAY, false))
            })
        }

    private fun cellText(text: String, size: Float, color: Int, bold: Boolean): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

    private fun button(label: String, action: () -> Unit) = TextView(this).apply {
        text = label
        textSize = 30f
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        setBackgroundColor(Color.rgb(27, 35, 41))
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(dp(52), dp(48)).apply { setMargins(0, 0, dp(6), 0) }
    }

    private fun changeMonth(amount: Long) {
        shown = shown.plusMonths(amount)
        render()
    }

    private fun weightParams() = LinearLayout.LayoutParams(0, -2, 1f).apply {
        setMargins(1, 1, 1, 1)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
