package com.example.fittrackr

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrackr.ui.theme.FitTrackrTheme
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class WorkoutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackrTheme {
                WorkoutScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    var stepsText by remember { mutableStateOf("") }
    var caloriesText by remember { mutableStateOf("") }
    var minutesText by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val gradientBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF4CAF50), Color(0xFF1B5E20))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Workout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isSaving) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBg)
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Enter Today’s Workout",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {

                        OutlinedTextField(
                            value = stepsText,
                            onValueChange = { stepsText = it },
                            label = { Text("Steps") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = caloriesText,
                            onValueChange = { caloriesText = it },
                            label = { Text("Calories Burned") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = minutesText,
                            onValueChange = { minutesText = it },
                            label = { Text("Workout Minutes") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val steps = stepsText.toIntOrNull() ?: 0
                                val calories = caloriesText.toIntOrNull() ?: 0
                                val minutes = minutesText.toIntOrNull() ?: 0

                                // Basic validation: don't save empty entry
                                if (steps == 0 && calories == 0 && minutes == 0) {
                                    Toast.makeText(
                                        context,
                                        "Please enter at least one value",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                isSaving = true

                                // 1) Save a workout log (history) - professional
                                val logData = hashMapOf(
                                    "steps" to steps,
                                    "calories" to calories,
                                    "workoutMinutes" to minutes,
                                    "timestamp" to Timestamp.now()
                                )

                                db.collection("workoutLogs")
                                    .add(logData)
                                    .addOnSuccessListener {

                                        // 2) Update dashboard stats doc
                                        val dashboardData = hashMapOf(
                                            "steps" to steps,
                                            "calories" to calories,
                                            "workoutMinutes" to minutes,
                                            "updatedAt" to Timestamp.now()
                                        )

                                        db.collection("dailyStats")
                                            .document("today")
                                            .set(dashboardData, SetOptions.merge()) // ✅ don’t wipe doc
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    context,
                                                    "Workout saved",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                isSaving = false
                                                onBack()
                                            }
                                            .addOnFailureListener {
                                                isSaving = false
                                                Toast.makeText(
                                                    context,
                                                    "Failed to update dashboard stats",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                    .addOnFailureListener {
                                        isSaving = false
                                        Toast.makeText(
                                            context,
                                            "Failed to save workout log",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text("Saving…", fontWeight = FontWeight.Bold)
                            } else {
                                Text("Save Workout", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
