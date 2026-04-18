package com.example.droponairdemo.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.droponair.sdk.DropOnAir
import com.droponair.sdk.DropOnAirConfig
import com.example.droponairdemo.BuildConfig
import com.example.droponairdemo.R
import com.example.droponairdemo.data.BackendService
import com.example.droponairdemo.ui.chat.ChatActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val backend = BackendService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUserId   = findViewById<EditText>(R.id.etUserId)
        val btnLogin   = findViewById<Button>(R.id.btnLogin)
        val tvError    = findViewById<TextView>(R.id.tvError)
        val progress   = findViewById<ProgressBar>(R.id.progress)

        btnLogin.setOnClickListener {
            val userId = etUserId.text.toString().trim()
            if (userId.isEmpty()) { tvError.text = "Enter a user ID"; return@setOnClickListener }

            btnLogin.isEnabled = false
            progress.visibility = android.view.View.VISIBLE
            tvError.text = ""

            lifecycleScope.launch {
                try {
                    // 1. Authenticate with the demo backend
                    backend.login(userId)

                    // 2. Initialise DropOnAir SDK
                    DropOnAir.initialize(
                        applicationContext,
                        DropOnAirConfig(
                            appId        = BuildConfig.DROPONAIR_APP_ID,
                            publicApiKey = BuildConfig.DROPONAIR_PUBLIC_API_KEY,
                            getUserJwt   = { backend.getJwt() },
                            tokenExchangeEndpoint = "${BuildConfig.BACKEND_URL}/api/droponair/token",
                            keyDirectoryEndpoint  = "${BuildConfig.BACKEND_URL}/api/droponair/keys",
                        )
                    )

                    // 3. Connect
                    DropOnAir.getInstance().connect(userId)

                    // 4. Navigate to chat
                    startActivity(Intent(this@LoginActivity, ChatActivity::class.java)
                        .putExtra("userId", userId))
                    finish()
                } catch (e: Exception) {
                    tvError.text = "Error: ${e.message}"
                    btnLogin.isEnabled = true
                } finally {
                    progress.visibility = android.view.View.GONE
                }
            }
        }
    }
}
