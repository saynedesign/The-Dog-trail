package com.codesmithslabs.thedogtail.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.codesmithslabs.thedogtail.MainActivity

private val IBg = Color(0xFF4B68FF)
private val ITextPrimary = Color(0xFFFFFFFF)
private val ITextSecondary = Color(0xFFD0D8FF)

/**
 * Widget 4: Inspiration — Daily motivational quote, changes by day of year.
 */
class InspirationWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dayOfYear = java.time.LocalDate.now().dayOfYear
        val quote = QUOTES[dayOfYear % QUOTES.size]

        provideContent {
            GlanceTheme {
                InspirationContent(quote)
            }
        }
    }

    companion object {
        val QUOTES = listOf(
            Pair("The secret of getting ahead is getting started.", "Mark Twain"),
            Pair("We are what we repeatedly do. Excellence is not an act, but a habit.", "Aristotle"),
            Pair("Small daily improvements are the key to staggering long-term results.", "Robin Sharma"),
            Pair("Success is the sum of small efforts, repeated day in and day out.", "Robert Collier"),
            Pair("Motivation is what gets you started. Habit is what keeps you going.", "Jim Ryun"),
            Pair("You do not rise to the level of your goals. You fall to the level of your systems.", "James Clear"),
            Pair("The only way to do great work is to love what you do.", "Steve Jobs"),
            Pair("Don't watch the clock; do what it does. Keep going.", "Sam Levenson"),
            Pair("A journey of a thousand miles begins with a single step.", "Lao Tzu"),
            Pair("It does not matter how slowly you go as long as you do not stop.", "Confucius"),
            Pair("Discipline is choosing between what you want now and what you want most.", "Abraham Lincoln"),
            Pair("Your habits shape your identity, and your identity shapes your habits.", "James Clear"),
            Pair("Every action you take is a vote for the type of person you wish to become.", "James Clear"),
            Pair("The best time to plant a tree was 20 years ago. The second best time is now.", "Chinese Proverb"),
            Pair("Progress is not achieved by luck or accident, but by working on yourself daily.", "Epictetus"),
            Pair("What you get by achieving your goals is not as important as what you become.", "Zig Ziglar"),
            Pair("Champions keep playing until they get it right.", "Billie Jean King"),
            Pair("First forget inspiration. Habit is more dependable.", "Octavia Butler"),
            Pair("Be patient with yourself. Self-growth is tender; it's holy ground.", "Stephen Covey"),
            Pair("Believe you can and you're halfway there.", "Theodore Roosevelt"),
            Pair("Start where you are. Use what you have. Do what you can.", "Arthur Ashe"),
            Pair("Quality is not an act, it is a habit.", "Aristotle"),
            Pair("Habits are the compound interest of self-improvement.", "James Clear"),
            Pair("It's not about perfect. It's about effort.", "Jillian Michaels"),
            Pair("Fall seven times, stand up eight.", "Japanese Proverb"),
            Pair("Act as if what you do makes a difference. It does.", "William James"),
            Pair("You are never too old to set another goal or dream a new dream.", "C.S. Lewis"),
            Pair("One percent better every day. That's the real secret.", "James Clear"),
            Pair("Dream big, start small, but most of all, start.", "Simon Sinek"),
            Pair("The harder you work for something, the greater you'll feel when you achieve it.", "Anonymous"),
            Pair("Success is not final, failure is not fatal: it is the courage to continue.", "Winston Churchill")
        )
    }
}

@Composable
private fun InspirationContent(quote: Pair<String, String>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(IBg))
            .padding(20.dp)
            .clickable(actionStartActivity<MainActivity>()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "💡", style = TextStyle(fontSize = 28.sp))

        Spacer(modifier = GlanceModifier.height(12.dp))

        Text(
            text = "\"${quote.first}\"",
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = ColorProvider(ITextPrimary),
                textAlign = TextAlign.Center
            ),
            modifier = GlanceModifier.fillMaxWidth()
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        Text(
            text = "— ${quote.second}",
            style = TextStyle(
                fontSize = 12.sp,
                color = ColorProvider(ITextSecondary),
                textAlign = TextAlign.Center
            ),
            modifier = GlanceModifier.fillMaxWidth()
        )
    }
}

class InspirationWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = InspirationWidget()
}
