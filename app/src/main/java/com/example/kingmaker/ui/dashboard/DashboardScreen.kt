package com.example.kingmaker.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kingmaker.R
import com.example.kingmaker.service.ContactStats
import com.example.kingmaker.service.QueuedPerson

@Composable
fun DashboardScreen(
    activeGoal: String,
    queuedPeople: List<QueuedPerson>,
    stats: ContactStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.dashboard_background))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kingmaker",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.popup_text_primary)
            )
            Text(
                text = "Today",
                fontSize = 14.sp,
                color = colorResource(R.color.popup_text_secondary)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Active goal",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.popup_badge_text)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = colorResource(R.color.popup_badge_background)
        ) {
            Text(
                text = activeGoal,
                modifier = Modifier.padding(20.dp),
                fontSize = 18.sp,
                lineHeight = 24.sp,
                color = colorResource(R.color.popup_text_primary)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Queued people",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.popup_text_primary)
        )
        Spacer(modifier = Modifier.height(12.dp))
        queuedPeople.forEach { person ->
            QueuedPersonRow(person)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "All contacts",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.popup_text_primary)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(value = stats.total.toString(), label = "total", modifier = Modifier.weight(1f))
            StatTile(value = stats.active.toString(), label = "active", modifier = Modifier.weight(1f))
            StatTile(value = stats.needAttention.toString(), label = "need attention", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun QueuedPersonRow(person: QueuedPerson) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colorResource(R.color.popup_card_background),
        border = BorderStroke(1.dp, colorResource(R.color.popup_handle))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(colorResource(R.color.avatar_background), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.initials,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.avatar_text)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = person.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.popup_text_primary)
                )
                Text(
                    text = "${person.channel} - ${person.reason}",
                    fontSize = 13.sp,
                    color = colorResource(R.color.popup_text_secondary)
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(colorResource(R.color.popup_badge_background), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.position.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.popup_badge_text)
                )
            }
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colorResource(R.color.popup_card_background),
        border = BorderStroke(1.dp, colorResource(R.color.popup_handle))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.popup_text_primary)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = colorResource(R.color.popup_text_secondary)
            )
        }
    }
}
