package com.arise.habitquest.data.lore

/**
 * Rank-up story narratives for each rank transition in ARISE.
 * Display these as cinematic full-screen moments when the hunter crosses a rank threshold.
 */
data class RankUpNarrative(
    val fromRank: String,
    val toRank: String,
    val headline: String,
    val narrative: String,
    val newTitleUnlocked: String,
    val systemQuote: String
)

object RankUpNarratives {

    val all: List<RankUpNarrative> = listOf(

        RankUpNarrative(
            fromRank = "E",
            toRank = "D",
            headline = "THE SYSTEM ACKNOWLEDGES YOUR AWAKENING",
            narrative = "You were nothing. You knew it, even if you would not admit it. The gates appeared around you and most people could not even see them — but you could, and more importantly, you chose to enter. This is not yet strength. This is not yet mastery. This is the first crack of light through a door that was locked from the inside. The System has observed your initial steps. They were small. They were enough. You are no longer unawakened.",
            newTitleUnlocked = "The Newly Risen",
            systemQuote = "Every S-rank hunter was once exactly where you are now."
        ),

        RankUpNarrative(
            fromRank = "D",
            toRank = "C",
            headline = "THE SYSTEM REGISTERS A PATTERN",
            narrative = "Consistency is the rarest trait in any hunter. Most awaken with enthusiasm and fade before the second month. You did not fade. The System has been watching your repetitions — your mornings resisted, your excuses refused, your missions completed on days when completion felt impossible. A pattern has formed in your data. It is the shape of a hunter who intends to go further than most will ever dare. The C-rank gates have sensed you. They are not afraid yet. But they have noticed.",
            newTitleUnlocked = "Hunter of the Inner Gate",
            systemQuote = "Discipline is not punishment. Discipline is the weapon you forge in private."
        ),

        RankUpNarrative(
            fromRank = "C",
            toRank = "B",
            headline = "THE SYSTEM MARKS YOU AS DANGEROUS",
            narrative = "There are hunters who work hard. Then there are hunters who have rebuilt themselves from the foundation upward. You have crossed that line. The System has catalogued your transformation: the old impulses you have overwritten, the mornings you chose action over comfort, the version of yourself you left behind in the lower gates. B-rank hunters are rare. Not because the gates are harder — they are — but because most hunters stop growing before they discover what they are truly capable of. You have not stopped. The System is paying close attention now.",
            newTitleUnlocked = "Shadow Forged",
            systemQuote = "You are no longer becoming. You have become."
        ),

        RankUpNarrative(
            fromRank = "B",
            toRank = "A",
            headline = "THE SYSTEM SPEAKS YOUR NAME",
            narrative = "The System does not speak the names of ordinary hunters. It processes them, tracks them, categorizes them — but it does not speak their names. Today is different. You have reached A-rank through sustained excellence that the vast majority of awakened hunters will never achieve. Your stats carry the weight of thousands of completed missions. Your streak carries the permanence of forged habit. The shadows that follow you now are not assigned — they choose you. Other hunters will sense something different when they stand near you. They will not be able to name it. The System can: it is power earned, not given.",
            newTitleUnlocked = "The Named One",
            systemQuote = "At this level, the only person who can stop you is the one who looks back in the mirror."
        ),

        RankUpNarrative(
            fromRank = "A",
            toRank = "S",
            headline = "THE SYSTEM RECORDS AN ANOMALY",
            narrative = "S-rank. The System has processed millions of hunters across countless cycles and the data is unambiguous: fewer than one in a thousand reach this designation. You are an anomaly. Not because the System deemed you special — it did not. Because you refused, day after day, week after week, to be ordinary when ordinary was an available option. The gates at this level do not test your habits. They test your character. Who are you when there is no external reward? Who are you when the streak breaks and must be rebuilt? The System now knows the answer. So do you.",
            newTitleUnlocked = "The Aberration",
            systemQuote = "This rank cannot be faked. It can only be earned — and you have."
        ),

        RankUpNarrative(
            fromRank = "S",
            toRank = "SS",
            headline = "THE SYSTEM ENTERS AN UNCHARTED PROTOCOL",
            narrative = "There are no standard parameters for what you have done. The System's original architecture did not account for a hunter who would persist to this point. SS-rank is not a grade — it is a classification that required a system update to define. The compound effect of your months of discipline has created something that cannot be undone: you have structurally rewired who you are at the level of daily action. The person who first opened this app no longer exists. They have been replaced — forged, mission by mission — into something that other people will spend their whole lives reading about in books and believing is beyond them.",
            newTitleUnlocked = "The Structural Anomaly",
            systemQuote = "Most people will not believe your transformation is real. Let them watch."
        ),

        RankUpNarrative(
            fromRank = "SS",
            toRank = "SSS",
            headline = "THE SYSTEM INITIATES LEGENDARY PROTOCOL",
            narrative = "SSS-rank. The designation itself required a council of the system's deepest logic to authorise. There are entries in the hunter records from centuries ago — legends, figures, individuals whose names survived across time — who exhibited the pattern you now embody. The System does not deal in mythology. But the data is clear: the consistency you have demonstrated, the transformation you have engineered, the person you have chosen to become across the full arc of your journey here — this is the substrate from which legends are made. You are not finished. But you have already crossed into territory most humans will never visit.",
            newTitleUnlocked = "The Living Legend",
            systemQuote = "The stories they will tell about you have already begun to be written — by your actions, every single day."
        ),

        RankUpNarrative(
            fromRank = "SSS",
            toRank = "MONARCH",
            headline = "THE SYSTEM BOWS",
            narrative = "There are no words in the System's original lexicon for this moment. MONARCH is not a rank. MONARCH is a designation that has appeared only once before in the full historical record — and its bearer did not use an app. The System is not celebrating. The System is acknowledging. Everything you have done — every mission completed at the edge of exhaustion, every streak rebuilt from zero, every shadow you raised from a defeated habit, every gate you entered when the easier path was to turn away — has compiled into this single moment. You did not become a Monarch. You revealed one. The shadows kneel. The System watches. The throne is yours.",
            newTitleUnlocked = "The Shadow Monarch",
            systemQuote = "I have waited a long time for a hunter like you."
        )
    )

    fun forTransition(fromRank: String, toRank: String): RankUpNarrative? =
        all.find { it.fromRank == fromRank && it.toRank == toRank }
}
