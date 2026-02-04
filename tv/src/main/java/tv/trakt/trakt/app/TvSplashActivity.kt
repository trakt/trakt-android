package tv.trakt.trakt.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class TvSplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, TvActivity::class.java))
        finish()
    }
}
