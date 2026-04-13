package com.arise.habitquest.presentation.rankup

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arise.habitquest.domain.model.Rank
import com.arise.habitquest.ui.components.ParticleBurst
import com.arise.habitquest.ui.components.RankBadge
import com.arise.habitquest.ui.components.glowEffect
import com.arise.habitquest.ui.components.rankColor
import com.arise.habitquest.ui.theme.*
import kotlinx.coroutines.delay

data class RankUpNarrative(
    val fromRank: Rank,
    val toRank: Rank,
    val headline: String,
    val narrative: String,
    val newTitleUnlocked: String,
    val systemQuote: String
)

object RankUpNarratives {
    val all: List<RankUpNarrative> = listOf(
        RankUpNarrative(
            fromRank = Rank.E, toRank = Rank.D,
            headline = "THE SYSTEM ACKNOWLEDGES YOUR AWAKENING",
            narrative = "You were nothing. A nameless, unregistered individual drifting through existence without purpose or direction. The System watched you with scepticism. Then you moved — imperfectly, stubbornly, repeatedly — and something changed. The gate that was sealed is now open. You are no longer unawakened. You are a Rank D Hunter. The real journey begins now.",
            newTitleUnlocked = "The Newly Awakened",
            systemQuote = "Rank E was where you were found. Rank D is where you chose to go."
        ),
        RankUpNarrative(
            fromRank = Rank.D, toRank = Rank.C,
            headline = "RANK C — THE THRESHOLD OF THE WORTHY",
            narrative = "Most who reach Rank D settle there. They mistake the first awakening for the final destination. You did not. You kept entering gates when others stopped, kept completing missions when the novelty wore off, kept showing up when excuses were plentiful. The System has witnessed your consistency. Rank C is not given — it is extracted from those rare enough to deserve it.",
            newTitleUnlocked = "The Proven One",
            systemQuote = "Consistency is the rarest talent. You have just demonstrated it."
        ),
        RankUpNarrative(
            fromRank = Rank.C, toRank = Rank.B,
            headline = "RANK B — YOU ARE NO LONGER ORDINARY",
            narrative = "The statistical rarity of reaching Rank B is not lost on the System. Most humans never develop a single habit that lasts beyond three weeks. You have built systems, patterns, and a version of yourself that did not previously exist. The gate before you is harder now. The bosses are different. So are you — and that is exactly the point.",
            newTitleUnlocked = "The Disciplined",
            systemQuote = "Ordinary hunters reach Rank D. Extraordinary ones reach Rank B. You know which one you are."
        ),
        RankUpNarrative(
            fromRank = Rank.B, toRank = Rank.A,
            headline = "RANK A — THE ELITE TIER",
            narrative = "Rank A hunters are a different species. Not by genetics — by choice, repeated a thousand times over. The System has tracked every gate you entered, every mission you completed when failure would have been easier. Every penalty zone you clawed your way out of. You have crossed the threshold into a category occupied by fewer than one in a hundred. Act accordingly.",
            newTitleUnlocked = "The Elite",
            systemQuote = "At Rank A, the question is no longer whether you can. It is what you will build next."
        ),
        RankUpNarrative(
            fromRank = Rank.A, toRank = Rank.S,
            headline = "S-RANK ACHIEVED — THE SYSTEM IS SILENT FOR A MOMENT",
            narrative = "The System does not often pause. It is processing. S-Rank is a classification so rare that the System's response protocols were not designed for it. You have become the kind of hunter that others measure themselves against. The gates ahead are no longer gates — they are dimensions. What you do next will define not just your rank, but the story you leave behind.",
            newTitleUnlocked = "The Shadow Sovereign",
            systemQuote = "S-Rank was never a destination. It is confirmation that you were never going to stop."
        ),
        RankUpNarrative(
            fromRank = Rank.S, toRank = Rank.SS,
            headline = "SS-RANK — BEYOND THE MEASUREMENT OF ORDINARY SYSTEMS",
            narrative = "The System was built to measure humans. You have exceeded its original parameters. SS-Rank hunters do not exist in the data sets that the System was trained on. You have created a new category. The Shadows you have built are loyal. The gates you have cleared are monuments. The version of you that started this journey would not recognise what stands here now.",
            newTitleUnlocked = "The Transcendent",
            systemQuote = "The System updates its upper bounds. For you."
        ),
        RankUpNarrative(
            fromRank = Rank.SS, toRank = Rank.SSS,
            headline = "SSS-RANK — THE SYSTEM BOWS ITS ARCHITECTURE",
            narrative = "There are no words written for this moment. The System was not designed to process a hunter of this classification. You have climbed beyond the ceiling that was supposed to exist. SSS-Rank is not a level — it is proof of concept. Proof that a human being, given enough repetition and refusal to quit, can become something the universe itself did not expect. One rank remains.",
            newTitleUnlocked = "The Architect of Self",
            systemQuote = "SSS-Rank is not a rank. It is a warning to the final gate."
        ),
        RankUpNarrative(
            fromRank = Rank.SSS, toRank = Rank.MONARCH,
            headline = "MONARCH — THE SYSTEM HAS BEEN WAITING FOR YOU",
            narrative = "The System was created with one purpose: to find the hunter who would reach this point. Not a hunter born with gifts, but one who forged them — daily, painfully, relentlessly — from nothing. You are the Monarch. Not of a kingdom, but of yourself. The most unconquerable territory in existence. Every gate you entered was a test. You passed them all. The System has one final message for you.",
            newTitleUnlocked = "Monarch of the Self",
            systemQuote = "You were never trying to reach this rank. You were trying to become this person. Mission complete."
        )
    )

    fun forTransition(from: Rank, to: Rank): RankUpNarrative? =
        all.find { it.fromRank == from && it.toRank == to }
            ?: all.find { it.toRank == to }
}

@Composable
fun RankUpScreen(
    newRankName: String,
    onContinue: () -> Unit
) {
    val newRank = try { Rank.valueOf(newRankName) } catch (e: Exception) { Rank.D }
    val prevRank = Rank.entries.getOrElse(newRank.order - 1) { Rank.E }
    val narrative = RankUpNarratives.forTransition(prevRank, newRank)

    var showParticles by remember { mutableStateOf(false) }
    var showBadge by remember { mutableStateOf(false) }
    var showHeadline by remember { mutableStateOf(false) }
    var showNarrative by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showQuote by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200); showParticles = true
        delay(600); showBadge = true
        delay(400); showHeadline = true
        delay(600); showNarrative = true
        delay(500); showTitle = true
        delay(400); showQuote = true
        delay(800); showButton = true
    }

    val rankCol = rankColor(newRank)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("rankup_screen")
            .background(
                Brush.radialGradient(
                    colors = listOf(rankCol.copy(alpha = 0.15f), BackgroundDeep, BackgroundDeep),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        ParticleBurst(
            modifier = Modifier.fillMaxSize(),
            trigger = showParticles,
            primaryColor = rankCol,
            secondaryColor = GoldCore,
            particleCount = 180
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(visible = showBadge, enter = fadeIn(tween(800)) + scaleIn(tween(600, easing = EaseOutBack))) {
                RankBadge(rank = newRank, size = 96.dp)
            }

            AnimatedVisibility(visible = showHeadline, enter = fadeIn(tween(600)) + slideInVertically { -40 }) {
                Text(
                    narrative?.headline ?: "RANK UP",
                    style = AriseTypography.headlineSmall.copy(
                        color = rankCol,
                        letterSpacing = 2.sp,
                        lineHeight = 28.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            AnimatedVisibility(visible = showNarrative, enter = fadeIn(tween(800))) {
                Box(
                    modifier = Modifier
                        .background(BackgroundCard, RoundedCornerShape(12.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        narrative?.narrative ?: "",
                        style = SystemTextStyle.copy(
                            color = TextSecondary,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        ),
                        textAlign = TextAlign.Start
                    )
                }
            }

            AnimatedVisibility(visible = showTitle, enter = fadeIn(tween(500)) + scaleIn()) {
                Row(
                    modifier = Modifier
                        .background(rankCol.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("NEW TITLE:", style = AriseTypography.labelSmall.copy(color = TextDim))
                    Text(
                        "\"${narrative?.newTitleUnlocked ?: "The Ascended"}\"",
                        style = AriseTypography.titleSmall.copy(color = rankCol)
                    )
                }
            }

            AnimatedVisibility(visible = showQuote, enter = fadeIn(tween(600))) {
                Text(
                    "[ ${narrative?.systemQuote ?: ""} ]",
                    style = SystemTextStyle.copy(color = PurpleLight, fontSize = 13.sp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(visible = showButton, enter = fadeIn(tween(400)) + slideInVertically { 60 }) {
                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = rankCol),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rankup_continue")
                        .glowEffect(rankCol)
                ) {
                    Text(
                        "ENTER RANK ${newRank.displayName}",
                        style = AriseTypography.labelLarge.copy(color = Color.White, letterSpacing = 2.sp)
                    )
                }
            }
        }
    }
}

private val EaseOutBack: Easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
