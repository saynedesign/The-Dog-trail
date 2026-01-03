package com.codesmithslabs.thedogtail.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ProfileContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<ProfileContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        val milestones = listOf(
            ProfileContract.Milestone(
                title = "30 Day",
                subtitle = "Streak Master",
                iconType = ProfileContract.IconType.FIRE,
                color = 0xFFFFAB91 // Orange-ish
            ),
            ProfileContract.Milestone(
                title = "Hydrated",
                subtitle = "Drink 100L",
                iconType = ProfileContract.IconType.DROPLET,
                color = 0xFFCFD8DC // Gray-ish
            )
        )

        val insights = listOf(
            ProfileContract.Insight(
                title = "Water Intake",
                value = "2.1L",
                unit = "/day",
                change = "+12%",
                isPositiveChange = true,
                iconType = ProfileContract.IconType.DROPLET,
                color = 0xFF4F6DFE
            ),
            ProfileContract.Insight(
                title = "Reading",
                value = "24m",
                unit = "/day",
                change = "-5%",
                isPositiveChange = false,
                iconType = ProfileContract.IconType.BOOK,
                color = 0xFF9C27B0
            ),
            ProfileContract.Insight(
                title = "Morning Walk",
                value = "Best streak: 14 days",
                unit = "",
                change = "+3 friends joined",
                isPositiveChange = true,
                iconType = ProfileContract.IconType.SUN,
                color = 0xFFFFC107,
                isWide = true
            )
        )

        _state.update {
            it.copy(
                milestones = milestones,
                insights = insights
            )
        }
    }

    fun handleEvent(event: ProfileContract.Event) {
        when (event) {
            ProfileContract.Event.OnBackClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.NavigateBack) }
            }
            ProfileContract.Event.OnShareClicked -> {
                // TODO: Share logic
            }
            ProfileContract.Event.OnEditJournalClicked -> {
                // TODO: Edit journal
            }
            ProfileContract.Event.OnViewAllMilestonesClicked -> {
                // TODO: View all
            }
        }
    }
}
