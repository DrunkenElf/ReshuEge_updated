package com.reshuege.utils

import android.annotation.TargetApi
import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView

import java.util.Calendar

import com.reshuege.R

object DatePickerUtils {


    /**
     * диалог в виде календаря
     * подходит для ближайших дат
     */
    @JvmOverloads
    fun showCommonCalendarDialog(context: Context, onDateSetListener: DatePickerDialog.OnDateSetListener, date: Calendar?, title: String? = null, minDate: Calendar? = null, maxDate: Calendar? = null) {
        var date = date
        if (date == null) {
            date = Calendar.getInstance()
        }
        if (Build.VERSION.SDK_INT <= 20) {
            DatePickerUtils.showOldDatePickerCalendarDialog(context, onDateSetListener, date!!.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), title, minDate, maxDate)
        } else {
            DatePickerUtils.showDatePickerDialog(context, onDateSetListener, date!!.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), "", minDate, maxDate)
        }
    }

    /**
     * диалог выбора даты с тремя полями
     * подходит для даты рождения
     */
    fun showCommonDateDialog(context: Context, listener: DatePickerDialog.OnDateSetListener, date: Calendar?) {
        var date = date
        if (date == null) {
            date = Calendar.getInstance()
        }
        if (Build.VERSION.SDK_INT <= 20) {
            DatePickerUtils.showOldDatePickerDialog(context, listener, date!!.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH))
        } else {
            DatePickerUtils.showDatePickerDialog(context, listener, date!!.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), "", null, null)
        }
    }

    private fun showDatePickerDialog(context: Context, onDateSetListener: DatePickerDialog.OnDateSetListener, year: Int, month: Int, day: Int, title: String, minDate: Calendar?, maxDate: Calendar?) {
        val dialog = DatePickerDialog(context, onDateSetListener, year, month, day)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        if (minDate != null) {
            dialog.datePicker.minDate = minDate.timeInMillis
        }
        if (maxDate != null) {
            dialog.datePicker.maxDate = maxDate.timeInMillis
        }
        if (AppTextUtils.isEmpty(title!!)) {
            dialog.setTitle(title)
        }
        dialog.show()
    }

    /**
     * диалог в виде календаря
     * подходит для ближайших дат
     */
    private fun showOldDatePickerCalendarDialog(context: Context, onDateSetListener: DatePickerDialog.OnDateSetListener, year: Int, month: Int, day: Int, title: String?, minDate: Calendar?, maxDate: Calendar?) {
        val COLOR_ACCENT = context.resources.getColor(R.color.colorAccent)
        val dialog = DatePickerDialog(context, onDateSetListener, year, month, day)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.datePicker.calendarViewShown = true
        dialog.datePicker.spinnersShown = false
        setCalendarColors(dialog)
        if (minDate != null && maxDate != null) {
            dialog.datePicker.minDate = minDate.timeInMillis
            dialog.datePicker.maxDate = maxDate.timeInMillis
        }
        dialog.datePicker.calendarView.showWeekNumber = false
        dialog.show()

        val dividerId = dialog.context.resources.getIdentifier("android:id/titleDivider", null, null)
        val divider = dialog.findViewById<View>(dividerId)
        divider.setBackgroundColor(COLOR_ACCENT)
        val textViewId = dialog.context.resources.getIdentifier("android:id/alertTitle", null, null)
        val tv = dialog.findViewById<View>(textViewId) as TextView
        tv.setTextColor(COLOR_ACCENT)
        if (AppTextUtils.isEmpty(title!!)) {
            tv.setText(R.string.date_choiсe)
        } else {
            tv.text = title
        }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(COLOR_ACCENT)
    }

    /**
     * диалог выбора даты с тремя полями
     * подходит для даты рождения
     */
    private fun showOldDatePickerDialog(context: Context, onDateSetListener: DatePickerDialog.OnDateSetListener, year: Int, month: Int, day: Int) {
        val COLOR_ACCENT = context.resources.getColor(R.color.colorAccent)
        val dialog = DatePickerDialog(context, onDateSetListener, year, month, day)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()

        val dividerId = dialog.context.resources.getIdentifier("android:id/titleDivider", null, null)
        val divider = dialog.findViewById<View>(dividerId)
        divider.setBackgroundColor(COLOR_ACCENT)
        val textViewId = dialog.context.resources.getIdentifier("android:id/alertTitle", null, null)
        val tv = dialog.findViewById<View>(textViewId) as TextView
        tv.setTextColor(COLOR_ACCENT)
        tv.setText(R.string.date_choiсe)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(COLOR_ACCENT)
        setSpinnerColors(dialog)
    }

    @TargetApi(16)
    private fun setCalendarColors(dialog: DatePickerDialog) {
        dialog.datePicker.calendarView.weekSeparatorLineColor = dialog.context.resources.getColor(android.R.color.transparent)
        dialog.datePicker.calendarView.selectedWeekBackgroundColor = dialog.context.resources.getColor(android.R.color.transparent)
        dialog.datePicker.calendarView.setSelectedDateVerticalBar(R.color.colorAccent)
    }

    private fun setSpinnerColors(dialog: DatePickerDialog) {
        val datePicker = dialog.datePicker
        val system = Resources.getSystem()
        val dayId = system.getIdentifier("day", "id", "android")
        val monthId = system.getIdentifier("month", "id", "android")
        val yearId = system.getIdentifier("year", "id", "android")

        val dayPicker = datePicker.findViewById<View>(dayId) as NumberPicker
        val monthPicker = datePicker.findViewById<View>(monthId) as NumberPicker
        val yearPicker = datePicker.findViewById<View>(yearId) as NumberPicker

        setDividerColor(dayPicker)
        setDividerColor(monthPicker)
        setDividerColor(yearPicker)
    }

    private fun setDividerColor(picker: NumberPicker?) {
        if (picker == null)
            return

        val count = picker.childCount
        for (i in 0 until count) {
            try {
                val dividerField = picker.javaClass.getDeclaredField("mSelectionDivider")
                dividerField.setAccessible(true)
                val colorDrawable = ColorDrawable(picker.resources.getColor(R.color.colorAccent))
                dividerField.set(picker, colorDrawable)
                picker.invalidate()
            } catch (e: Exception) {
            }

        }
    }
}
/**
 * диалог в виде календаря
 * подходит для ближайших дат
 */
/**
 * диалог в виде календаря
 * подходит для ближайших дат
 */
