package cn.edu.app.douyu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cn.edu.app.douyu.core.navigation.DoyuApp
import cn.edu.app.douyu.ui.theme.DouYuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DouYuTheme {
                DoyuApp()
            }
        }
    }
}
