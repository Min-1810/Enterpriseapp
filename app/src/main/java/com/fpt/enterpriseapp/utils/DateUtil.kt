package com.fpt.enterpriseapp.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
    private val dateFmt     = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateTimeFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val timeFmt     = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val todayFmt    = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFmt    = SimpleDateFormat("MM/yyyy", Locale.getDefault())

    fun formatDate(millis: Long)     = dateFmt.format(Date(millis))
    fun formatDateTime(millis: Long) = dateTimeFmt.format(Date(millis))
    fun formatTime(millis: Long)     = timeFmt.format(Date(millis))
    fun todayKey()                    = todayFmt.format(Date())
    fun formatMonth(millis: Long)    = monthFmt.format(Date(millis))

    fun startOfMonth(year: Int, month: Int): Long = Calendar.getInstance().apply {
        set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun endOfMonth(year: Int, month: Int): Long = Calendar.getInstance().apply {
        set(year, month, 1, 23, 59, 59); set(Calendar.MILLISECOND, 999)
        add(Calendar.MONTH, 1); add(Calendar.DAY_OF_MONTH, -1)
    }.timeInMillis

    fun formatCurrency(value: Double) = "%,.0f đ".format(value)
}