package se.umu.calu0217.thirty

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import se.umu.calu0217.thirty.databinding.ActivityGameBinding


/**
 * Activity that handles the game logic, user interactions, and UI updates
 * for the Thirty dice game.
 *
 * This activity manages user input for rolling dice, selecting scoring options,
 * and transitioning between rounds. It uses a ViewModel to preserve and observe
 * game state across configuration changes.
 *
 * @constructor Initializes the activity, sets up views, observers and handles game lifecycle.
 */
class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private lateinit var viewModel: GameViewModel

    private lateinit var diceImageViews: List<ImageView>

    private lateinit var spinnerOptions: MutableList<String>
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    /**
     * Called when the activity is starting. Initializes ViewModel, sets up UI, and observes game state.
     *
     * @param savedInstanceState The previously saved state of the activity, or null if none.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this, ViewModelFactory(this))[GameViewModel::class.java]
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeViewModel()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Initializes all UI setup functions: dice, spinner, buttons, and selection listeners.
     */
    private fun setupViews() {
        setupDiceClickListeners()
        setupSpinner()
        setupRollButton()
        setupCalculateButton()
        setupSpinnerSelectionListener()
    }

    /**
     * Sets click listeners on each dice ImageView to toggle selection.
     * Selected dice are visually highlighted and updated in ViewModel.
     */
    private fun setupDiceClickListeners() {
        diceImageViews = listOf(
            binding.dieOne, binding.dieTwo, binding.dieThree,
            binding.dieFour, binding.dieFive, binding.dieSix
        )

        diceImageViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                val current = viewModel.selectedDice.value?.toMutableSet() ?: mutableSetOf()

                if (current.contains(index)) {
                    imageView.background = null
                    current.remove(index)
                } else {
                    imageView.setBackgroundResource(R.drawable.die_selected)
                    current.add(index)
                }
                viewModel.setSelectedDice(current)
            }
        }
    }

    /**
     * Initializes the spinner with score options using a default array and sets the adapter.
     */
    private fun setupSpinner() {
        spinnerOptions = resources.getStringArray(R.array.score_options).toMutableList()
        spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            spinnerOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = spinnerAdapter
    }

    /**
     * Configures the roll button click behavior.
     * Handles roll action or moves to next round depending on game state.
     * Displays toast message if required conditions aren't met.
     */
    private fun setupRollButton() {
        binding.rollButton.setOnClickListener {
            val selectedScoreOption = viewModel.selectedScoreOption.value ?: "Choose your score"

            if (viewModel.nextRound.value == true && selectedScoreOption == "Choose your score") {
                Toast.makeText(this, "Choose a score option to remove before continuing!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selected = viewModel.selectedDice.value ?: emptySet()

            viewModel.rollDice(selected) { finalScore ->
                if (finalScore != null) {
                    Toast.makeText(this, "Game Over! Your final score is: $finalScore", Toast.LENGTH_LONG).show()
                    val resultIntent = Intent().apply {
                        putExtra("finalScore", finalScore)
                        putExtra("scoreBreakdown", HashMap(viewModel.scoreByOption))
                    }
                    setResult(RESULT_OK, resultIntent)

                    finish()
                }
            }
        }
    }

    /**
     * Configures the calculate button to score selected dice and update the game state.
     * Adds score, updates dice visuals and disables used dice.
     */
    private fun setupCalculateButton() {
        binding.calculate.setOnClickListener {
            val selectedScoreOption = binding.spinner.selectedItem.toString()
            viewModel.setScoreOption(selectedScoreOption)

            if (selectedScoreOption == "Choose your score") {
                Toast.makeText(this, "Please select a score option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val roundScore = viewModel.calculateScore()
            if (roundScore > 0) {
                viewModel.addScore(selectedScoreOption, roundScore)
                Toast.makeText(this, "You scored: $roundScore", Toast.LENGTH_SHORT).show()

                val selected = viewModel.selectedDice.value ?: emptySet()
                viewModel.clearSelectedDice()

                selected.forEach { index ->
                    diceImageViews[index].setBackgroundResource(R.drawable.die_calculated)
                    diceImageViews[index].isClickable = false
                }
                viewModel.addToCalculatedDice(selected)
            } else {
                Toast.makeText(this, "Invalid dice value for $selectedScoreOption", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Sets listener on the spinner to update the selected score option in the ViewModel.
     */
    private fun setupSpinnerSelectionListener() {
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selected = spinnerOptions[position]
                viewModel.setScoreOption(selected)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No-op
            }
        }
    }

    /**
     * Observes all relevant LiveData properties from the ViewModel and updates the UI accordingly.
     * This includes dice visuals, score, round info, button states, and spinner contents.
     */
    private fun observeViewModel() {
        viewModel.gameScore.observe(this) { score ->
            binding.gameScore.text = "Total score: $score"
            binding.rollButton.isEnabled = true
        }

        viewModel.rounds.observe(this) { round ->
            binding.round.text = "Round: $round / 10"
            binding.rollButton.isEnabled = true
            resetDiceUI()
        }

        viewModel.rollCount.observe(this) {
            updateRollButtonText()
            updateCalculateButtonState()
        }

        viewModel.nextRound.observe(this) {
            updateRollButtonText()
            updateCalculateButtonState()
        }

        viewModel.diceValues.observe(this) { diceValues ->
            diceValues.forEachIndexed { index, value ->
                diceImageViews[index].setImageResource(viewModel.getDieDrawable(value))
            }
        }

        viewModel.selectedDice.observe(this) { selected ->
            diceImageViews.forEachIndexed { index, imageView ->
                if (index in selected) {
                    imageView.setBackgroundResource(R.drawable.die_selected)
                }
            }
            updateCalculateButtonState()
        }

        viewModel.selectedScoreOption.observe(this) { option ->
            val index = spinnerOptions.indexOf(option)
            if (index != -1) binding.spinner.setSelection(index)
            updateCalculateButtonState()

            if (viewModel.nextRound.value == true && !viewModel.selectedScoreOption.value.equals("Choose your score")) binding.rollButton.isEnabled = true

        }

        viewModel.spinnerOptions.observe(this) { options ->
            spinnerOptions.clear()
            spinnerOptions.addAll(options)
            spinnerAdapter.notifyDataSetChanged()
        }

        viewModel.calculatedDice.observe(this) { calculated ->
            calculated.forEach { index ->
                diceImageViews[index].setBackgroundResource(R.drawable.die_calculated)
                diceImageViews[index].isClickable = false
            }
        }

    }

    /**
     * Updates the roll button's text and enabled state based on remaining rolls and game state.
     */
    private fun updateRollButtonText() {
        val rollsLeft = viewModel.rollCount.value ?: 3
        val isNextRound = viewModel.nextRound.value ?: false

        if (isNextRound && viewModel.selectedScoreOption.value.equals("Choose your score")) binding.rollButton.isEnabled = false

        binding.rollButton.text = when {
            isNextRound -> {getString(R.string.next_round)}
            rollsLeft == 3 -> getString(R.string.roll)
            else -> "${getString(R.string.re_roll)} ($rollsLeft left)"
        }
    }

    /**
     * Enables or disables the calculate button depending on whether the game is ready for next round
     * and valid selections have been made.
     */
    private fun updateCalculateButtonState() {
        val isScoreOptionValid = viewModel.selectedScoreOption.value != "Choose your score"
        val isAnyDiceSelected = viewModel.selectedDice.value?.isNotEmpty() ?: false
        val isNextRound = viewModel.nextRound.value ?: false

        binding.calculate.isEnabled = isNextRound && isScoreOptionValid && isAnyDiceSelected
    }

    /**
     * Resets all dice ImageViews to default clickable and unselected visual state.
     */
    private fun resetDiceUI() {
        diceImageViews.forEach {
            it.background = null
            it.isClickable = true
        }
    }
}