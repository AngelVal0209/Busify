package com.example.busify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.busify.core.navigation.BusifyNavigation
import com.example.busify.core.theme.BusifyTheme
import com.example.busify.features.onboarding.OnboardingManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import timber.log.Timber

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (android.util.Log.isLoggable("Busify", android.util.Log.DEBUG)) {
            Timber.plant(Timber.DebugTree())
        }

        FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        setContent {
            val onboardingManager = remember { OnboardingManager(applicationContext) }
            var onboardingComplete by remember { mutableStateOf(onboardingManager.isCompleted()) }

            BusifyTheme {
                BusifyNavigation(
                    onboardingComplete = onboardingComplete,
                    onOnboardingComplete = {
                        onboardingManager.complete()
                        onboardingComplete = true
                    }
                )
            }
        }
    }
}
