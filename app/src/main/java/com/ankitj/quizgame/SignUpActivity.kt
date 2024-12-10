package com.ankitj.quizgame

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ankitj.quizgame.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    private lateinit var signUpActivityBinding: ActivitySignUpBinding
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        signUpActivityBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(signUpActivityBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        signUpActivityBinding.buttonSignUp.setOnClickListener {
            val email = signUpActivityBinding.editTextSignUpEmail.text.toString()
            val password = signUpActivityBinding.editTextSignUpPassword.text.toString()
            signUpWithFirebase(email, password)
        }
    }

    private fun signUpWithFirebase(email: String, password: String) {
        signUpActivityBinding.progressBarSignUp.visibility = View.VISIBLE
        signUpActivityBinding.buttonSignUp.isClickable = false

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@SignUpActivity, getText(R.string.account_creation_successful_text), Toast.LENGTH_SHORT).show()
                signUpActivityBinding.progressBarSignUp.visibility = View.INVISIBLE
                signUpActivityBinding.buttonSignUp.isClickable = true
                finish()
            } else {
                Toast.makeText(this@SignUpActivity, getText(R.string.account_creation_failure_text), Toast.LENGTH_SHORT).show()
                Log.e("SignUpActivity", getString(R.string.account_creation_failure_text), task.exception)
            }
        }
    }
}