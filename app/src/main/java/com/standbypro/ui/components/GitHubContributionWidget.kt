package com.standbypro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.data.GitHubContributionsState

// Authentic GitHub Contribution Heatmap Colors
val GitHubLevel0 = Color(0xFF1E1E22)
val GitHubLevel1 = Color(0xFF0E4429)
val GitHubLevel2 = Color(0xFF006D32)
val GitHubLevel3 = Color(0xFF26A641)
val GitHubLevel4 = Color(0xFF39D353)

@Composable
fun GitHubContributionWidget(
    state: GitHubContributionsState,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isCompact: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Header: User, Total Contribs & Streak
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // GitHub Logo Icon
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "GitHub",
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "@${state.username}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "• ${state.totalContributions} contribs",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Streak Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE36209).copy(alpha = 0.2f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = Color(0xFFF9826C),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${state.currentStreak}d",
                    color = Color(0xFFF9826C),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                if (state.isLoading) {
                    Spacer(modifier = Modifier.width(4.dp))
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 1.5.dp,
                        modifier = Modifier.size(9.dp)
                    )
                }
            }
        }

        // The Heatmap Matrix: Columns (Weeks) x Rows (Days of week)
        val weeks = state.recentWeeks
        if (weeks.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                weeks.forEach { week ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.5.dp)
                    ) {
                        week.forEach { day ->
                            val cellColor = when (day.level) {
                                0 -> GitHubLevel0
                                1 -> GitHubLevel1
                                2 -> GitHubLevel2
                                3 -> GitHubLevel3
                                else -> GitHubLevel4
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(cellColor)
                            )
                        }
                    }
                }
            }
        }
    }
}
