package se.umu.calu0217.thirty

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

/**
 * ViewModel for holding score data passed to the ScoreTableActivity.
 * This ViewModel does not use LiveData since the data is static and
 * does not change during the activity's lifecycle.
 */
class ScoreTableViewModel(private val state: SavedStateHandle) : ViewModel() {

    /**
     * Stores the final score and breakdown of scores in SavedStateHandle.
     *
     * @param finalScore The total score from the game.
     * @param breakdown A map of score options to achieved scores.
     */
    fun setData(finalScore: Int, breakdown: HashMap<String, Int>) {
        state["finalScore"] = finalScore
        state["scoreBreakdown"] = breakdown
    }

    /**
     * Returns the final score.
     *
     * @return The total score, or 0 if none is stored.
     */
    fun getFinalScore(): Int {
        return state.get<Int>("finalScore") ?: 0
    }

    /**
     * Returns the score breakdown.
     *
     * @return A map of score options to achieved scores, or an empty map if none is stored.
     */
    fun getScoreBreakdown(): HashMap<String, Int> {
        return state.get<HashMap<String, Int>>("scoreBreakdown") ?: hashMapOf()
    }
}
