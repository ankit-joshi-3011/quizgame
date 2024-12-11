package com.ankitj.quizgame

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ankitj.quizgame.databinding.ActivityResultBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ResultActivity : AppCompatActivity() {
    private lateinit var resultActivityBinding: ActivityResultBinding
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val reference = database.reference.child("scores")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    private var userCorrectAnswers = 0
    private var userWrongAnswers = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        resultActivityBinding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(resultActivityBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user?.let {
                    val userUID = it.uid
                    userCorrectAnswers = snapshot.child(userUID).child("correct").value.toString().toInt()
                    userWrongAnswers = snapshot.child(userUID).child("wrong").value.toString().toInt()

                    resultActivityBinding.textViewCorrectAnswersLabel.text = "$userCorrectAnswers"
                    resultActivityBinding.textViewWrongAnswersLabel.text = "$userWrongAnswers"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ResultActivity, getText(R.string.database_scores_retrieval_error_text), Toast.LENGTH_SHORT).show()
                Log.e("ResultActivity", getString(R.string.database_scores_retrieval_error_text), error.toException())
            }
        })

        resultActivityBinding.buttonPlayAgain.setOnClickListener {
            val intent = Intent(this@ResultActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        resultActivityBinding.buttonExit.setOnClickListener {
            finish()
        }
    }
}