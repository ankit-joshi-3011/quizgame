package com.ankitj.quizgame

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ankitj.quizgame.databinding.ActivityQuizBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class QuizActivity : AppCompatActivity() {
    private lateinit var quizActivityBinding: ActivityQuizBinding
    private var database = FirebaseDatabase.getInstance()
    private var databaseReference = database.reference.child("questions")

    private var questionCount = 0L
    private var questionToRetrieve = 1

    private var question = ""
    private var answer1 = ""
    private var answer2 = ""
    private var answer3 = ""
    private var answer4 = ""
    private var correctAnswer = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        quizActivityBinding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(quizActivityBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        gameLogic()

        quizActivityBinding.buttonNext.setOnClickListener {
            gameLogic()
        }

        quizActivityBinding.buttonFinish.setOnClickListener {

        }

        quizActivityBinding.textViewAnswer1.setOnClickListener {

        }

        quizActivityBinding.textViewAnswer2.setOnClickListener {

        }

        quizActivityBinding.textViewAnswer3.setOnClickListener {

        }

        quizActivityBinding.textViewAnswer4.setOnClickListener {

        }
    }

    private fun gameLogic() {
        databaseReference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                questionCount = snapshot.childrenCount

                if (questionToRetrieve <= questionCount) {
                    question = snapshot.child(questionToRetrieve.toString()).child("question").value.toString()
                    answer1 = snapshot.child(questionToRetrieve.toString()).child("option1").value.toString()
                    answer2 = snapshot.child(questionToRetrieve.toString()).child("option2").value.toString()
                    answer3 = snapshot.child(questionToRetrieve.toString()).child("option3").value.toString()
                    answer4 = snapshot.child(questionToRetrieve.toString()).child("option4").value.toString()
                    correctAnswer = snapshot.child(questionToRetrieve.toString()).child("answer").value.toString()

                    quizActivityBinding.textViewQuestion.text = question
                    quizActivityBinding.textViewAnswer1.text = answer1
                    quizActivityBinding.textViewAnswer2.text = answer2
                    quizActivityBinding.textViewAnswer3.text = answer3
                    quizActivityBinding.textViewAnswer4.text = answer4

                    quizActivityBinding.progressBarQuiz.visibility = View.INVISIBLE
                    quizActivityBinding.statisticsLinearLayout.visibility = View.VISIBLE
                    quizActivityBinding.gameAreaLinearLayout.visibility = View.VISIBLE
                    quizActivityBinding.buttonLinearLayout.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this@QuizActivity, getString(R.string.quiz_completed_text), Toast.LENGTH_SHORT).show()
                }

                questionToRetrieve++
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@QuizActivity, getText(R.string.database_question_retrieval_error_text), Toast.LENGTH_SHORT).show()
                Log.e("QuizActivity", getString(R.string.database_question_retrieval_error_text), error.toException())
            }
        })
    }
}