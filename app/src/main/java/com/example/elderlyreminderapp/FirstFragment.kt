
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
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
    private var textToSpeech: TextToSpeech? = null

    // Soft, elderly-friendly colours
    private val darkGreen = Color.rgb(45, 92, 68)
    private val cream = Color.rgb(255, 250, 240)
    private val softGreen = Color.rgb(231, 243, 232)
    private val softPink = Color.rgb(255, 232, 235)
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

        binding.root.setBackgroundColor(cream)

        // Initialise Text-to-Speech
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                setSpeechLanguage()
            }
        }

        binding.buttonFirst.text =
            if (isKiswahili) {
                "➕ Ongeza Kikumbusho"
            } else {
                "➕ Add a Reminder"
            }

        // Open Add Reminder screen
        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(
                R.id.action_FirstFragment_to_SecondFragment
            )
        }

        // Language switch
        binding.languageButton.text = "🌍 Language: English"
        binding.assistantButton.text = "💬 Talk to Assistant"

        binding.languageButton.setOnClickListener {

            isKiswahili = !isKiswahili

            if (isKiswahili) {
                binding.languageButton.text = "🌍 Lugha: Kiswahili"
                binding.assistantButton.text = "🎤 Zungumza na Msaidizi"
                binding.buttonFirst.text = "➕ Ongeza Kikumbusho"
            } else {
                binding.languageButton.text = "🌍 Language: English"
                binding.assistantButton.text = "🎤 Talk to Assistant"
                binding.buttonFirst.text = "➕ Add a Reminder"
            }

            setSpeechLanguage()
            displayReminders()
        }

        // Voice assistant
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
                if (isKiswahili) "sw-KE" else "en-KE"
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

    override fun onResume() {
        super.onResume()

        if (_binding != null) {
            displayReminders()
        }
    }

    private fun makeRoundedBackground(
        backgroundColor: Int,
        strokeColor: Int = backgroundColor
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(backgroundColor)
            cornerRadius = 28f
            setStroke(2, strokeColor)
        }
    }

    private fun displayReminders() {

        val reminders =
            ReminderStorage.getReminders(requireContext())

        binding.remindersContainer.removeAllViews()
        binding.remindersContainer.orientation =
            LinearLayout.VERTICAL

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
            gravity = android.view.Gravity.CENTER
            setPadding(24, 32, 24, 32)
        }

        for (reminder in reminders) {

            // Reminder card
            val card = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 20)

                background = makeRoundedBackground(
                    softGreen,
                    Color.rgb(195, 220, 198)
                )

                elevation = 4f
            }

            // Reminder title
            val titleText = TextView(requireContext()).apply {
                text = "🌷 ${reminder.title}"
                textSize = 23f
                setTextColor(darkGreen)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, 12)
            }

            // Reminder date
            val dateText = TextView(requireContext()).apply {

                text = if (isKiswahili) {
                    "📅 Tarehe: ${reminder.date}"
                } else {
                    "📅 Date: ${reminder.date}"
                }

                textSize = 19f
                setTextColor(darkText)
                setPadding(0, 4, 0, 4)
            }

            // Reminder time
            val timeText = TextView(requireContext()).apply {

                text = if (isKiswahili) {
                    "⏰ Saa: ${reminder.time}"
                } else {
                    "⏰ Time: ${reminder.time}"
                }

                textSize = 19f
                setTextColor(darkText)
                setPadding(0, 4, 0, 16)
            }

            card.addView(titleText)
            card.addView(dateText)
            card.addView(timeText)

            // EDIT BUTTON
            val editButton = Button(requireContext()).apply {

                text = if (isKiswahili) {
                    "✏️ Hariri Kikumbusho"
                } else {
                    "✏️ Edit Reminder"
                }

                textSize = 17f
                isAllCaps = false
                setTextColor(darkGreen)

                background = makeRoundedBackground(
                    softGreen,
                    Color.rgb(160, 200, 168)
                )

                contentDescription = if (isKiswahili) {
                    "Hariri ${reminder.title}"
                } else {
                    "Edit ${reminder.title}"
                }
            }

            editButton.setOnClickListener {

                val bundle = Bundle().apply {
                    putString("edit_title", reminder.title)
                    putString("edit_date", reminder.date)
                    putString("edit_time", reminder.time)
                }

                findNavController().navigate(
                    R.id.action_FirstFragment_to_SecondFragment,
                    bundle
                )
            }

            card.addView(editButton)

            // DELETE BUTTON
            val deleteButton = Button(requireContext()).apply {

                text = if (isKiswahili) {
                    "🗑️ Futa Kikumbusho"
                } else {
                    "🗑️ Delete Reminder"
                }

                textSize = 17f
                isAllCaps = false
                setTextColor(Color.rgb(130, 40, 55))

                background = makeRoundedBackground(
                    softPink,
                    Color.rgb(245, 190, 200)
                )

                contentDescription = if (isKiswahili) {
                    "Futa ${reminder.title}"
                } else {
                    "Delete ${reminder.title}"
                }
            }

            deleteButton.setOnClickListener {

                AlertDialog.Builder(requireContext())
                    .setTitle(
                        if (isKiswahili) {
                            "Futa kikumbusho? 🌷"
                        } else {
                            "Delete reminder? 🌷"
                        }
                    )
                    .setMessage(
                        if (isKiswahili) {
                            "Una uhakika unataka kufuta " +
                                    "\"${reminder.title}\"?"
                        } else {
                            "Are you sure you want to delete " +
                                    "\"${reminder.title}\"?"
                        }
                    )
                    .setPositiveButton(
                        if (isKiswahili) "Ndiyo, futa" else "Yes, delete"
                    ) { _, _ ->

                        val deleted =
                            ReminderStorage.deleteReminder(
                                requireContext(),
                                reminder
                            )

                        if (deleted) {

                            ReminderScheduler.cancelReminder(
                                requireContext(),
                                reminder
                            )

                            Toast.makeText(
                                requireContext(),
                                if (isKiswahili) {
                                    "Kikumbusho kimefutwa 💗"
                                } else {
                                    "Reminder deleted 💗"
                                },
                                Toast.LENGTH_SHORT
                            ).show()

                            displayReminders()

                        } else {

                            Toast.makeText(
                                requireContext(),
                                if (isKiswahili) {
                                    "Kikumbusho hakikuweza kufutwa."
                                } else {
                                    "Could not delete reminder."
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

            card.addView(deleteButton)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20
                marginStart = 12
                marginEnd = 12
            }

            binding.remindersContainer.addView(card, params)
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
