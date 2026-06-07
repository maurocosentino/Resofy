package com.resofy.music.fragments.settings

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import code.name.monkey.appthemehelper.ThemeStore
import com.resofy.music.App
import com.resofy.music.R
import com.resofy.music.databinding.FragmentMainSettingsBinding
import com.resofy.music.extensions.drawAboveSystemBarsWithPadding
import com.resofy.music.extensions.goToProVersion
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.resofy.music.extensions.installLanguageAndRecreate
import com.resofy.music.util.PreferenceUtil
class MainSettingsFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentMainSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onClick(view: View) {
        if (view.id == R.id.languageSettings) {
            showLanguageDialog()
            return
        }
        findNavController().navigate(
            when (view.id) {
                R.id.generalSettings -> R.id.action_mainSettingsFragment_to_appearanceSettingsFragment
                R.id.audioSettings -> R.id.action_mainSettingsFragment_to_audioSettings
                R.id.imageSettings -> R.id.action_mainSettingsFragment_to_imageSettingFragment
                R.id.aboutSettings -> R.id.action_mainSettingsFragment_to_aboutActivity
                R.id.backup_restore_settings -> R.id.action_mainSettingsFragment_to_backupFragment
                R.id.serverSettings -> R.id.action_mainSettingsFragment_to_musicProvidersFragment
                else -> R.id.action_mainSettingsFragment_to_appearanceSettingsFragment
            }
        )
    }

    private fun showLanguageDialog() {
        val names = resources.getStringArray(R.array.pref_language_names)
        val codes = resources.getStringArray(R.array.pref_language_codes)

        val currentCode = AppCompatDelegate.getApplicationLocales()
            .toLanguageTags().ifEmpty { "auto" }
        val currentIndex = codes.indexOf(currentCode).takeIf { it >= 0 } ?: 0

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.pref_language_name)
            .setSingleChoiceItems(names, currentIndex) { dialog, which ->
                val selected = codes[which]
                PreferenceUtil.languageCode = selected
                if (selected == "auto") {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                } else {
                    requireActivity().installLanguageAndRecreate(selected) {
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(selected)
                        )
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.generalSettings.setOnClickListener(this)
        binding.audioSettings.setOnClickListener(this)
        binding.imageSettings.setOnClickListener(this)
        binding.aboutSettings.setOnClickListener(this)
        binding.backupRestoreSettings.setOnClickListener(this)
        binding.serverSettings.setOnClickListener(this)
        binding.languageSettings.setOnClickListener(this)

        binding.buyProContainer.apply {
            isGone = App.isProVersion()
            setOnClickListener {
                requireContext().goToProVersion()
            }
        }
        binding.buyPremium.setOnClickListener {
            requireContext().goToProVersion()
        }
        ThemeStore.accentColor(requireContext()).let {
            binding.buyPremium.setTextColor(it)
            binding.diamondIcon.imageTintList = ColorStateList.valueOf(it)
        }

        binding.container.drawAboveSystemBarsWithPadding()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}