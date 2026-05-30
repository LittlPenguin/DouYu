package cn.edu.app.douyu

import android.app.Application
import cn.edu.app.douyu.core.data.DoyuAppContainer
import kotlinx.coroutines.runBlocking

class DoyuApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        runBlocking {
            DoyuAppContainer.hydrateTokenStore(this@DoyuApplication)
        }
    }
}
