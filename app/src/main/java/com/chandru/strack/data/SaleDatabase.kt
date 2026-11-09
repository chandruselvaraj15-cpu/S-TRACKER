package com.chandru.strack.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brand: String,
    val model: String,
    val quantity: Int,
    val timestampMillis: Long,
    val notes: String = ""
)

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<SaleEntity>>

    @Insert
    suspend fun insert(sale: SaleEntity): Long

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Database(entities = [SaleEntity::class], version = 1, exportSchema = false)
abstract class STrackDatabase : RoomDatabase() {
    abstract fun saleDao(): SaleDao
}
