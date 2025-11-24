package com.example.fittrackr

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrackr.data.FitTrackrDatabase
import com.example.fittrackr.ui.theme.FitTrackrTheme
import androidx.compose.runtime.collectAsState

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackrTheme {
                HomeScreenUI()
            }
        }
    }
}

@Composable
fun HomeScreenUI() {

    val context = LocalContext.current
    val dao = remember { FitTrackrDatabase.getInstance(context).dailyStatDao() }

    // Observe latest stats (read-only)
    val latestStat by dao.getLatestStat().collectAsState(initial = null)

    val steps = latestStat?.steps ?: 0
    val calories = latestStat?.calories ?: 0
    val workoutMinutes = latestStat?.workoutMinutes ?: 0

    val gradientBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF4CAF50),
            Color(0xFF1B5E20)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBg)
            .padding(18.dp),
        contentAlignment = Alignment.TopCenter
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp)
        ) {

            // Title
            Text(
                text = "FitTrackr Dashboard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(25.dp))

            // Daily Stats Card (centered with side margins)
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f),   // 90% width -> centered by Column
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Today's Stats",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    StatItem(label = "Steps", value = steps.toString())
                    Spacer(modifier = Modifier.height(10.dp))

                    StatItem(label = "Calories Burned", value = "$calories kcal")
                    Spacer(modifier = Modifier.height(10.dp))

                    StatItem(label = "Workout Minutes", value = "$workoutMinutes min")
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Quick Action Buttons
            Text(
                text = "Quick Actions",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Log Workout -> go to another page, not auto-add data
                ActionButton("Log Workout") {
                    context.startActivity(
                        Intent(context, WorkoutActivity::class.java)
                    )
                }

                ActionButton("Track Steps") {
                    context.startActivity(
                        Intent(context, StepsActivity::class.java)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton("Sleep Entry") {
                    // later: open SleepActivity
                }
                ActionButton("Nutrition") {
                    // later: open NutritionActivity
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ActionButton(title: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50)
        )
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
