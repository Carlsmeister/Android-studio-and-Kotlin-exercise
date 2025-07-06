package se.umu.calu0217.thirty

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner

/**
 * Factory class for creating ViewModels with a SavedStateHandle.
 *
 * This factory is used to instantiate ViewModel instances that require access to a
 * SavedStateHandle for saving and restoring UI state across configuration changes.
 *
 * @param owner The SavedStateRegistryOwner which provides the SavedStateHandle.
 */
class ViewModelFactory(owner: SavedStateRegistryOwner)
    : AbstractSavedStateViewModelFactory(owner, null) {

    /**
     * Creates a new instance of the given ViewModel class.
     *
     * @param key A unique key associated with the ViewModel.
     * @param modelClass The class of the ViewModel to create.
     * @param handle The SavedStateHandle associated with the ViewModel.
     * @return A new instance of the specified ViewModel class.
     * @throws IllegalArgumentException if the ViewModel class is not recognized.
     */
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return when (modelClass) {
            GameViewModel::class.java -> GameViewModel(handle) as T
            HomeViewModel::class.java -> HomeViewModel(handle) as T
            ScoreTableViewModel::class.java -> ScoreTableViewModel(handle) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}