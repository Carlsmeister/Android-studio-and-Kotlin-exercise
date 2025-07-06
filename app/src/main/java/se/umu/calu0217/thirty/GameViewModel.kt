package se.umu.calu0217.thirty

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

/**
 * ViewModel for the Thirty game, responsible for managing and storing game state across configuration changes.
 * Uses SavedStateHandle to persist state and LiveData to observe changes in the UI.
 */
class GameViewModel(private val state: SavedStateHandle) : ViewModel() {

    val gameScore = state.getLiveData("gameScore", 0)
    val rounds = state.getLiveData("rounds", 1)
    val nextRound = state.getLiveData("nextRound", false)
    val reRollEnabled = state.getLiveData("reRollEnabled", false)
    val rollCount = state.getLiveData("rollCount", 3)
    val diceValues = state.getLiveData("diceValues", intArrayOf(1, 2, 3, 4, 5, 6))
    val selectedDice = state.getLiveData("selectedDice", emptySet<Int>())
    val calculatedDice = state.getLiveData("calculatedDice", emptySet<Int>())
    val selectedScoreOption = state.getLiveData("selectedScoreOption", "Choose your score")
    val spinnerOptions = state.getLiveData("spinnerOptions", listOf(
       "Choose your score", "Low", "4", "5", "6", "7", "8", "9", "10", "11", "12"
    ).toMutableList())
    val scoreByOption = mutableMapOf<String, Int>()


    /**
     * Adds the given number of points to the current game score.
     * @param option the score option associated with the points
     * @param points the number of points to add
     */
    fun addScore(option: String, points: Int) {
        scoreByOption[option] = (scoreByOption[option] ?: 0) + points
        state["gameScore"] = (state.get<Int>("gameScore") ?: 0) + points
    }

    /**
     * Increments the round counter by one.
     */
    fun incrementRound() {
        state["rounds"] = (state.get<Int>("rounds") ?: 1) + 1
    }

    /**
     * Decrements the number of remaining rolls by one.
     */
    fun decrementRollCount() {
        state["rollCount"] = (state.get<Int>("rollCount") ?: 0) - 1
    }

    /**
     * Resets the roll count to the default value (3).
     */
    fun resetRollCount() {
        state["rollCount"] = 3
    }

    /**
     * Updates the value of a specific die.
     * @param index the index of the die to update
     * @param value the new value to set
     */
    fun updateDice(index: Int, value: Int) {
        val current = state.get<IntArray>("diceValues") ?: IntArray(6)
        current[index] = value
        state["diceValues"] = current
    }

    /**
     * Sets the currently selected dice.
     * @param indices a set of dice indices to mark as selected
     */
    fun setSelectedDice(indices: Set<Int>) {
        state["selectedDice"] = indices
    }

    /**
     * Clears all selected dice.
     */
    fun clearSelectedDice() {
        state["selectedDice"] = emptySet<Int>()
    }

    /**
     * Adds dice indices to the set of already calculated dice.
     * @param indices a set of indices to add to the calculated dice set
     */
    fun addToCalculatedDice(indices: Set<Int>) {
        val current = state.get<Set<Int>>("calculatedDice")?.toMutableSet() ?: mutableSetOf()
        current.addAll(indices)
        state["calculatedDice"] = current
    }

    /**
     * Clears all calculated dice.
     */
    fun clearCalculatedDice() {
        state["calculatedDice"] = emptySet<Int>()
    }

    /**
     * Sets the currently selected score option.
     * @param option the selected score option (e.g., "Low", "7", "10", etc.)
     */
    fun setScoreOption(option: String) {
        state["selectedScoreOption"] = option
    }

    /**
     * Sets whether the game is in the "next round" state.
     * @param flag true if ready for the next round, false otherwise
     */
    fun setNextRound(flag: Boolean) {
        state["nextRound"] = flag
    }

    /**
     * Sets whether re-rolling is enabled.
     * @param flag true to enable re-rolling, false to disable
     */
    fun setReRollEnabled(flag: Boolean) {
        state["reRollEnabled"] = flag
    }

    /**
     * Removes a score option from the spinner list.
     * @param option the score option to remove
     */
    fun removeScoreOption(option: String) {
        if (option == "Choose your score") return
        val current = spinnerOptions.value?.toMutableList() ?: return
        current.remove(option)
        spinnerOptions.value = current
    }

    /**
     * Calculates the score based on the selected dice and the selected score option.
     * @return the calculated score, or 0 if the selection is invalid
     */
    fun calculateScore(): Int {
        val dice = diceValues.value ?: return 0
        val selected = selectedDice.value ?: emptySet()
        val option = selectedScoreOption.value ?: return 0
        val selectedValues = selected.map { dice[it] }

        return when (option) {
            "Low" -> if (selectedValues.all { it <= 3 }) selectedValues.sum() else 0
            else -> {
                val target = option.toIntOrNull() ?: return 0
                if (selectedValues.sum() == target) target else 0
            }
        }
    }

    /**
     * Handles the dice roll logic, including re-rolling and state updates.
     * @param selectedDice the set of dice indices to re-roll
     * @param onRoundFinished callback invoked when round ends or game ends, with the final score or null
     */
    fun rollDice(selectedDice: Set<Int>, onRoundFinished: (finalScore: Int?) -> Unit) {
        if (nextRound.value == true) {
            nextRound { score -> onRoundFinished(score) }
            return
        }

        val currentDiceValues = diceValues.value?.copyOf() ?: IntArray(6)
        val reRoll = reRollEnabled.value == true

        for (index in currentDiceValues.indices) {
            val randomValue = (1..6).random()
            if (!reRoll || selectedDice.contains(index)) {
                currentDiceValues[index] = randomValue
            }
        }

        state["diceValues"] = currentDiceValues
        decrementRollCount()
        setReRollEnabled(true)

        val rollsLeft = rollCount.value ?: 0
        if (rollsLeft <= 0) {
            setNextRound(true)
            onRoundFinished(null)
        }
    }

    /**
     * Prepares the game state for the next round and checks if the game is over.
     * @param onGameOver callback invoked with the final score if game ends, or null otherwise
     */
    fun nextRound(onGameOver: (finalScore: Int?) -> Unit) {
        val option = selectedScoreOption.value ?: ""
        if (option == "Choose your score") {
            onGameOver(null)
            return
        }

        clearSelectedDice()
        clearCalculatedDice()
        resetRollCount()
        setReRollEnabled(false)
        setNextRound(false)
        incrementRound()

        removeScoreOption(option)
        setScoreOption("Choose your score")

        val currentRound = rounds.value ?: 1
        if (currentRound > 10) {
            val finalScore = gameScore.value ?: 0
            onGameOver(finalScore)
        } else {
            onGameOver(null)
        }
    }

    /**
     * Returns the drawable resource ID for a given die value.
     * @param value the die value (1 to 6)
     * @return the drawable resource ID corresponding to the die face
     */
    fun getDieDrawable(value: Int): Int {
        return when (value) {
            1 -> R.drawable.die_1
            2 -> R.drawable.die_2
            3 -> R.drawable.die_3
            4 -> R.drawable.die_4
            5 -> R.drawable.die_5
            else -> R.drawable.die_6
        }
    }
}