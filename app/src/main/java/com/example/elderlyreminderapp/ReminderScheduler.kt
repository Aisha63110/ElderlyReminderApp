
package com.example.elderlyreminderapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ReminderScheduler {

    fun scheduleReminder(
        context: Context,
        title: String,
        date: String,
        time: String
    ): Boolean {

        val dateParts = date.split("-")
        val timeParts = time.split(":")

        if (dateParts.size != 3 || timeParts.size != 2) {
            return false
        }

        val calendar = Calendar.getInstance()

        calendar.set(
            dateParts[0].toInt(),
            dateParts[1].toInt() - 1,
            dateParts[2].toInt(),
            timeParts[0].toInt(),
            timeParts[1].toInt(),
            0
        )
        calendar.set(Calendar.MILLISECOND, 0)

        // Do not schedule reminders in the past.
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            return false
        }

        val alarmManager = context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_title", title)
        }

        val requestCode = (title + date + time).hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                // Use an inexact alarm if exact-alarm access is unavailable.
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            return false
        }

        return true
    }

    fun cancelReminder(
        context: Context,
        reminder: Reminder
    ) {
        val intent = Intent(
            context,
            ReminderReceiver::class.java
        ).apply {
            putExtra("reminder_title", reminder.title)
        }

        val requestCode = (
                reminder.title + reminder.date + reminder.time
                ).hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun updateReminder(
        context: Context,
        oldReminder: Reminder,
        updatedReminder: Reminder
    ): Boolean {
        val updated = ReminderStorage.updateReminder(
            context,
            oldReminder,
            updatedReminder
        )

        if (!updated) {
            return false
        }

        cancelReminder(context, oldReminder)
        scheduleReminder(
            context,
            updatedReminder.title,
            updatedReminder.date,
            updatedReminder.time
        )

        return true
    }

}
