package com.ankitj.quizgame

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ankitj.quizgame.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var forgotPasswordActivityBinding: ActivityForgotPasswordBinding
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        forgotPasswordActivityBinding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(forgotPasswordActivityBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        forgotPasswordActivityBinding.buttonReset.setOnClickListener {
            val email = forgotPasswordActivityBinding.editTextForgotPasswordEmail.text.toString()

            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@ForgotPasswordActivity, getText(R.string.forgot_password_email_sent_successfully), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@ForgotPasswordActivity, getText(R.string.forgot_password_email_send_failure_text), Toast.LENGTH_SHORT).show()
                    Log.e("ForgotPasswordActivity", getString(R.string.forgot_password_email_send_failure_text), task.exception)
                }
            }
        }
    }
}