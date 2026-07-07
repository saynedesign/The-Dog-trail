#include <jni.h>
#include <string>
#include <vector>
#include <unordered_map>
#include <unordered_set>
#include <algorithm>
#include <sstream>
#include <cmath>
#include <cctype>
#include <numeric>

// ======================== HELPERS ========================

std::string jstring2string(JNIEnv* env, jstring jstr) {
    if (!jstr) return "";
    const char* str = env->GetStringUTFChars(jstr, nullptr);
    if (!str) return "";
    std::string result(str);
    env->ReleaseStringUTFChars(jstr, str);
    return result;
}

std::vector<std::string> jobjectArray2vector(JNIEnv* env, jobjectArray array) {
    std::vector<std::string> result;
    if (!array) return result;
    jsize len = env->GetArrayLength(array);
    for (jsize i = 0; i < len; ++i) {
        jstring jstr = (jstring)env->GetObjectArrayElement(array, i);
        result.push_back(jstring2string(env, jstr));
        env->DeleteLocalRef(jstr);
    }
    return result;
}

std::vector<long long> jlongArray2vector(JNIEnv* env, jlongArray array) {
    std::vector<long long> result;
    if (!array) return result;
    jsize len = env->GetArrayLength(array);
    jlong* body = env->GetLongArrayElements(array, nullptr);
    if (body) {
        for (jsize i = 0; i < len; ++i) result.push_back(body[i]);
        env->ReleaseLongArrayElements(array, body, JNI_ABORT);
    }
    return result;
}

std::vector<float> jfloatArray2vector(JNIEnv* env, jfloatArray array) {
    std::vector<float> result;
    if (!array) return result;
    jsize len = env->GetArrayLength(array);
    jfloat* body = env->GetFloatArrayElements(array, nullptr);
    if (body) {
        for (jsize i = 0; i < len; ++i) result.push_back(body[i]);
        env->ReleaseFloatArrayElements(array, body, JNI_ABORT);
    }
    return result;
}

// Normalize text: lowercase, replace separators with space, strip punctuation
std::string normalize(const std::string& s) {
    std::string out;
    out.reserve(s.size());
    for (unsigned char c : s) {
        if (std::isalnum(c)) {
            out += std::tolower(c);
        } else if (std::isspace(c) || c == '-' || c == '_' || c == '/' || c == '.') {
            if (!out.empty() && out.back() != ' ') out += ' ';
        }
    }
    // trim trailing space
    while (!out.empty() && out.back() == ' ') out.pop_back();
    return out;
}

// Word-boundary-aware stem match: checks if any stem appears as a word-start substring
bool matchesStem(const std::string& text, const std::vector<std::string>& stems) {
    for (const auto& stem : stems) {
        size_t pos = 0;
        while ((pos = text.find(stem, pos)) != std::string::npos) {
            // Check word boundary: must be at start of string or preceded by space
            if (pos == 0 || text[pos - 1] == ' ') {
                return true;
            }
            pos++;
        }
    }
    return false;
}

// Substring match (no word boundary, for unit matching)
bool containsStem(const std::string& text, const std::vector<std::string>& stems) {
    for (const auto& stem : stems) {
        if (text.find(stem) != std::string::npos) return true;
    }
    return false;
}

std::string escapeJson(const std::string& input) {
    std::ostringstream ss;
    for (char c : input) {
        switch (c) {
            case '"': ss << "\\\""; break;
            case '\\': ss << "\\\\"; break;
            case '\n': ss << "\\n"; break;
            case '\r': ss << "\\r"; break;
            case '\t': ss << "\\t"; break;
            default:
                if (static_cast<unsigned char>(c) >= 32) ss << c;
                break;
        }
    }
    return ss.str();
}

// Parse comma-separated ints
std::unordered_set<int> parseIntSet(const std::string& csv) {
    std::unordered_set<int> result;
    std::stringstream ss(csv);
    std::string tok;
    while (std::getline(ss, tok, ',')) {
        try {
            if (!tok.empty()) result.insert(std::stoi(tok));
        } catch (...) {}
    }
    if (result.empty()) result = {1,2,3,4,5,6,7};
    return result;
}

// Day of week from epoch day (Mon=1 .. Sun=7, matching Java's DayOfWeek)
int dayOfWeek(long long epochDay) {
    // Java epoch day 0 = 1970-01-01 which was a Thursday (4)
    int d = static_cast<int>(((epochDay % 7) + 7) % 7); // 0=Thu, 1=Fri, ..., 6=Wed
    // Convert: Thu=4, Fri=5, Sat=6, Sun=7, Mon=1, Tue=2, Wed=3
    static const int mapping[] = {4, 5, 6, 7, 1, 2, 3};
    return mapping[d];
}

// Current year from epoch day (approximate, good enough for age calc)
int yearFromEpochDay(long long epochDay) {
    // Approximate: 365.25 days per year from 1970
    return 1970 + static_cast<int>(epochDay / 365.25);
}

// ======================== CLASSIFICATION ========================

enum HabitCategory {
    CAT_UNKNOWN = 0,
    CAT_PHYSICAL = 1,
    CAT_HYDRATION = 2,
    CAT_RECOVERY = 3,
    CAT_MENTAL = 4,
    CAT_NUTRITION = 5
};

struct CategoryDef {
    HabitCategory cat;
    std::vector<std::string> titleStems;
    std::vector<std::string> unitStems;
};

HabitCategory classifyHabit(const std::string& normTitle, const std::string& normUnit,
                            const std::vector<CategoryDef>& defs) {
    // Check in priority order. Physical first to avoid "workout" matching "work" in mental.
    for (const auto& def : defs) {
        if (matchesStem(normTitle, def.titleStems)) return def.cat;
        if (!def.unitStems.empty() && containsStem(normUnit, def.unitStems)) return def.cat;
    }
    return CAT_UNKNOWN;
}

// ======================== MAIN JNI ENTRY ========================

extern "C" JNIEXPORT jstring JNICALL
Java_com_saynedesign_habitloop_data_InsightEngine_generateInsightsJson(
    JNIEnv* env, jclass,
    jstring user_name, jstring user_dob, jfloat user_height,
    jint user_xp, jint user_level, jstring user_journal,
    jlongArray habit_ids, jobjectArray habit_titles,
    jfloatArray habit_target_values, jobjectArray habit_units,
    jobjectArray habit_types, jobjectArray habit_selected_days,
    jobjectArray habit_frequencies, jobjectArray habit_times_of_day,
    jlongArray habit_created_epoch_days,
    jlongArray log_habit_ids, jlongArray log_dates, jfloatArray log_values,
    jlong today_epoch_day_jlong
) {
    // ---- Unpack ----
    long long today_epoch_day = static_cast<long long>(today_epoch_day_jlong);
    std::string userName = jstring2string(env, user_name);
    std::string userDob = jstring2string(env, user_dob);
    std::string userJournal = jstring2string(env, user_journal);

    auto hIds = jlongArray2vector(env, habit_ids);
    auto hTitles = jobjectArray2vector(env, habit_titles);
    auto hTargetValues = jfloatArray2vector(env, habit_target_values);
    auto hUnits = jobjectArray2vector(env, habit_units);
    auto hTypes = jobjectArray2vector(env, habit_types);
    auto hSelDays = jobjectArray2vector(env, habit_selected_days);
    auto hFreqs = jobjectArray2vector(env, habit_frequencies);
    auto hTimes = jobjectArray2vector(env, habit_times_of_day);
    auto hCreatedDays = jlongArray2vector(env, habit_created_epoch_days);

    auto lHabitIds = jlongArray2vector(env, log_habit_ids);
    auto lDates = jlongArray2vector(env, log_dates);
    auto lValues = jfloatArray2vector(env, log_values);

    int N = hIds.size();
    int L = lDates.size();

    // ---- Category definitions (physical checked first to avoid "work" in "workout" matching mental) ----
    std::vector<CategoryDef> catDefs = {
        {CAT_PHYSICAL, {"run", "runn", "jog", "jogg", "walk", "gym", "workou", "cardi", "cardio",
                        "swim", "swimm", "lift", "stretc", "yoga", "sport", "pushup", "pull up",
                        "pullup", "push up", "situp", "sit up", "crunch", "exercis", "train",
                        "cycle", "cycl", "bik", "danc", "pilates", "squat", "hike", "hik",
                        "plank", "active", "muscl", "calisthenic", "crossfit", "kickbox",
                        "martial", "climb", "rowing", "skip", "jump", "sprint", "deadlift",
                        "bench", "curl", "lunge", "burpee", "abs", "leg day", "arm day",
                        "chest day", "back day", "shoulder"},
         {"steps", "km", "miles", "reps", "meter", "metres", "laps", "rounds"}},

        {CAT_HYDRATION, {"water", "hydrat", "h2o", "fluid"},
         {"ml", "liter", "litre", "oz", "glass", "glasses", "cup", "cups", "bottle"}},

        {CAT_NUTRITION, {"eat", "food", "meal", "veggie", "vegetabl", "fruit", "diet", "calori",
                         "protein", "breakfas", "lunch", "dinner", "salad", "snack", "cook",
                         "nutriti", "vitamin", "supplement", "fast", "keto", "vegan"},
         {}},

        {CAT_RECOVERY, {"sleep", "bed", "rest", "nap", "chill", "relax", "recover", "wind down",
                        "cool down", "massage", "ice bath", "sauna", "meditat"},
         {"hours", "hrs"}},

        {CAT_MENTAL, {"read", "study", "code", "codin", "learn", "journal", "focus", "book",
                      "writ", "paint", "draw", "practic", "skill", "languag", "class",
                      "mindful", "silent", "gratitud", "pray", "reflect", "think", "review",
                      "plan", "organiz", "tidy", "clean", "declutt", "creative"},
         {}}
    };

    // ---- Build indexes ----
    // habitId -> vector of log indices
    std::unordered_map<long long, std::vector<int>> habitLogIdx;
    for (int i = 0; i < L; ++i) {
        habitLogIdx[lHabitIds[i]].push_back(i);
    }

    // Pre-parse scheduled days for each habit
    std::vector<std::unordered_set<int>> schedDays(N);
    for (int i = 0; i < N; ++i) {
        schedDays[i] = parseIntSet(hSelDays[i]);
    }

    // ---- Per-habit analysis ----
    struct HabitAnalysis {
        long long id;
        long long createdDay;     // epoch day the habit was created
        std::string title;
        HabitCategory category;
        std::string timeOfDay;
        int currentStreak = 0;
        int longestStreak = 0;
        float consistency7d = 0;
        float consistency30d = 0;
        float consistencyAllTime = 0;
        int totalCompletions = 0;
        int scheduledLast30 = 0;
        int completedLast30 = 0;
        int habitAgeDays = 0;
        float avgValue = 0;       // for numeric habits
        float valueTrend = 0;     // positive = improving
        std::string grade;
        std::string trend;        // "improving", "stable", "declining"
        int historicalAvgStreakLen = 0;
    };

    std::vector<HabitAnalysis> analyses(N);
    int catCounts[6] = {0};

    for (int i = 0; i < N; ++i) {
        auto& a = analyses[i];
        a.id = hIds[i];
        a.createdDay = (i < (int)hCreatedDays.size()) ? hCreatedDays[i] : today_epoch_day;
        a.title = hTitles[i];
        a.timeOfDay = hTimes[i];

        std::string normTitle = normalize(hTitles[i]);
        std::string normUnit = normalize(hUnits[i]);
        a.category = classifyHabit(normTitle, normUnit, catDefs);
        catCounts[a.category]++;

        // Collect this habit's log dates and values
        auto it = habitLogIdx.find(a.id);
        std::unordered_set<long long> logDatesSet;
        std::unordered_map<long long, float> logValMap;
        if (it != habitLogIdx.end()) {
            for (int idx : it->second) {
                logDatesSet.insert(lDates[idx]);
                logValMap[lDates[idx]] = lValues[idx];
            }
        }
        a.totalCompletions = logDatesSet.size();

        const auto& sched = schedDays[i];

        // ---- Schedule-aware streak (clamped to creation date) ----
        {
            int streak = 0;
            long long check = today_epoch_day;
            // If not completed today and today is scheduled, start from yesterday
            if (!logDatesSet.count(today_epoch_day) && sched.count(dayOfWeek(today_epoch_day))) {
                check = today_epoch_day - 1;
            }
            for (long long d = check; d >= a.createdDay; --d) {
                int dow = dayOfWeek(d);
                if (logDatesSet.count(d)) {
                    streak++;
                } else if (!sched.count(dow)) {
                    // non-scheduled day, skip
                } else {
                    break; // missed scheduled day
                }
                if (streak > 3650) break;
            }
            a.currentStreak = streak;
        }

        // ---- Longest streak + historical average streak length (from creation date) ----
        {
            int longest = 0, rolling = 0;
            std::vector<int> allStreaks;
            long long startDay = a.createdDay; // start from when habit was created
            for (long long d = startDay; d <= today_epoch_day; ++d) {
                int dow = dayOfWeek(d);
                if (logDatesSet.count(d)) {
                    rolling++;
                } else if (!sched.count(dow)) {
                    // non-scheduled, streak continues
                } else {
                    if (rolling > 0) allStreaks.push_back(rolling);
                    rolling = 0;
                }
                longest = std::max(longest, rolling);
            }
            if (rolling > 0) allStreaks.push_back(rolling);
            a.longestStreak = longest;
            if (!allStreaks.empty()) {
                int sum = 0;
                for (int s : allStreaks) sum += s;
                a.historicalAvgStreakLen = sum / allStreaks.size();
            }
        }

        // ---- Consistency windows (clamped to creation date) ----
        // Only count days AFTER the habit was created
        auto calcConsistency = [&](int windowDays) -> std::pair<float, std::pair<int,int>> {
            int scheduled = 0, completed = 0;
            long long windowStart = today_epoch_day - windowDays + 1;
            // CRITICAL: never analyze days before the habit existed
            long long effectiveStart = std::max(windowStart, a.createdDay);
            for (long long d = effectiveStart; d <= today_epoch_day; ++d) {
                if (sched.count(dayOfWeek(d))) {
                    scheduled++;
                    if (logDatesSet.count(d)) completed++;
                }
            }
            float rate = (scheduled > 0) ? static_cast<float>(completed) / scheduled : 0;
            return {rate, {scheduled, completed}};
        };

        auto [c7, p7] = calcConsistency(7);
        auto [c30, p30] = calcConsistency(30);
        int allTimeDays = static_cast<int>(today_epoch_day - a.createdDay + 1);
        auto [cAll, pAll] = calcConsistency(allTimeDays);
        a.consistency7d = c7;
        a.consistency30d = c30;
        a.consistencyAllTime = cAll;
        a.scheduledLast30 = p30.first;
        a.completedLast30 = p30.second;

        // ---- Habit age (from actual creation date) ----
        a.habitAgeDays = static_cast<int>(today_epoch_day - a.createdDay);

        // ---- Grade ----
        // For very new habits (< 3 days), use only 7d consistency
        float weightedScore;
        if (a.habitAgeDays < 3) {
            weightedScore = a.consistency7d;
        } else if (a.habitAgeDays < 14) {
            weightedScore = a.consistency7d * 0.7f + a.consistencyAllTime * 0.3f;
        } else {
            weightedScore = a.consistency7d * 0.4f + a.consistency30d * 0.4f + a.consistencyAllTime * 0.2f;
        }
        if (weightedScore >= 0.95f) a.grade = "A+";
        else if (weightedScore >= 0.85f) a.grade = "A";
        else if (weightedScore >= 0.75f) a.grade = "B+";
        else if (weightedScore >= 0.65f) a.grade = "B";
        else if (weightedScore >= 0.50f) a.grade = "C";
        else if (weightedScore >= 0.35f) a.grade = "D";
        else a.grade = "F";

        // ---- Trend ----
        // Need at least 7 days of data to detect a trend, otherwise "new"
        if (a.habitAgeDays < 7) {
            a.trend = "new";
        } else {
            float diff = a.consistency7d - a.consistency30d;
            if (diff > 0.10f) a.trend = "improving";
            else if (diff < -0.10f) a.trend = "declining";
            else a.trend = "stable";
        }

        // ---- Numeric value trend (for NUMERIC / TIMER habits, only if enough data) ----
        if (hTypes[i] == "NUMERIC" || hTypes[i] == "TIMER") {
            // Compare average of last 7 days vs prior 7 days, only within habit's lifetime
            float sum7 = 0, cnt7 = 0, sumPrior = 0, cntPrior = 0;
            for (long long d = std::max(today_epoch_day - 6, a.createdDay); d <= today_epoch_day; ++d) {
                auto vit = logValMap.find(d);
                if (vit != logValMap.end()) { sum7 += vit->second; cnt7++; }
            }
            for (long long d = std::max(today_epoch_day - 13, a.createdDay); d <= today_epoch_day - 7; ++d) {
                auto vit = logValMap.find(d);
                if (vit != logValMap.end()) { sumPrior += vit->second; cntPrior++; }
            }
            float avg7 = (cnt7 > 0) ? sum7 / cnt7 : 0;
            float avgPrior = (cntPrior > 0) ? sumPrior / cntPrior : 0;
            a.avgValue = avg7;
            a.valueTrend = (cntPrior >= 2 && cnt7 >= 2) ? avg7 - avgPrior : 0;
        }
    }

    // ---- Collect insights, highlights, advices ----
    std::vector<std::string> highlights;
    std::vector<std::string> insights;
    std::vector<std::string> advices;

    // ================================================================
    // MODULE 1: Best & Worst Habit (only habits with 5+ scheduled days since creation)
    // ================================================================
    if (N >= 2) {
        int bestIdx = -1, worstIdx = -1;
        float bestScore = -1, worstScore = 2;
        for (int i = 0; i < N; ++i) {
            // Require at least 5 scheduled days within the habit's lifetime
            if (analyses[i].scheduledLast30 < 5) continue;
            if (analyses[i].habitAgeDays < 3) continue; // too new to rank
            float score = analyses[i].consistency30d;
            if (score > bestScore) { bestScore = score; bestIdx = i; }
            if (score < worstScore) { worstScore = score; worstIdx = i; }
        }
        if (bestIdx >= 0 && worstIdx >= 0 && bestIdx != worstIdx) {
            highlights.push_back("Your strongest habit is '" + analyses[bestIdx].title + "' at " +
                                 std::to_string(static_cast<int>(bestScore * 100)) + "% consistency. Keep it up!");
            if (worstScore < 0.5f) {
                insights.push_back("'" + analyses[worstIdx].title + "' needs attention — only " +
                                   std::to_string(static_cast<int>(worstScore * 100)) + "% consistency.");
            }
        }
    }

    // ================================================================
    // MODULE 2: Best & Worst Day of Week (only counts days where habit existed)
    // ================================================================
    {
        int dayCompleted[8] = {0};
        int dayScheduled[8] = {0};
        for (long long d = today_epoch_day - 29; d <= today_epoch_day; ++d) {
            int dow = dayOfWeek(d);
            for (int i = 0; i < N; ++i) {
                // CRITICAL: skip this habit for days before it was created
                if (d < analyses[i].createdDay) continue;
                if (schedDays[i].count(dow)) {
                    dayScheduled[dow]++;
                    auto it = habitLogIdx.find(hIds[i]);
                    if (it != habitLogIdx.end()) {
                        for (int idx : it->second) {
                            if (lDates[idx] == d) { dayCompleted[dow]++; break; }
                        }
                    }
                }
            }
        }
        static const char* dayNames[] = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        int bestDay = 1, worstDay = 1;
        float bestRate = -1, worstRate = 2;
        for (int d = 1; d <= 7; ++d) {
            if (dayScheduled[d] < 4) continue; // need at least 4 data points
            float rate = static_cast<float>(dayCompleted[d]) / dayScheduled[d];
            if (rate > bestRate) { bestRate = rate; bestDay = d; }
            if (rate < worstRate) { worstRate = rate; worstDay = d; }
        }
        if (bestRate >= 0 && bestDay != worstDay) {
            highlights.push_back("Your best day is " + std::string(dayNames[bestDay]) + " (" +
                                 std::to_string(static_cast<int>(bestRate * 100)) + "% completion).");
            if (worstRate < 0.5f) {
                insights.push_back("Your toughest day is " + std::string(dayNames[worstDay]) + " at only " +
                                   std::to_string(static_cast<int>(worstRate * 100)) + "%. Consider lighter goals on that day.");
            }
        }
    }

    // ================================================================
    // MODULE 3: Streak Milestones & Danger Prediction
    // ================================================================
    for (int i = 0; i < N; ++i) {
        auto& a = analyses[i];
        if (a.currentStreak >= 7) {
            highlights.push_back("'" + a.title + "' is on a " + std::to_string(a.currentStreak) +
                                 "-day streak! (Longest ever: " + std::to_string(a.longestStreak) + " days)");
        }
        // Streak danger: current streak is approaching historical average break point
        if (a.historicalAvgStreakLen > 3 && a.currentStreak > 0) {
            float ratio = static_cast<float>(a.currentStreak) / a.historicalAvgStreakLen;
            if (ratio >= 0.8f && ratio < 1.3f && a.currentStreak < a.longestStreak) {
                insights.push_back("Streak danger: You historically break '" + a.title + "' streaks around day " +
                                   std::to_string(a.historicalAvgStreakLen) + ". You're on day " +
                                   std::to_string(a.currentStreak) + " — stay focused!");
            }
        }
    }

    // ================================================================
    // MODULE 4: Fatigue / Burnout Detection (needs 14+ days of data)
    // ================================================================
    for (int i = 0; i < N; ++i) {
        auto& a = analyses[i];
        // Only flag burnout if habit is old enough to have meaningful 30d vs 7d comparison
        if (a.habitAgeDays >= 14 && a.scheduledLast30 >= 10 && (a.consistency30d - a.consistency7d) >= 0.30f) {
            insights.push_back("Burnout risk: '" + a.title + "' dropped from " +
                               std::to_string(static_cast<int>(a.consistency30d * 100)) + "% to " +
                               std::to_string(static_cast<int>(a.consistency7d * 100)) + "% this week.");
        }
    }

    // ================================================================
    // MODULE 5: Habit Maturity Tracker (66-day rule)
    // ================================================================
    for (int i = 0; i < N; ++i) {
        auto& a = analyses[i];
        if (a.habitAgeDays > 0 && a.habitAgeDays < 66 && a.consistency30d >= 0.6f) {
            int pct = static_cast<int>((a.habitAgeDays / 66.0f) * 100);
            highlights.push_back("'" + a.title + "' is " + std::to_string(pct) +
                                 "% of the way to becoming automatic (day " + std::to_string(a.habitAgeDays) + " of 66).");
        } else if (a.habitAgeDays >= 66 && a.consistency30d >= 0.7f) {
            highlights.push_back("'" + a.title + "' has been active for " + std::to_string(a.habitAgeDays) +
                                 " days — it's likely an automatic part of your routine now!");
        }
    }

    // ================================================================
    // MODULE 6: Numeric Value Trends
    // ================================================================
    for (int i = 0; i < N; ++i) {
        auto& a = analyses[i];
        if ((hTypes[i] == "NUMERIC" || hTypes[i] == "TIMER") && std::abs(a.valueTrend) > 0.01f) {
            std::string unit = hUnits[i];
            if (a.valueTrend > 0) {
                highlights.push_back("'" + a.title + "' average increased by " +
                                     std::to_string(static_cast<int>(a.valueTrend * 10) / 10.0f).substr(0,4) + " " + unit + " this week!");
            } else {
                insights.push_back("'" + a.title + "' average decreased by " +
                                   std::to_string(static_cast<int>(std::abs(a.valueTrend) * 10) / 10.0f).substr(0,4) + " " + unit + " this week.");
            }
        }
    }

    // ================================================================
    // MODULE 7: Daily Overload Detection
    // ================================================================
    {
        for (int d = 1; d <= 7; ++d) {
            int count = 0;
            for (int i = 0; i < N; ++i) {
                if (schedDays[i].count(d)) count++;
            }
            static const char* dayNames[] = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            if (count >= 7) {
                advices.push_back("Overload: You have " + std::to_string(count) + " habits scheduled on " +
                                  std::string(dayNames[d]) + ". Research suggests 3-5 is optimal for sustainable consistency.");
            }
        }
    }

    // ================================================================
    // MODULE 8: Co-occurrence Correlation (only counts days where BOTH habits existed)
    // ================================================================
    if (N >= 2) {
        int pairsChecked = 0;
        int maxPairs = std::min(N * (N - 1) / 2, 105);
        for (int i = 0; i < N && pairsChecked < maxPairs; ++i) {
            if (analyses[i].habitAgeDays < 5) continue; // too new
            for (int j = i + 1; j < N && pairsChecked < maxPairs; ++j) {
                if (analyses[j].habitAgeDays < 5) continue; // too new
                pairsChecked++;
                auto itA = habitLogIdx.find(hIds[i]);
                auto itB = habitLogIdx.find(hIds[j]);
                std::unordered_set<long long> logsA, logsB;
                if (itA != habitLogIdx.end()) for (int idx : itA->second) logsA.insert(lDates[idx]);
                if (itB != habitLogIdx.end()) for (int idx : itB->second) logsB.insert(lDates[idx]);

                // Only count days where BOTH habits existed
                long long pairStart = std::max(analyses[i].createdDay, analyses[j].createdDay);
                long long windowStart = std::max(today_epoch_day - 29, pairStart);

                int both = 0, aOnly = 0, bOnly = 0;
                for (long long d = windowStart; d <= today_epoch_day; ++d) {
                    bool hA = logsA.count(d), hB = logsB.count(d);
                    if (hA && hB) both++;
                    else if (hA) aOnly++;
                    else if (hB) bOnly++;
                }
                if (both >= 4) {
                    if (both + bOnly >= 5) {
                        float p = static_cast<float>(both) / (both + bOnly);
                        if (p >= 0.80f) {
                            insights.push_back("Correlation: When you complete '" + hTitles[j] + "', there's a " +
                                               std::to_string(static_cast<int>(p * 100)) + "% chance you also complete '" + hTitles[i] + "'.");
                        }
                    }
                    if (both + aOnly >= 5) {
                        float p = static_cast<float>(both) / (both + aOnly);
                        if (p >= 0.80f) {
                            insights.push_back("Correlation: When you complete '" + hTitles[i] + "', there's a " +
                                               std::to_string(static_cast<int>(p * 100)) + "% chance you also complete '" + hTitles[j] + "'.");
                        }
                    }
                }
            }
        }
    }

    // ================================================================
    // MODULE 9: Cross-Day Causality (only for habits 7+ days old, clamped to both creation dates)
    // ================================================================
    if (N >= 2) {
        for (int i = 0; i < N && i < 10; ++i) {
            if (analyses[i].habitAgeDays < 7) continue;
            for (int j = 0; j < N && j < 10; ++j) {
                if (i == j) continue;
                if (analyses[j].habitAgeDays < 7) continue;
                auto itA = habitLogIdx.find(hIds[i]);
                auto itB = habitLogIdx.find(hIds[j]);
                std::unordered_set<long long> logsA, logsB;
                if (itA != habitLogIdx.end()) for (int idx : itA->second) logsA.insert(lDates[idx]);
                if (itB != habitLogIdx.end()) for (int idx : itB->second) logsB.insert(lDates[idx]);

                long long pairStart = std::max(analyses[i].createdDay, analyses[j].createdDay);
                long long windowStart = std::max(today_epoch_day - 28, pairStart);

                int skipA_then_skipB = 0, skipA_total = 0;
                for (long long d = windowStart; d < today_epoch_day; ++d) {
                    if (schedDays[i].count(dayOfWeek(d)) && !logsA.count(d)) {
                        skipA_total++;
                        long long next = d + 1;
                        if (next >= analyses[j].createdDay && schedDays[j].count(dayOfWeek(next)) && !logsB.count(next)) {
                            skipA_then_skipB++;
                        }
                    }
                }
                if (skipA_total >= 4 && skipA_then_skipB >= 3) {
                    float p = static_cast<float>(skipA_then_skipB) / skipA_total;
                    if (p >= 0.70f) {
                        insights.push_back("Causal pattern: Skipping '" + hTitles[i] + "' makes you " +
                                           std::to_string(static_cast<int>(p * 100)) + "% likely to skip '" + hTitles[j] + "' the next day.");
                    }
                }
            }
        }
    }

    // ================================================================
    // MODULE 10: Weekend vs Weekday Dip (only counts days where each habit existed)
    // ================================================================
    {
        int wdSched = 0, wdComp = 0, weSched = 0, weComp = 0;
        for (long long d = today_epoch_day - 29; d <= today_epoch_day; ++d) {
            int dow = dayOfWeek(d);
            bool isWe = (dow == 6 || dow == 7);
            for (int i = 0; i < N; ++i) {
                // CRITICAL: skip days before this habit was created
                if (d < analyses[i].createdDay) continue;
                if (!schedDays[i].count(dow)) continue;
                auto it = habitLogIdx.find(hIds[i]);
                bool completed = false;
                if (it != habitLogIdx.end()) {
                    for (int idx : it->second) {
                        if (lDates[idx] == d) { completed = true; break; }
                    }
                }
                if (isWe) { weSched++; if (completed) weComp++; }
                else { wdSched++; if (completed) wdComp++; }
            }
        }
        float wdRate = (wdSched > 0) ? static_cast<float>(wdComp) / wdSched : 0;
        float weRate = (weSched > 0) ? static_cast<float>(weComp) / weSched : 0;
        if (weSched >= 5 && (wdRate - weRate) >= 0.20f) {
            insights.push_back("Weekend dip: Weekday consistency is " + std::to_string(static_cast<int>(wdRate * 100)) +
                               "% but drops to " + std::to_string(static_cast<int>(weRate * 100)) + "% on weekends.");
        }
    }

    // ================================================================
    // MODULE 11: Time-of-Day Effectiveness
    // ================================================================
    {
        std::unordered_map<std::string, std::pair<int,int>> todStats; // {scheduled, completed}
        for (int i = 0; i < N; ++i) {
            std::string tod = hTimes[i];
            if (tod.empty() || tod == "Anytime") continue;
            auto& st = todStats[tod];
            st.first += analyses[i].scheduledLast30;
            st.second += analyses[i].completedLast30;
        }
        std::string bestTod, worstTod;
        float bestTodRate = -1, worstTodRate = 2;
        for (auto& [tod, stats] : todStats) {
            if (stats.first < 5) continue;
            float rate = static_cast<float>(stats.second) / stats.first;
            if (rate > bestTodRate) { bestTodRate = rate; bestTod = tod; }
            if (rate < worstTodRate) { worstTodRate = rate; worstTod = tod; }
        }
        if (!bestTod.empty() && !worstTod.empty() && bestTod != worstTod && (bestTodRate - worstTodRate) >= 0.15f) {
            advices.push_back("Your " + bestTod + " habits have " + std::to_string(static_cast<int>(bestTodRate * 100)) +
                              "% consistency vs " + worstTod + " at " + std::to_string(static_cast<int>(worstTodRate * 100)) +
                              "%. Consider moving struggling habits to " + bestTod + ".");
        }
    }

    // ================================================================
    // MODULE 12: Comeback / Resilience Detection (clamped to creation date)
    // ================================================================
    for (int i = 0; i < N; ++i) {
        if (analyses[i].habitAgeDays < 7) continue; // need at least a week of data
        auto it = habitLogIdx.find(hIds[i]);
        if (it == habitLogIdx.end()) continue;
        std::unordered_set<long long> logSet;
        for (int idx : it->second) logSet.insert(lDates[idx]);

        // Look for streak breaks followed by quick restarts, only within habit's lifetime
        bool inStreak = false;
        int comebacks = 0;
        long long scanStart = std::max(today_epoch_day - 29, analyses[i].createdDay);
        for (long long d = scanStart; d <= today_epoch_day; ++d) {
            int dow = dayOfWeek(d);
            if (!schedDays[i].count(dow)) continue;
            if (logSet.count(d)) {
                if (!inStreak) comebacks++;
                inStreak = true;
            } else {
                inStreak = false;
            }
        }
        if (comebacks >= 3 && analyses[i].consistency30d >= 0.4f) {
            highlights.push_back("Resilience: You restarted '" + analyses[i].title + "' " +
                                 std::to_string(comebacks) + " times. That determination matters more than perfection!");
        }
    }

    // ================================================================
    // Perfect week celebration (only for habits that existed during the full week)
    // ================================================================
    {
        bool perfectWeek = true;
        bool anyHabitQualifies = false;
        for (int i = 0; i < N; ++i) {
            // Only check habits that existed for the entire 7-day window
            if (analyses[i].createdDay > today_epoch_day - 6) continue;
            // Check if this habit had any scheduled days in last 7
            int sched7 = 0;
            long long weekStart = today_epoch_day - 6;
            for (long long d = weekStart; d <= today_epoch_day; ++d) {
                if (schedDays[i].count(dayOfWeek(d))) sched7++;
            }
            if (sched7 > 0) {
                anyHabitQualifies = true;
                if (analyses[i].consistency7d < 1.0f) {
                    perfectWeek = false;
                    break;
                }
            }
        }
        if (perfectWeek && anyHabitQualifies) {
            highlights.insert(highlights.begin(), "Perfect week! You completed every scheduled habit for 7 straight days!");
        }
    }

    // ================================================================
    // Profile-based advice
    // ================================================================
    {
        // Age from DOB
        size_t lastSlash = userDob.find_last_of('/');
        int age = -1;
        if (lastSlash != std::string::npos && lastSlash + 1 < userDob.length()) {
            try {
                int birthYear = std::stoi(userDob.substr(lastSlash + 1));
                int currentYear = yearFromEpochDay(today_epoch_day);
                age = currentYear - birthYear;
            } catch (...) {}
        }

        if (age > 0 && age < 18) {
            advices.push_back("Youth health: Prioritize 8-10 hours of sleep and daily physical activity for optimal development.");
        } else if (age > 50) {
            advices.push_back("Active aging: Balance cardio with flexibility. Consider adding stretching or yoga habits.");
        }

        if (user_height > 100.0f && catCounts[CAT_PHYSICAL] > 0 && catCounts[CAT_HYDRATION] == 0) {
            advices.push_back("Hydration gap: You have physical habits but no hydration tracking. Add a water intake habit.");
        }
        if (catCounts[CAT_PHYSICAL] == 0) {
            advices.push_back("Movement reminder: No fitness habits detected. Even a 10-minute daily walk makes a measurable difference.");
        }
        if (catCounts[CAT_RECOVERY] == 0 && catCounts[CAT_PHYSICAL] >= 2) {
            advices.push_back("Recovery balance: You track " + std::to_string(catCounts[CAT_PHYSICAL]) +
                              " physical habits but no recovery. Add a sleep or stretching habit to prevent burnout.");
        }
    }

    // ================================================================
    // Journal sentiment analysis
    // ================================================================
    if (!userJournal.empty()) {
        std::string j = normalize(userJournal);
        if (matchesStem(j, {"tired", "exhaust", "sleepy", "sore", "pain", "fatigu", "drained", "worn out", "burnout", "burnt out"})) {
            advices.push_back("Journal signal: Physical tiredness detected. Consider reducing intensity or adding a rest day.");
        }
        if (matchesStem(j, {"stress", "anxi", "overwhelm", "panic", "nervous", "worry", "tense"})) {
            advices.push_back("Wellbeing: Stress signals in your journal. A 5-minute breathing or meditation habit can help significantly.");
        }
        if (matchesStem(j, {"sick", "fever", "cough", "cold", "headach", "flu", "unwell", "nausea"})) {
            advices.push_back("Recovery priority: Health issues detected. Use rest days to protect your streaks while you recover.");
        }
        if (matchesStem(j, {"happy", "great", "amazing", "proud", "accomplish", "energiz", "motivat", "excit"})) {
            highlights.push_back("Your journal radiates positive energy! That mindset is fuel for long-term consistency.");
        }
    }

    // ================================================================
    // XP Level Projection
    // ================================================================
    int projectionDays = -1;
    {
        int nextLevelReqs[] = {0, 100, 500, 2000, 5000, 10000, 50000, 100000, 500000, 1000000};
        int nextReq = (user_level >= 1 && user_level <= 9) ? nextLevelReqs[user_level] : 10000000;
        int xpNeeded = nextReq - user_xp;

        // Count completions in last 7 days
        int recent = 0;
        for (int i = 0; i < L; ++i) {
            if (lDates[i] >= today_epoch_day - 6) recent++;
        }
        float dailyXp = (recent * 10.0f + (recent > 0 ? 21.0f : 0)) / 7.0f; // 10 per completion + ~3/day bonuses
        if (xpNeeded > 0 && dailyXp > 0.5f) {
            projectionDays = static_cast<int>(std::ceil(xpNeeded / dailyXp));
        }
    }

    // ================================================================
    // BUILD JSON
    // ================================================================
    std::ostringstream json;
    json << "{\n";

    // highlights
    json << "  \"highlights\": [\n";
    for (size_t i = 0; i < highlights.size(); ++i) {
        json << "    \"" << escapeJson(highlights[i]) << "\"";
        if (i + 1 < highlights.size()) json << ",";
        json << "\n";
    }
    json << "  ],\n";

    // insights
    json << "  \"insights\": [\n";
    for (size_t i = 0; i < insights.size(); ++i) {
        json << "    \"" << escapeJson(insights[i]) << "\"";
        if (i + 1 < insights.size()) json << ",";
        json << "\n";
    }
    json << "  ],\n";

    // advices
    json << "  \"advices\": [\n";
    for (size_t i = 0; i < advices.size(); ++i) {
        json << "    \"" << escapeJson(advices[i]) << "\"";
        if (i + 1 < advices.size()) json << ",";
        json << "\n";
    }
    json << "  ],\n";

    // habitScores
    json << "  \"habitScores\": [\n";
    for (int i = 0; i < N; ++i) {
        auto& a = analyses[i];
        json << "    {\"habitId\":" << a.id
             << ",\"title\":\"" << escapeJson(a.title) << "\""
             << ",\"grade\":\"" << a.grade << "\""
             << ",\"consistency7d\":" << static_cast<int>(a.consistency7d * 100)
             << ",\"consistency30d\":" << static_cast<int>(a.consistency30d * 100)
             << ",\"currentStreak\":" << a.currentStreak
             << ",\"longestStreak\":" << a.longestStreak
             << ",\"trend\":\"" << a.trend << "\""
             << ",\"ageDays\":" << a.habitAgeDays
             << "}";
        if (i + 1 < N) json << ",";
        json << "\n";
    }
    json << "  ],\n";

    // levelProjectionDays
    json << "  \"levelProjectionDays\": " << projectionDays << "\n";
    json << "}";

    return env->NewStringUTF(json.str().c_str());
}
