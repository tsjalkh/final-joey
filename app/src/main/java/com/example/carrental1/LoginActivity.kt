package com.example.carrental1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText: TextInputEditText = findViewById(R.id.emailAutoComplete)
        val passwordEditText: TextInputEditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)
        val signUpLinkButton: MaterialButton = findViewById(R.id.signUpLinkButton)

        animateFormIn()

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val dbHelper = DatabaseHelper.getInstance(this)
                val user = dbHelper.checkUser(email, password)

                if (user != null) {
                    getSharedPreferences("UserData", MODE_PRIVATE).edit {
                        putInt("userId", user.id)
                        putString("userName", user.name)
                        putString("userEmail", user.email)
                    }

                    Toast.makeText(this, "Welcome back, ${user.name}!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        signUpLinkButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
    }

    private fun animateFormIn() {
        val header = findViewById<View>(R.id.loginHeader)
        header.alpha = 0f
        header.translationY = -40f
        header.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(100).start()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
