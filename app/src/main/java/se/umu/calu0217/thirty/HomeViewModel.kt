package se.umu.calu0217.thirty

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

/**
 * ViewModel for the Home screen of the Thirty game app.
 * Stores and manages the total game score and score breakdown across configuration changes.
 *
 * @property gameScore LiveData holding the final score of the last game.
 * @property scoreBreakdown LiveData containing a mapping of score options to their earned points.
 * @constructor Creates a HomeViewModel using a SavedStateHandle to persist state.
 */
class HomeViewModel (private val state : SavedStateHandle) :ViewModel(){

    val gameScore: LiveData<Int> = state.getLiveData("gameScore", 0)
    val scoreBreakdown: LiveData<HashMap<String, Int>?> = state.getLiveData("scoreBreakdown", null)

    /**
     * Updates the stored game score and score breakdown.
     *
     * @param score The final score of the completed game.
     * @param breakdown A HashMap representing the breakdown of points by score option.
     */
    fun updateGameScore(score: Int, breakdown: HashMap<String, Int>?) {
        state["gameScore"] = score
        state["scoreBreakdown"] = breakdown
    }
}