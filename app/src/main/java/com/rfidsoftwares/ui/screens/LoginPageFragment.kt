/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.ui.screens

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.rfidsoftwares.R
import com.rfidsoftwares.common.config.AppConfig
import com.rfidsoftwares.common.auth.AuthUiStateStore
import com.rfidsoftwares.ui.base.BaseScreenFragment

class LoginPageFragment : BaseScreenFragment() {

    override fun screenTitle(): String = "Sign In"
    override fun screenSubtitle(): String? = "Enter your account details to continue"

    private val handler = Handler(Looper.getMainLooper())

    override fun createBody(inflater: LayoutInflater, container: FrameLayout): View {
        val body = inflater.inflate(R.layout.body_login, container, false)

        val captchaContainer = body.findViewById<View>(R.id.captchaContainer)
        val captchaConfigErrorPanel = body.findViewById<View>(R.id.captchaConfigErrorPanel)
        val captchaErrorTitle: TextView = captchaConfigErrorPanel.findViewById(R.id.statePanelTitle)
        val captchaErrorMsg: TextView = captchaConfigErrorPanel.findViewById(R.id.statePanelMessage)
        val captchaErrorAction: Button = captchaConfigErrorPanel.findViewById(R.id.statePanelActionButton)

        val usernameInput: TextInputEditText = body.findViewById(R.id.usernameInput)
        val passwordInput: TextInputEditText = body.findViewById(R.id.passwordInput)
        val captchaInputLayout = body.findViewById<View>(R.id.captchaInputLayout)
        val captchaInput = body.findViewById<TextInputEditText>(R.id.captchaInput)
        val loginSubmitButton: MaterialButton = body.findViewById(R.id.loginSubmitButton)
        val loadingContainer: FrameLayout = body.findViewById(R.id.loginLoadingContainer)

        val captchaEnabled = AppConfig.CaptchaConfig.CAPTCHA_ENABLED
        val siteKey = AppConfig.CaptchaConfig.CAPTCHA_SITE_KEY
        val captchaBlocked = captchaEnabled && siteKey.isBlank()

        loginSubmitButton.setOnClickListener {
            if (captchaBlocked) return@setOnClickListener

            AuthUiStateStore.username = usernameInput.text?.toString()?.trim().orEmpty()
            AuthUiStateStore.password = passwordInput.text?.toString()?.trim().orEmpty()
            AuthUiStateStore.captchaToken = captchaInput.text?.toString()?.trim().orEmpty().ifBlank { null }

            loadingContainer.removeAllViews()
            val loading = inflater.inflate(R.layout.view_state_loading, loadingContainer, false)
            val msg: TextView = loading.findViewById(R.id.loadingMessage)
            msg.text = "Signing in..."
            loadingContainer.addView(loading)
            loadingContainer.visibility = View.VISIBLE
            loginSubmitButton.isEnabled = false

            handler.postDelayed(
                {
                    loadingContainer.visibility = View.GONE
                    findNavController().navigate(R.id.action_loginPage_to_connectApiPageFragment)
                },
                750
            )
        }

        return body
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }
}

