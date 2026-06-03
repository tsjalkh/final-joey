package com.example.carrental1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = getSharedPreferences("UserData", MODE_PRIVATE).getInt("userId", -1)
        if (userId != -1) {
            startActivity(Intent(this, MainActivity::class.java))
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
            return
        }

        setContentView(R.layout.activity_sign_up)

        val nameEditText: TextInputEditText = findViewById(R.id.nameEditText)
        val emailEditText: TextInputEditText = findViewById(R.id.emailEditText)
        val passwordEditText: TextInputEditText = findViewById(R.id.passwordEditText)
        val signUpButton: MaterialButton = findViewById(R.id.signUpButton)
        val loginLinkButton: MaterialButton = findViewById(R.id.loginLinkButton)

        animateHeaderIn()

        signUpButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (isValidPassword(password)) {
                    val dbHelper = DatabaseHelper.getInstance(this)
                    val result = dbHelper.addUser(name, email, password)

                    if (result != -1L) {
                        val user = dbHelper.checkUser(email, password)
                        if (user != null) {
                            getSharedPreferences("UserData", MODE_PRIVATE).edit {
                                putInt("userId", user.id)
                                putString("userName", user.name)
                                putString("userEmail", user.email)
                            }
                        }
                        Toast.makeText(this, "Welcome to CedarDrive, $name!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        @Suppress("DEPRECATION")
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        finish()
                    } else {
                        Toast.makeText(this, "Registration failed. Email might already exist.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Password must contain at least one uppercase letter, one number, and one special character.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        loginLinkButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
    }

    private fun animateHeaderIn() {
        val header = findViewById<View>(R.id.signUpHeader)
        header.alpha = 0f
        header.translationY = -40f
        header.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(100).start()
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!]).*$"
        return password.matches(Regex(passwordPattern))
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
