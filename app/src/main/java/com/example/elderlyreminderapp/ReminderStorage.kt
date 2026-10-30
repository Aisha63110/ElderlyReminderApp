
package com.example.elderlyreminderapp

import android.content.Context

data class Reminder(
    val title: String,
    val date: String,
    val time: String
)

object ReminderStorage {

    private const val PREFS_NAME = "elderly_reminders"
    private const val KEY_COUNT = "reminder_count"

    fun getReminders(context: Context): List<Reminder> {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val count = prefs.getInt(KEY_COUNT, 0)
        val reminders = mutableListOf<Reminder>()

        for (i in 0 until count) {
            val title = prefs.getString("title_$i", null)
            val date = prefs.getString("date_$i", null)
            val time = prefs.getString("time_$i", null)

            if (title != null && date != null && time != null) {
                reminders.add(Reminder(title, date, time))
            }
        }

        return reminders
    }

    fun addReminder(context: Context, reminder: Reminder) {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val count = prefs.getInt(KEY_COUNT, 0)

        prefs.edit()
            .putString("title_$count", reminder.title)
            .putString("date_$count", reminder.date)
            .putString("time_$count", reminder.time)
            .putInt(KEY_COUNT, count + 1)
            .apply()
    }


    fun deleteReminder(context: Context, reminder: Reminder): Boolean {
        val reminders = getReminders(context).toMutableList()

        // Find the reminder to delete
        val index = reminders.indexOf(reminder)

        if (index == -1) {
            return false
        }

        reminders.removeAt(index)

        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val oldCount = prefs.getInt(KEY_COUNT, 0)
        val editor = prefs.edit()

        // Remove the old saved entries
        for (i in 0 until oldCount) {
            editor.remove("title_$i")
            editor.remove("date_$i")
            editor.remove("time_$i")
        }

        // Save the remaining reminders again
        reminders.forEachIndexed { i, item ->
            editor.putString("title_$i", item.title)
            editor.putString("date_$i", item.date)
            editor.putString("time_$i", item.time)
        }

        editor.putInt(KEY_COUNT, reminders.size)
        editor.apply()

        return true
    }

    fun updateReminder(
        context: Context,
        oldReminder: Reminder,
        updatedReminder: Reminder
    ): Boolean {
        val reminders = getReminders(context).toMutableList()

        val index = reminders.indexOf(oldReminder)

        if (index == -1) {
            return false
        }

        reminders[index] = updatedReminder

        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val oldCount = prefs.getInt(KEY_COUNT, 0)
        val editor = prefs.edit()

        // Remove the old saved entries
        for (i in 0 until oldCount) {
            editor.remove("title_$i")
            editor.remove("date_$i")
            editor.remove("time_$i")
        }

        // Save the updated list
        reminders.forEachIndexed { i, item ->
            editor.putString("title_$i", item.title)
            editor.putString("date_$i", item.date)
            editor.putString("time_$i", item.time)
        }

        editor.putInt(KEY_COUNT, reminders.size)
        editor.apply()

        return true
    }

}
