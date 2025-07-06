package se.umu.calu0217.thirty

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import se.umu.calu0217.thirty.databinding.ActivityHomeBinding

/**
 * HomeActivity serves as the entry point of the app where users can start a new game
 * or see the result of their last game. It observes the game score using a ViewModel
 * and launches the GameActivity for gameplay.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    /**
     * ActivityResultLauncher to handle the result from GameActivity.
     */
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    /**
     * ViewModel for maintaining game score across configuration changes.
     */
    private lateinit var viewModel: HomeViewModel


    /**
     * Initializes the activity, sets up the UI components, and observes game score.
     * Also handles the result returned from the GameActivity.
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this, ViewModelFactory(this))[HomeViewModel::class.java]

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.gameScore.observe(this) { score ->
            if (savedInstanceState != null) {
                binding.result.text = "Total score: $score"
                binding.result.textSize = 28f
                binding.result.visibility = VISIBLE
                binding.play.text = getString(R.string.play_again)
            }
        }

        resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val score = result.data?.getIntExtra("finalScore", 0) ?: 0
                val breakdown = result.data?.getSerializableExtra("scoreBreakdown") as? HashMap<String, Int>

                binding.result.text = "Total score: $score"
                binding.result.textSize = 28f
                binding.result.visibility = VISIBLE
                binding.play.text = getString(R.string.play_again)
                viewModel.updateGameScore(score, breakdown)
                binding.scoreBoard.visibility = VISIBLE
            }
        }

        binding.play.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            resultLauncher.launch(intent)
        }

        binding.scoreBoard.setOnClickListener {
            val breakdown = viewModel.scoreBreakdown.value ?: hashMapOf()
            val score = viewModel.gameScore.value ?: 0

            val intent = Intent(this, ScoreTableActivity::class.java).apply {
                putExtra("finalScore", score)
                putExtra("scoreBreakdown", breakdown)
            }
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}