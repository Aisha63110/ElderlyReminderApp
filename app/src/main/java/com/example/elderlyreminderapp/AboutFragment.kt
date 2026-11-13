
package com.example.elderlyreminderapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.elderlyreminderapp.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateLanguage()

        // Apply the selected text size
        TextSizeHelper.apply(binding.root, requireContext())
    }

    private fun updateLanguage() {

        val kiswahili = AppPreferences.isKiswahili(requireContext())

        if (kiswahili) {

            binding.txtAboutTitle.text = "🌷 Kuhusu Kikumbusho"

            binding.txtAboutDescription.text =
                "Programu rahisi na rafiki iliyoundwa kusaidia wazee " +
                        "kukumbuka shughuli zao za kila siku."

            binding.txtFeatures.text =
                "Unachoweza kufanya:\n\n" +
                        "➕ Kuongeza vikumbusho\n\n" +
                        "📅 Kuchagua tarehe na saa\n\n" +
                        "🔔 Kupokea arifa za vikumbusho\n\n" +
                        "✏️ Kuhariri na kufuta vikumbusho\n\n" +
                        "🎤 Kutumia msaidizi wa sauti\n\n" +
                        "🌍 Kutumia Kiingereza au Kiswahili"

            binding.txtAboutTagline.text =
                "Tunataka kukusaidia kufanya kila siku iwe rahisi zaidi. 💚"

        } else {

            binding.txtAboutTitle.text = "🌷 About Elderly Reminder"

            binding.txtAboutDescription.text =
                "A simple and friendly reminder app designed to help " +
                        "older adults remember their daily activities."

            binding.txtFeatures.text =
                "What you can do:\n\n" +
                        "➕ Add reminders\n\n" +
                        "📅 Choose dates and times\n\n" +
                        "🔔 Receive reminder notifications\n\n" +
                        "✏️ Edit and delete reminders\n\n" +
                        "🎤 Use the voice assistant\n\n" +
                        "🌍 Use English or Kiswahili"

            binding.txtAboutTagline.text =
                "We are here to help make every day a little easier. 💚"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
