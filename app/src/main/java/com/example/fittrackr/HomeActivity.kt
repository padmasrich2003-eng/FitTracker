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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrackr.ui.theme.FitTrackrTheme
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
fun HomeScreenUI(onLogout: () -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    var steps by remember { mutableStateOf(0) }
    var calories by remember { mutableStateOf(0) }
    var workoutMinutes by remember { mutableStateOf(0) }
    var lastUpdatedText by remember { mutableStateOf("—") }

    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    // ✅ Live updates from Firestore (auto-refresh dashboard)
    DisposableEffect(Unit) {
        val docRef = db.collection("dailyStats").document("today")
        val registration: ListenerRegistration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                errorText = "Failed to load stats. Check internet / Firebase rules."
                isLoading = false
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                steps = (snapshot.getLong("steps") ?: 0L).toInt()
                calories = (snapshot.getLong("calories") ?: 0L).toInt()
                workoutMinutes = (snapshot.getLong("workoutMinutes") ?: 0L).toInt()

                val ts = snapshot.getTimestamp("updatedAt")
                lastUpdatedText = if (ts != null) dateFormat.format(ts.toDate()) else dateFormat.format(Date())

                errorText = null
            } else {
                // If doc doesn't exist yet, show zeros (no crash)
                steps = 0
                calories = 0
                workoutMinutes = 0
                lastUpdatedText = "—"
            }
            isLoading = false
        }

        onDispose { registration.remove() }
    }

    // Refresh button (manual load)
    fun manualRefresh() {
        isLoading = true
        db.collection("dailyStats").document("today").get()
            .addOnSuccessListener { doc ->
                steps = (doc.getLong("steps") ?: 0L).toInt()
                calories = (doc.getLong("calories") ?: 0L).toInt()
                workoutMinutes = (doc.getLong("workoutMinutes") ?: 0L).toInt()
                lastUpdatedText = dateFormat.format(Date())
                errorText = null
                isLoading = false
            }
            .addOnFailureListener {
                errorText = "Failed to refresh stats."
                isLoading = false
            }
    }

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
                .padding(top = 55.dp)
        ) {

            Text(
                text = "FitTrackr Dashboard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Last updated: $lastUpdatedText",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ✅ Loading / Error UI
            if (isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(10.dp))
                        Text("Loading your stats…", fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (errorText != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = errorText ?: "",
                            color = Color(0xFFB71C1C),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { manualRefresh() }) {
                            Text("Try Again")
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ✅ Main Stats Card
            DailyStatsCardPro(
                steps = steps,
                calories = calories,
                workoutMinutes = workoutMinutes,
                onRefresh = { manualRefresh() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Quick Actions",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(14.dp))

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

            Spacer(modifier = Modifier.height(14.dp))

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

            Spacer(modifier = Modifier.height(26.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Logout", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DailyStatsCardPro(
    steps: Int,
    calories: Int,
    workoutMinutes: Int,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(0.92f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Statistics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onRefresh) {
                    Text("Refresh")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatTile(title = "Steps", value = steps.toString())
                StatTile(title = "Calories", value = "$calories kcal")
                StatTile(title = "Workout", value = "$workoutMinutes min")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tip: Add a workout or sleep entry to update your dashboard instantly.",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun StatTile(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun ActionButton(name: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
        modifier = Modifier.width(160.dp)
    ) {
        Text(
            text = name,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
