package se.umu.calu0217.thirty

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import se.umu.calu0217.thirty.databinding.ActivityScoreTableBinding


/**
 * Activity that displays the score breakdown and total score from the last game of Thirty.
 * It retrieves the final score and score breakdown from the intent extras and displays them in a list.
 */
class ScoreTableActivity : AppCompatActivity() {

    private lateinit var viewModel: ScoreTableViewModel

    private lateinit var binding: ActivityScoreTableBinding

    /**
     * Initializes the activity, sets up view binding, retrieves score data from the intent, and populates the UI.
     *
     * @param savedInstanceState The saved state of the activity, or null if none.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this, ViewModelFactory(this))[ScoreTableViewModel::class.java]
        binding = ActivityScoreTableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val scoreBreakdown = intent.getSerializableExtra("scoreBreakdown") as? HashMap<String, Int> ?: hashMapOf()
            val finalScore = intent.getIntExtra("finalScore", 0)
            viewModel.setData(finalScore, scoreBreakdown)
        }

        binding.totalScore.text = "Total Score last game: ${viewModel.getFinalScore()}"

        val scoreList = viewModel.getScoreBreakdown().map { (label, value) ->
            String.format("Option %-8s %30d", label, value)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, scoreList)
        binding.scoreList.adapter = adapter

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}