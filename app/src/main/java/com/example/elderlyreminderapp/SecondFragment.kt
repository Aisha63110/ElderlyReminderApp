
package com.example.elderlyreminderapp

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.elderlyreminderapp.databinding.FragmentSecondBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private var selectedDate = ""
    private var selectedTime = ""
    private var originalReminder: Reminder? = null

    private val darkGreen = Color.rgb(45, 92, 68)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editTitle = arguments?.getString("edit_title")
        val editDate = arguments?.getString("edit_date")
        val editTime = arguments?.getString("edit_time")
        val editRepeat = arguments?.getString("edit_repeat") ?: "None"

        val isEditing = editTitle != null &&
                editDate != null &&
                editTime != null

        binding.addEditHeading.text =
            if (isEditing) "Edit Reminder 🌷" else "Add Reminder 🌷"

        if (isEditing) {
            originalReminder = Reminder(
                editTitle!!,
                editDate!!,
                editTime!!,
                editRepeat
            )

            selectedDate = editDate
            selectedTime = editTime

            binding.reminderTitle.setText(editTitle)
            binding.dateButton.text = "📅 ${displayDate(editDate)}"
            binding.timeButton.text = "⏰ ${displayTime(editTime)}"
            binding.saveReminderButton.text = "💚 Save Changes"

        } else {
            originalReminder = null
            selectedDate = ""
            selectedTime = ""

            binding.dateButton.text = "📅 Choose Date"
            binding.timeButton.text = "⏰ Choose Time"
            binding.saveReminderButton.text = "💚 Save Reminder"
        }

        binding.reminderTitle.apply {
            setTextColor(darkGreen)
            textSize = 20f
        }
        // Restore the repeat option when editing
        val repeatOptions = resources.getStringArray(R.array.repeat_options)

        val selectedPosition = repeatOptions.indexOf(editRepeat)

        binding.repeatSpinner.setSelection(
            if (selectedPosition >= 0) selectedPosition else 0
        )


        binding.dateButton.setOnClickListener {
            showDatePicker()
        }

        binding.timeButton.setOnClickListener {
            showTimePicker()
        }

        binding.saveReminderButton.setOnClickListener {
            saveReminder()

            TextSizeHelper.apply(binding.root, requireContext())
        }
    }

    // FIX FOR NUMBER PICKER SCROLLING
    private fun enableWheelScrolling(picker: NumberPicker) {
        picker.descendantFocusability =
            ViewGroup.FOCUS_BLOCK_DESCENDANTS

        picker.isFocusable = true
        picker.isFocusableInTouchMode = true
        picker.isClickable = true

        picker.setOnTouchListener { view, event ->
            view.parent?.requestDisallowInterceptTouchEvent(true)

            if (event.action == android.view.MotionEvent.ACTION_UP ||
                event.action == android.view.MotionEvent.ACTION_CANCEL
            ) {
                view.parent?.requestDisallowInterceptTouchEvent(false)
            }

            false
        }
    }

    // DATE PICKER
    private fun showDatePicker() {

        val calendar = Calendar.getInstance()

        if (selectedDate.isNotEmpty()) {
            try {
                val parser = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                )
                parser.isLenient = false

                val parsed = parser.parse(selectedDate)

                if (parsed != null) {
                    calendar.time = parsed
                }
            } catch (_: Exception) {
            }
        }

        val dialogView = layoutInflater.inflate(
            R.layout.dialog_date_picker,
            null
        )

        val dayPicker =
            dialogView.findViewById<NumberPicker>(R.id.day_picker)

        val monthPicker =
            dialogView.findViewById<NumberPicker>(R.id.month_picker)

        val yearPicker =
            dialogView.findViewById<NumberPicker>(R.id.year_picker)

        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr",
            "May", "Jun", "Jul", "Aug",
            "Sep", "Oct", "Nov", "Dec"
        )

        val currentYear = Calendar.getInstance()
            .get(Calendar.YEAR)

        dayPicker.minValue = 1
        dayPicker.maxValue = 31

        monthPicker.minValue = 0
        monthPicker.maxValue = 11
        monthPicker.displayedValues = months

        yearPicker.minValue = currentYear
        yearPicker.maxValue = currentYear + 10

        dayPicker.value =
            calendar.get(Calendar.DAY_OF_MONTH)

        monthPicker.value =
            calendar.get(Calendar.MONTH)

        yearPicker.value =
            calendar.get(Calendar.YEAR)

        fun updateMaximumDay() {
            val temp = Calendar.getInstance()

            temp.set(Calendar.YEAR, yearPicker.value)
            temp.set(Calendar.MONTH, monthPicker.value)
            temp.set(Calendar.DAY_OF_MONTH, 1)

            val maximum =
                temp.getActualMaximum(Calendar.DAY_OF_MONTH)

            dayPicker.maxValue = maximum

            if (dayPicker.value > maximum) {
                dayPicker.value = maximum
            }
        }

        monthPicker.setOnValueChangedListener { _, _, _ ->
            updateMaximumDay()
        }

        yearPicker.setOnValueChangedListener { _, _, _ ->
            updateMaximumDay()
        }

        // Apply scrolling fix after configuring the wheels
        enableWheelScrolling(dayPicker)
        enableWheelScrolling(monthPicker)
        enableWheelScrolling(yearPicker)

        val dialog = AlertDialog.Builder(
            requireContext(),
            R.style.ReminderDialogTheme
        )
            .setView(dialogView)
            .setPositiveButton("Confirm Date", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(darkGreen)

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                ?.setTextColor(darkGreen)

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setOnClickListener {

                    val chosen = Calendar.getInstance()

                    chosen.set(Calendar.YEAR, yearPicker.value)
                    chosen.set(Calendar.MONTH, monthPicker.value)
                    chosen.set(Calendar.DAY_OF_MONTH, dayPicker.value)

                    chosen.set(Calendar.HOUR_OF_DAY, 0)
                    chosen.set(Calendar.MINUTE, 0)
                    chosen.set(Calendar.SECOND, 0)
                    chosen.set(Calendar.MILLISECOND, 0)

                    val today = Calendar.getInstance()

                    today.set(Calendar.HOUR_OF_DAY, 0)
                    today.set(Calendar.MINUTE, 0)
                    today.set(Calendar.SECOND, 0)
                    today.set(Calendar.MILLISECOND, 0)

                    if (chosen.before(today)) {
                        Toast.makeText(
                            requireContext(),
                            "Please choose today or a future date",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    selectedDate = String.format(
                        Locale.US,
                        "%04d-%02d-%02d",
                        yearPicker.value,
                        monthPicker.value + 1,
                        dayPicker.value
                    )

                    binding.dateButton.text =
                        "📅 ${displayDate(selectedDate)}"

                    dialog.dismiss()
                }
        }

        dialog.show()
    }

    // TIME PICKER
    private fun showTimePicker() {

        val calendar = Calendar.getInstance()

        if (selectedTime.isNotEmpty()) {
            try {
                val parts = selectedTime.split(":")

                calendar.set(
                    Calendar.HOUR_OF_DAY,
                    parts[0].toInt()
                )

                calendar.set(
                    Calendar.MINUTE,
                    parts[1].toInt()
                )
            } catch (_: Exception) {
            }
        }

        val dialogView = layoutInflater.inflate(
            R.layout.dialog_time_picker,
            null
        )

        val hourPicker =
            dialogView.findViewById<NumberPicker>(R.id.hour_picker)

        val minutePicker =
            dialogView.findViewById<NumberPicker>(R.id.minute_picker)

        val ampmPicker =
            dialogView.findViewById<NumberPicker>(R.id.ampm_picker)

        val hour24 = calendar.get(Calendar.HOUR_OF_DAY)

        hourPicker.minValue = 1
        hourPicker.maxValue = 12

        hourPicker.value = when {
            hour24 % 12 == 0 -> 12
            else -> hour24 % 12
        }

        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        minutePicker.displayedValues = Array(60) {
            String.format(Locale.US, "%02d", it)
        }
        minutePicker.value = calendar.get(Calendar.MINUTE)

        ampmPicker.minValue = 0
        ampmPicker.maxValue = 1
        ampmPicker.displayedValues = arrayOf("AM", "PM")
        ampmPicker.value = if (hour24 >= 12) 1 else 0

        // Apply scrolling fix after configuring the wheels
        enableWheelScrolling(hourPicker)
        enableWheelScrolling(minutePicker)
        enableWheelScrolling(ampmPicker)

        val dialog = AlertDialog.Builder(
            requireContext(),
            R.style.ReminderDialogTheme
        )
            .setView(dialogView)
            .setPositiveButton("Confirm Time", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(darkGreen)

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                ?.setTextColor(darkGreen)

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setOnClickListener {

                    var hour = hourPicker.value

                    if (ampmPicker.value == 1 && hour != 12) {
                        hour += 12
                    } else if (ampmPicker.value == 0 && hour == 12) {
                        hour = 0
                    }

                    selectedTime = String.format(
                        Locale.US,
                        "%02d:%02d",
                        hour,
                        minutePicker.value
                    )

                    binding.timeButton.text =
                        "⏰ ${displayTime(selectedTime)}"

                    dialog.dismiss()
                }
        }

        dialog.show()
    }

    // SAVE OR UPDATE REMINDER
    private fun saveReminder() {

        val title = binding.reminderTitle.text
            .toString()
            .trim()

        if (title.isEmpty()) {
            binding.reminderTitle.error =
                "Please enter what you want to remember"
            return
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Please choose a date",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (selectedTime.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Please choose a time",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val repeatType = when (binding.repeatSpinner.selectedItemPosition) {
            1 -> "Daily"
            2 -> "Weekly"
            3 -> "Monthly"
            else -> "None"
        }

        val reminder = Reminder(
            title,
            selectedDate,
            selectedTime,
            repeatType
        )


        val context = requireContext()

        if (originalReminder != null) {

            val oldReminder = originalReminder!!

            val updated = ReminderStorage.updateReminder(
                context,
                oldReminder,
                reminder
            )

            if (!updated) {
                Toast.makeText(
                    context,
                    "Could not update reminder. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            ReminderScheduler.cancelReminder(
                context,
                oldReminder
            )

            val scheduled = ReminderScheduler.scheduleReminder(
                context,
                title,
                selectedDate,
                selectedTime
            )

            Toast.makeText(
                context,
                if (scheduled) {
                    "💚 Reminder updated successfully!"
                } else {
                    "Reminder updated, but notification could not be scheduled."
                },
                Toast.LENGTH_LONG
            ).show()

        } else {

            ReminderStorage.addReminder(context, reminder)

            val scheduled = ReminderScheduler.scheduleReminder(
                context,
                title,
                selectedDate,
                selectedTime
            )

            Toast.makeText(
                context,
                if (scheduled) {
                    "🌷 Reminder saved successfully!"
                } else {
                    "Reminder saved, but notification could not be scheduled."
                },
                Toast.LENGTH_LONG
            ).show()
        }

        findNavController().navigate(
            R.id.action_SecondFragment_to_FirstFragment
        )
    }

    private fun displayDate(date: String): String {
        return try {
            val parser = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )
            parser.isLenient = false

            val parsed = parser.parse(date)

            if (parsed != null) {
                SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.US
                ).format(parsed)
            } else {
                date
            }
        } catch (_: Exception) {
            date
        }
    }

    private fun displayTime(time: String): String {
        return try {
            val parser = SimpleDateFormat(
                "HH:mm",
                Locale.US
            )
            parser.isLenient = false

            val parsed = parser.parse(time)

            if (parsed != null) {
                SimpleDateFormat(
                    "hh:mm a",
                    Locale.US
                ).format(parsed)
            } else {
                time
            }
        } catch (_: Exception) {
            time
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
