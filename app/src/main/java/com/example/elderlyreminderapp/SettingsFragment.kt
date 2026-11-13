
package com.example.elderlyreminderapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.elderlyreminderapp.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateLanguageTexts()

        // Apply the saved text size to Settings
        TextSizeHelper.apply(binding.root, requireContext())

        // LANGUAGE
        binding.btnLanguage.setOnClickListener {

            val languages = arrayOf("English", "Kiswahili")

            val currentLanguage =
                if (AppPreferences.isKiswahili(requireContext())) 1 else 0

            AlertDialog.Builder(requireContext())
                .setTitle(
                    if (currentLanguage == 1) "Chagua Lugha"
                    else "Choose Language"
                )
                .setSingleChoiceItems(languages, currentLanguage) {
                        dialog, which ->

                    AppPreferences.saveLanguage(
                        requireContext(),
                        which == 1
                    )

                    dialog.dismiss()
                    updateLanguageTexts()

                    val message = if (which == 1) {
                        "Lugha imebadilishwa kuwa Kiswahili."
                    } else {
                        "Language changed to English."
                    }

                    Toast.makeText(
                        requireContext(),
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton(
                    if (currentLanguage == 1) "Ghairi" else "Cancel",
                    null
                )
                .show()
        }

        // TEXT SIZE
        binding.btnTextSize.setOnClickListener {

            val kiswahili =
                AppPreferences.isKiswahili(requireContext())

            val sizes = if (kiswahili) {
                arrayOf("Ndogo", "Wastani", "Kubwa")
            } else {
                arrayOf("Small", "Medium", "Large")
            }

            val currentSize =
                AppPreferences.getTextSize(requireContext())

            AlertDialog.Builder(requireContext())
                .setTitle(
                    if (kiswahili) "Chagua Ukubwa wa Maandishi"
                    else "Choose Text Size"
                )
                .setSingleChoiceItems(sizes, currentSize) {
                        dialog, which ->

                    AppPreferences.saveTextSize(
                        requireContext(),
                        which
                    )

                    // Apply the selected size immediately
                    TextSizeHelper.apply(
                        binding.root,
                        requireContext()
                    )

                    val message = if (kiswahili) {
                        "Ukubwa wa maandishi umebadilishwa."
                    } else {
                        "Text size changed to ${sizes[which]}."
                    }

                    Toast.makeText(
                        requireContext(),
                        message,
                        Toast.LENGTH_SHORT
                    ).show()

                    dialog.dismiss()
                }
                .setNegativeButton(
                    if (kiswahili) "Ghairi" else "Cancel",
                    null
                )
                .show()
        }

        // PRIVACY INFORMATION
        binding.btnPrivacy.setOnClickListener {

            val kiswahili =
                AppPreferences.isKiswahili(requireContext())

            AlertDialog.Builder(requireContext())
                .setTitle(
                    if (kiswahili) "Taarifa za Faragha"
                    else "Privacy Information"
                )
                .setMessage(
                    if (kiswahili) {
                        "Vikumbusho vyako vinahifadhiwa kwenye simu yako. " +
                                "Tafadhali linda simu yako ili kulinda taarifa zako."
                    } else {
                        "Your reminders are stored on your device. " +
                                "Please keep your device secure to protect your information."
                    }
                )
                .setPositiveButton(
                    if (kiswahili) "Sawa" else "OK",
                    null
                )
                .show()
        }
    }

    private fun updateLanguageTexts() {

        val kiswahili =
            AppPreferences.isKiswahili(requireContext())

        if (kiswahili) {

            binding.txtSettingsTitle.text = "⚙️ Mipangilio"

            binding.txtSettingsDescription.text =
                "Badilisha jinsi unavyotumia vikumbusho vyako."

            binding.txtLanguageTitle.text = "🌍 Lugha"
            binding.btnLanguage.text = "Badilisha Lugha"

            binding.txtTextSizeTitle.text = "🔠 Ukubwa wa Maandishi"
            binding.btnTextSize.text = "Badilisha Ukubwa wa Maandishi"

            binding.txtPrivacyTitle.text = "🔒 Faragha"
            binding.btnPrivacy.text = "Taarifa za Faragha"

        } else {

            binding.txtSettingsTitle.text = "⚙️ Settings"

            binding.txtSettingsDescription.text =
                "Personalise your reminder experience."

            binding.txtLanguageTitle.text = "🌍 Language"
            binding.btnLanguage.text = "Change Language"

            binding.txtTextSizeTitle.text = "🔠 Text Size"
            binding.btnTextSize.text = "Adjust Text Size"

            binding.txtPrivacyTitle.text = "🔒 Privacy"
            binding.btnPrivacy.text = "Privacy Information"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
