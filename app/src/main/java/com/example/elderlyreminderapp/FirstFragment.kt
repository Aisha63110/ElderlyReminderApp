
package com.example.elderlyreminderapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.ContextMenu
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.elderlyreminderapp.databinding.FragmentFirstBinding
import java.util.Locale

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private var isKiswahili = false
    private var selectedReminder: Reminder? = null
    private var textToSpeech: TextToSpeech? = null

    companion object {
        private const val CONTEXT_EDIT = 1001
        private const val CONTEXT_DELETE = 1002
    }

    // Colours
    private val darkGreen = Color.rgb(45, 92, 68)
    private val cream = Color.rgb(255, 250, 240)
    private val softGreen = Color.rgb(231, 243, 232)
    private val darkText = Color.rgb(48, 57, 49)

    // Notification permission
    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                Toast.makeText(
                    requireContext(),
                    "Notifications enabled! 💗",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle("Stay on track 🌷")
                    .setMessage(
                        "Notifications are disabled. You can enable " +
                                "them in your phone settings to receive alerts."
                    )
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

    // Microphone permission
    private val microphonePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                startVoiceRecognition()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Microphone permission is needed.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    // Speech recognition result
    private val speechRecognitionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                val spokenWords = result.data
                    ?.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS
                    )
                    ?.firstOrNull()

                if (!spokenWords.isNullOrBlank()) {

                    speakResponse(
                        if (isKiswahili) {
                            "Umesema: $spokenWords"
                        } else {
                            "You said: $spokenWords"
                        }
                    )

                    Toast.makeText(
                        requireContext(),
                        spokenWords,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        isKiswahili = AppPreferences.isKiswahili(requireContext())

        binding.root.setBackgroundColor(cream)

        // Initialise Text-to-Speech
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                setSpeechLanguage()
            }
        }

        // Add Reminder button
        updateButtonTexts()

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(
                R.id.action_FirstFragment_to_SecondFragment
            )
        }

        // Voice Assistant button
        binding.assistantButton.setOnClickListener {

            val permission = Manifest.permission.RECORD_AUDIO

            if (
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startVoiceRecognition()
            } else {
                microphonePermissionLauncher.launch(permission)
            }
        }

        requestNotificationPermission()
        displayReminders()
    }

    // Update button language
    private fun updateButtonTexts() {

        binding.buttonFirst.text =
            if (isKiswahili) {
                "➕ Ongeza Kikumbusho"
            } else {
                "➕ Add a Reminder"
            }

        binding.assistantButton.text =
            if (isKiswahili) {
                "🎤 Zungumza na Msaidizi"
            } else {
                "🎤 Talk to Assistant"
            }
    }

    // Start listening to the user's voice
    private fun startVoiceRecognition() {

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                if (isKiswahili) "sw-KE" else "en-US"
            )

            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                if (isKiswahili) {
                    "Zungumza sasa..."
                } else {
                    "Speak now..."
                }
            )
        }

        try {
            speechRecognitionLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Speech recognition is not available on this device.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Choose the speaking language
    private fun setSpeechLanguage() {

        val language = if (isKiswahili) {
            Locale("sw", "KE")
        } else {
            Locale.UK
        }

        val result = textToSpeech?.setLanguage(language)

        if (
            result == TextToSpeech.LANG_MISSING_DATA ||
            result == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            textToSpeech?.language = if (isKiswahili) {
                Locale("sw")
            } else {
                Locale.UK
            }
        }
    }

    // Speak a message aloud
    private fun speakResponse(message: String) {

        setSpeechLanguage()

        textToSpeech?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "assistant_response"
        )
    }

    // Request notification permission
    private fun requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= 33) {

            val permission =
                Manifest.permission.POST_NOTIFICATIONS

            if (
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }

    // Refresh language and reminders when returning Home
    override fun onResume() {
        super.onResume()

        if (_binding != null) {

            isKiswahili = AppPreferences.isKiswahili(requireContext())

            setSpeechLanguage()
            updateButtonTexts()
            displayReminders()
        }
    }

    // Create rounded backgrounds
    private fun makeRoundedBackground(
        backgroundColor: Int,
        strokeColor: Int = backgroundColor
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(backgroundColor)
            cornerRadius = 24f
            setStroke(2, strokeColor)
        }
    }

    // Display saved reminders
    private fun displayReminders() {

        val reminders =
            ReminderStorage.getReminders(requireContext())

        binding.remindersContainer.removeAllViews()
        binding.remindersContainer.orientation =
            LinearLayout.VERTICAL

        // Empty reminders message
        binding.emptyRemindersText.apply {

            visibility = if (reminders.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }

            text = if (isKiswahili) {
                "🌸 Huna vikumbusho kwa sasa.\n" +
                        "Ongeza kikumbusho chako cha kwanza!"
            } else {
                "🌸 No reminders yet!\n" +
                        "Add your first reminder and let us help you stay on track."
            }

            textSize = 20f
            setTextColor(darkGreen)
            gravity = Gravity.CENTER
            setPadding(24, 32, 24, 32)
        }

        // Create a card for each reminder
        for (reminder in reminders) {

            // Main reminder card
            val card = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(18, 16, 18, 16)
                background = makeRoundedBackground(
                    softGreen,
                    Color.rgb(195, 220, 198)
                )
                elevation = 3f
                tag = reminder
            }

            // Long-press opens the Context Menu
            registerForContextMenu(card)

            // Header row: title and three-dot icon
            val header = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val titleText = TextView(requireContext()).apply {
                text = "🌷 ${reminder.title}"
                textSize = 21f
                setTextColor(darkGreen)
                setTypeface(null, Typeface.BOLD)

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            // Proper three-dot icon button
            val moreButton = ImageButton(requireContext()).apply {

                setImageResource(R.drawable.ic_more_vert)

                background = makeRoundedBackground(
                    cream,
                    Color.rgb(195, 220, 198)
                )

                scaleType =
                    android.widget.ImageView.ScaleType.CENTER

                setPadding(8, 8, 8, 8)

                layoutParams = LinearLayout.LayoutParams(
                    44,
                    44
                )

                contentDescription = if (isKiswahili) {
                    "Chaguo zaidi"
                } else {
                    "More options"
                }
            }

            // Popup Menu
            moreButton.setOnClickListener { button ->

                val popup = PopupMenu(requireContext(), button)

                popup.menu.add(
                    Menu.NONE,
                    CONTEXT_EDIT,
                    Menu.NONE,
                    if (isKiswahili) {
                        "✏️ Hariri Kikumbusho"
                    } else {
                        "✏️ Edit Reminder"
                    }
                )

                popup.menu.add(
                    Menu.NONE,
                    CONTEXT_DELETE,
                    Menu.NONE,
                    if (isKiswahili) {
                        "🗑️ Futa Kikumbusho"
                    } else {
                        "🗑️ Delete Reminder"
                    }
                )

                popup.setOnMenuItemClickListener { item ->

                    when (item.itemId) {

                        CONTEXT_EDIT -> {
                            editReminder(reminder)
                            true
                        }

                        CONTEXT_DELETE -> {
                            confirmDeleteReminder(reminder)
                            true
                        }

                        else -> false
                    }
                }

                popup.show()
            }

            header.addView(titleText)
            header.addView(moreButton)

            // Date
            val dateText = TextView(requireContext()).apply {

                text = if (isKiswahili) {
                    "📅 Tarehe: ${reminder.date}"
                } else {
                    "📅 ${reminder.date}"
                }

                textSize = 17f
                setTextColor(darkText)
                setPadding(0, 10, 0, 4)
            }

            // Time
            val timeText = TextView(requireContext()).apply {

                text = if (isKiswahili) {
                    "⏰ Saa: ${reminder.time}"
                } else {
                    "⏰ ${reminder.time}"
                }

                textSize = 17f
                setTextColor(darkText)
                setPadding(0, 4, 0, 0)
            }

            card.addView(header)
            card.addView(dateText)
            card.addView(timeText)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 14
                marginStart = 4
                marginEnd = 4
            }

            binding.remindersContainer.addView(card, params)

        }
        TextSizeHelper.apply(binding.root, requireContext())

    }

    // Open the Edit Reminder screen
    // Open the Edit Reminder screen
    private fun editReminder(reminder: Reminder) {

        val bundle = Bundle().apply {
            putString("edit_title", reminder.title)
            putString("edit_date", reminder.date)
            putString("edit_time", reminder.time)
            putString("edit_repeat", reminder.repeatType)
        }

        findNavController().navigate(
            R.id.action_FirstFragment_to_SecondFragment,
            bundle
        )
    }

    // Confirm before deleting a reminder
    private fun confirmDeleteReminder(reminder: Reminder) {

        AlertDialog.Builder(requireContext())
            .setTitle(
                if (isKiswahili) {
                    "Futa kikumbusho?"
                } else {
                    "Delete reminder?"
                }
            )
            .setMessage(
                if (isKiswahili) {
                    "Una uhakika unataka kufuta ${reminder.title}?"
                } else {
                    "Are you sure you want to delete ${reminder.title}?"
                }
            )
            .setPositiveButton(
                if (isKiswahili) "Ndiyo, futa" else "Yes, delete"
            ) { _, _ ->

                val deleted = ReminderStorage.deleteReminder(
                    requireContext(),
                    reminder
                )

                if (deleted) {

                    ReminderScheduler.cancelReminder(
                        requireContext(),
                        reminder
                    )

                    displayReminders()

                    Toast.makeText(
                        requireContext(),
                        if (isKiswahili) {
                            "Kikumbusho kimefutwa 💗"
                        } else {
                            "Reminder deleted 💗"
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(
                if (isKiswahili) "Ghairi" else "Cancel",
                null
            )
            .show()
    }

    // Context Menu appears when a reminder is long-pressed
    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)

        selectedReminder = v.tag as? Reminder

        menu.setHeaderTitle(
            selectedReminder?.title ?: "Reminder"
        )

        menu.add(
            Menu.NONE,
            CONTEXT_EDIT,
            Menu.NONE,
            if (isKiswahili) {
                "✏️ Hariri Kikumbusho"
            } else {
                "✏️ Edit Reminder"
            }
        )

        menu.add(
            Menu.NONE,
            CONTEXT_DELETE,
            Menu.NONE,
            if (isKiswahili) {
                "🗑️ Futa Kikumbusho"
            } else {
                "🗑️ Delete Reminder"
            }
        )
    }

    // Handle Context Menu selections
    override fun onContextItemSelected(item: MenuItem): Boolean {

        if (
            item.itemId != CONTEXT_EDIT &&
            item.itemId != CONTEXT_DELETE
        ) {
            return super.onContextItemSelected(item)
        }

        val reminder = selectedReminder
            ?: return super.onContextItemSelected(item)

        selectedReminder = null

        return when (item.itemId) {

            CONTEXT_EDIT -> {
                editReminder(reminder)
                true
            }

            CONTEXT_DELETE -> {
                confirmDeleteReminder(reminder)
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    override fun onDestroyView() {

        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null

        super.onDestroyView()
        _binding = null
    }
}
