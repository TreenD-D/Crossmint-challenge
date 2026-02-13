package sample.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.achulkov.challenge.di.KoinInitializer

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Koin for dependency injection
        KoinInitializer.init()
        
        enableEdgeToEdge()
        setContent { App() }
    }
}