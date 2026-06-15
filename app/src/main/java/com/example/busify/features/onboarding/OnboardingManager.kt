package com.example.busify.features.onboarding

import android.content.Context
import android.content.SharedPreferences

class OnboardingManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("busify_prefs", Context.MODE_PRIVATE)

    fun isCompleted(): Boolean = prefs.getBoolean("onboarding_completed", false)

    fun complete() {
        prefs.edit().putBoolean("onboarding_completed", true).apply()
    }

    fun reset() {
        prefs.edit().putBoolean("onboarding_completed", false).apply()
    }
}
