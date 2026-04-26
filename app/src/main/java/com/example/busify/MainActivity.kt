package com.example.busify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.busify.core.navigation.BusifyNavigation
import com.example.busify.core.theme.BusifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BusifyTheme {
                BusifyNavigation()
            }
        }
    }
}
