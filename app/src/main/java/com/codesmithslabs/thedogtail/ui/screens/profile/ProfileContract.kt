package com.codesmithslabs.thedogtail.ui.screens.profile

interface ProfileContract {
    data class State(
        val userName: String = "",
        val userLevel: Int = 0,
        val consistency: Int = 0,
        val yearlyGridData: List<Int> = emptyList(),
        val milestones: List<Milestone> = emptyList(),
        val insights: List<Insight> = emptyList(),
        val journalQuote: String = "",
        val journalLastUpdated: String = "",
        val isLoading: Boolean = true
    )

    data class Milestone(
        val title: String,
        val subtitle: String,
        val iconType: IconType,
        val color: Long
    )

    data class Insight(
        val title: String,
        val value: String,
        val unit: String,
        val change: String, // "+12%"
        val isPositiveChange: Boolean,
        val iconType: IconType,
        val color: Long,
        val isWide: Boolean = false // For the "Morning Walk" type card
    )

    enum class IconType {
        FIRE, DROPLET, BOOK, SUN
    }

    sealed class Event {
        data object OnBackClicked : Event()
        data object OnShareClicked : Event()
        data object OnViewAllMilestonesClicked : Event()
        data object OnEditJournalClicked : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
    }
}
