package com.ankitj.quizgame

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ankitj.quizgame.databinding.ActivityQuizBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.random.Random

class QuizActivity : AppCompatActivity() {
    private lateinit var quizActivityBinding: ActivityQuizBinding
    private var database = FirebaseDatabase.getInstance()
    private var databaseReferenceQuestions = database.reference.child("questions")

    private var questionCount = 0L
    private var questionToRetrieve = 0
    private val numberOfQuestionsToRetrieve = 5

    private var question = ""
    private var answer1 = ""
    private var answer2 = ""
    private var answer3 = ""
    private var answer4 = ""
    private var correctAnswer = ""
    private var userAnswer = ""
    private var numberOfCorrectAnswers = 0
    private var numberOfWrongAnswers = 0
    private val questions = HashSet<Int>()

    private lateinit var timer: CountDownTimer
    private val totalTime = 60_000L
    private var leftTime = totalTime

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser
    private val databaseReferenceScores = database.reference.child("scores")

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

        do {
            questions.add(Random.nextInt(1, 11))
        } while (questions.size < numberOfQuestionsToRetrieve)

        gameLogic()

        quizActivityBinding.buttonNext.setOnClickListener {
            resetTimer()
            gameLogic()
        }

        quizActivityBinding.buttonFinish.setOnClickListener {
            sendScores()
        }

        quizActivityBinding.textViewAnswer1.setOnClickListener(getAnswerTextViewOnClickListener("option1"))
        quizActivityBinding.textViewAnswer2.setOnClickListener(getAnswerTextViewOnClickListener("option2"))
        quizActivityBinding.textViewAnswer3.setOnClickListener(getAnswerTextViewOnClickListener("option3"))
        quizActivityBinding.textViewAnswer4.setOnClickListener(getAnswerTextViewOnClickListener("option4"))
    }

    private fun getAnswerTextViewOnClickListener(answer: String): View.OnClickListener {
        return View.OnClickListener {
            pauseTimer()

            userAnswer = answer

            when (correctAnswer) {
                "option1" -> quizActivityBinding.textViewAnswer1.setBackgroundColor(Color.GREEN)
                "option2" -> quizActivityBinding.textViewAnswer2.setBackgroundColor(Color.GREEN)
                "option3" -> quizActivityBinding.textViewAnswer3.setBackgroundColor(Color.GREEN)
                "option4" -> quizActivityBinding.textViewAnswer4.setBackgroundColor(Color.GREEN)
            }

            if (correctAnswer == userAnswer) {
                numberOfCorrectAnswers++
                quizActivityBinding.textViewCorrectAnswers.text = "$numberOfCorrectAnswers"
            } else {
                when (userAnswer) {
                    "option1" -> quizActivityBinding.textViewAnswer1.setBackgroundColor(Color.RED)
                    "option2" -> quizActivityBinding.textViewAnswer2.setBackgroundColor(Color.RED)
                    "option3" -> quizActivityBinding.textViewAnswer3.setBackgroundColor(Color.RED)
                    "option4" -> quizActivityBinding.textViewAnswer4.setBackgroundColor(Color.RED)
                }
                numberOfWrongAnswers++
                quizActivityBinding.textViewWrongAnswers.text = "$numberOfWrongAnswers"
            }

            disableTextViewsFromBeingClickable()
        }
    }

    private fun gameLogic() {
        restoreUI()

        databaseReferenceQuestions.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                questionCount = snapshot.childrenCount

                if (questionToRetrieve < numberOfQuestionsToRetrieve) {
                    val questionChild = questions.elementAt(questionToRetrieve).toString()

                    question = snapshot.child(questionChild).child("question").value.toString()
                    answer1 = snapshot.child(questionChild).child("option1").value.toString()
                    answer2 = snapshot.child(questionChild).child("option2").value.toString()
                    answer3 = snapshot.child(questionChild).child("option3").value.toString()
                    answer4 = snapshot.child(questionChild).child("option4").value.toString()
                    correctAnswer = snapshot.child(questionChild).child("answer").value.toString()

                    quizActivityBinding.textViewQuestion.text = question
                    quizActivityBinding.textViewAnswer1.text = answer1
                    quizActivityBinding.textViewAnswer2.text = answer2
                    quizActivityBinding.textViewAnswer3.text = answer3
                    quizActivityBinding.textViewAnswer4.text = answer4

                    quizActivityBinding.progressBarQuiz.visibility = View.INVISIBLE
                    quizActivityBinding.statisticsLinearLayout.visibility = View.VISIBLE
                    quizActivityBinding.gameAreaLinearLayout.visibility = View.VISIBLE
                    quizActivityBinding.buttonLinearLayout.visibility = View.VISIBLE

                    startTimer()
                } else {
                    AlertDialog.Builder(this@QuizActivity)
                        .setTitle(getText(R.string.app_name))
                        .setMessage(getText(R.string.quiz_completed_text))
                        .setCancelable(false)
                        .setPositiveButton(getText(R.string.see_result_button_text)) { _, _ ->
                            sendScores()
                        }
                        .setNegativeButton(getText(R.string.play_again_button_text)) { _, _ ->
                            val intent = Intent(this@QuizActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .create()
                        .show()
                }

                questionToRetrieve++
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@QuizActivity, getText(R.string.database_question_retrieval_error_text), Toast.LENGTH_SHORT).show()
                Log.e("QuizActivity", getString(R.string.database_question_retrieval_error_text), error.toException())
            }
        })
    }

    private fun disableTextViewsFromBeingClickable() {
        quizActivityBinding.textViewAnswer1.isClickable = false
        quizActivityBinding.textViewAnswer2.isClickable = false
        quizActivityBinding.textViewAnswer3.isClickable = false
        quizActivityBinding.textViewAnswer4.isClickable = false
    }

    private fun restoreUI() {
        quizActivityBinding.textViewAnswer1.setBackgroundColor(Color.WHITE)
        quizActivityBinding.textViewAnswer2.setBackgroundColor(Color.WHITE)
        quizActivityBinding.textViewAnswer3.setBackgroundColor(Color.WHITE)
        quizActivityBinding.textViewAnswer4.setBackgroundColor(Color.WHITE)

        quizActivityBinding.textViewAnswer1.isClickable = true
        quizActivityBinding.textViewAnswer2.isClickable = true
        quizActivityBinding.textViewAnswer3.isClickable = true
        quizActivityBinding.textViewAnswer4.isClickable = true
    }

    private fun startTimer() {
        timer = object : CountDownTimer(leftTime, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                leftTime = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                resetTimer()
                quizActivityBinding.textViewQuestion.text = getText(R.string.time_up_text)
                disableTextViewsFromBeingClickable()
            }
        }.start()
    }

    private fun updateCountDownText() {
        val remainingTimeInSeconds: Int = (leftTime / 1000).toInt()
        quizActivityBinding.textViewTime.text = "$remainingTimeInSeconds"
    }

    private fun pauseTimer() {
        timer.cancel()
    }

    private fun resetTimer() {
        pauseTimer()
        leftTime = totalTime
        updateCountDownText()
    }

    private fun sendScores() {
        user?.let {
            val userUID = it.uid
            databaseReferenceScores.child(userUID).child("correct").setValue(numberOfCorrectAnswers)
            databaseReferenceScores.child(userUID).child("wrong").setValue(numberOfWrongAnswers).addOnSuccessListener {
                Toast.makeText(this, getString(R.string.score_send_to_database_successful_text), Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ResultActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}