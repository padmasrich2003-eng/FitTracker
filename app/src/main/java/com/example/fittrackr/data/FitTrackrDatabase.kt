package com.example.fittrackr.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DailyStat::class],
    version = 1,
    exportSchema = false
)
abstract class FitTrackrDatabase : RoomDatabase() {

    abstract fun dailyStatDao(): DailyStatDao

    companion object {
        @Volatile
        private var INSTANCE: FitTrackrDatabase? = null

        fun getInstance(context: Context): FitTrackrDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitTrackrDatabase::class.java,
                    "fittrackr_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}