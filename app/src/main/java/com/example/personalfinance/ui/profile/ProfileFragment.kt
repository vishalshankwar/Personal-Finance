package com.example.personalfinance.ui.profile

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.personalfinance.R
import com.example.personalfinance.data.AuthStorage
import com.example.personalfinance.data.ThemeStorage
import com.google.android.material.materialswitch.MaterialSwitch

class ProfileFragment : Fragment() {

    private lateinit var authStorage: AuthStorage
    private lateinit var themeStorage: ThemeStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authStorage = AuthStorage(requireContext())
        themeStorage = ThemeStorage(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = authStorage.getSavedEmail()
        val displayName = buildDisplayName(email)

        view.findViewById<TextView>(R.id.profileNameText).text = displayName
        view.findViewById<TextView>(R.id.profileEmailText).text = email

        view.findViewById<ImageView>(R.id.closeProfileButton).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<MaterialSwitch>(R.id.themeSwitch).apply {
            isChecked = themeStorage.isDarkModeEnabled()
            setOnCheckedChangeListener { _, isChecked ->
                themeStorage.setDarkModeEnabled(isChecked)
                themeStorage.applyTheme()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun buildDisplayName(email: String): String {
        val localPart = email.substringBefore("@").ifBlank { getString(R.string.profile_default_name) }
        return localPart
            .replace(".", " ")
            .replace("_", " ")
            .replace("-", " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.lowercase().replaceFirstChar { firstChar -> firstChar.uppercase() }
            }
            .ifBlank { getString(R.string.profile_default_name) }
    }
}
