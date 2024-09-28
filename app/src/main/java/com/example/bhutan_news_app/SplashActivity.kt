import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.bhutan_news_app.MainActivity
import com.example.bhutan_news_app.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity(){
    private val splashScreenDuration = 2000L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_splash_activity)
        // Delay for the splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            // Start the MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            // Close the SplashActivity so the user can't go back to it
            finish()
        }, splashScreenDuration)
    }
}