package com.example.fittrackr

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    // Observe the most recent stat entry
    val latestStat by dao.getLatestStat().collectAsState(initial = null)

    val steps = latestStat?.steps ?: 0
    val calories = latestStat?.calories ?: 0
    val workoutMinutes = latestStat?.workoutMinutes ?: 0

    val gradientBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF4CAF50), Color(0xFF1B5E20))
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
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp)
        ) {

            Text(
                text = "FitTrackr Dashboard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ---- DAILY STATS CARD ----
            DailyStatsCard(steps, calories, workoutMinutes)

            Spacer(modifier = Modifier.height(25.dp))

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
                ActionButton("Log Workout") {
                    context.startActivity(Intent(context, WorkoutActivity::class.java))
                }

                ActionButton("Track Steps") {
                    context.startActivity(Intent(context, StepsActivity::class.java))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton("Sleep Entry") {
                    context.startActivity(Intent(context, SleepActivity::class.java))
                }

                ActionButton("Nutrition") {
                    context.startActivity(Intent(context, NutritionActivity::class.java))
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ---- LOGOUT BUTTON ----
            Button(
                onClick = {
                    Toast.makeText(context, "Logged out!", Toast.LENGTH_SHORT).show()
                    context.startActivity(Intent(context, LoginPageActivity::class.java))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Logout", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun DailyStatsCard(steps: Int, calories: Int, workoutMinutes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(0.9f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Today's Statistics", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(15.dp))

            StatItem("Steps", steps.toString())
            Spacer(modifier = Modifier.height(10.dp))

            StatItem("Calories Burned", "$calories kcal")
            Spacer(modifier = Modifier.height(10.dp))

            StatItem("Workout Minutes", "$workoutMinutes min")
        }
    }
}

@Composable
fun StatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActionButton(name: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
    ) {
        Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
