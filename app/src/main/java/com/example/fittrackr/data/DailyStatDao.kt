package com.example.fittrackr.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stat: DailyStat)

    @Query("SELECT * FROM daily_stats ORDER BY id DESC LIMIT 1")
    fun getLatestStat(): Flow<DailyStat?>
}
