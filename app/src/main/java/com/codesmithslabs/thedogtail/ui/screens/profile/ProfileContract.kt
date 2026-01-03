package com.codesmithslabs.thedogtail.ui.screens.profile

interface ProfileContract {
    data class State(
        val userName: String = "Mert Kahveci",
        val userLevel: Int = 12,
        val consistency: Int = 85,
        val yearlyGridData: List<Int> = List(48) { (0..4).random() }, // Mock data
        val milestones: List<Milestone> = emptyList(),
        val insights: List<Insight> = emptyList(),
        val journalQuote: String = "I want to build these habits to prove to myself that I am capable of change, and to have more energy for my kids on the weekends.",
        val journalLastUpdated: String = "Updated 2 days ago",
        val isLoading: Boolean = false
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
