package com.example.carrental1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Skip sign-up screen if user is already logged in
        val userId = getSharedPreferences("UserData", MODE_PRIVATE).getInt("userId", -1)
        if (userId != -1) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_sign_up)

        val nameEditText: TextInputEditText = findViewById(R.id.nameEditText)
        val emailEditText: TextInputEditText = findViewById(R.id.emailEditText)
        val passwordEditText: TextInputEditText = findViewById(R.id.passwordEditText)
        val signUpButton: MaterialButton = findViewById(R.id.signUpButton)
        val loginLinkButton: MaterialButton = findViewById(R.id.loginLinkButton)

        signUpButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (isValidPassword(password)) {
                    val dbHelper = DatabaseHelper(this)
                    val result = dbHelper.addUser(name, email, password)
                    dbHelper.close()

                    if (result != -1L) {
                        showSuccessDialog()
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
            finish()
        }
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!]).*$"
        return password.matches(Regex(passwordPattern))
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Account Created")
            .setMessage("Your account has been created successfully! You can now log in.")
            .setPositiveButton("OK") { _, _ ->
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setCancelable(false)
            .show()
    }
}