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
import com.example.fittrackr.ui.theme.FitTrackrTheme
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackrTheme {
                HomeScreenUI(
                    onLogout = {
                        Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginPageActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreenUI(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    // UI state for stats (comes from Firestore)
    var steps by remember { mutableStateOf(0) }
    var calories by remember { mutableStateOf(0) }
    var workoutMinutes by remember { mutableStateOf(0) }

    // Load stats once when screen opens
    LaunchedEffect(Unit) {
        db.collection("dailyStats")
            .document("today")            // simple fixed doc, later you can use date
            .get()
            .addOnSuccessListener { doc ->
                steps = (doc.getLong("steps") ?: 0L).toInt()
                calories = (doc.getLong("calories") ?: 0L).toInt()
                workoutMinutes = (doc.getLong("workoutMinutes") ?: 0L).toInt()
            }
            .addOnFailureListener {
                // optional: show a toast or log error
            }
    }

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

            // Daily Stats Card
            DailyStatsCard(
                steps = steps,
                calories = calories,
                workoutMinutes = workoutMinutes
            )

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

            Button(
                onClick = { onLogout() },
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
            Text(
                text = "Today's Statistics",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
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
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ActionButton(name: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
    ) {
        Text(
            text = name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
