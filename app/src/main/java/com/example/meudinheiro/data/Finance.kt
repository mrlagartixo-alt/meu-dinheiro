package com.example.meudinheiro.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val amount: Double,
    val type: String, // INCOME / EXPENSE
    val category: String,
    val essential: Boolean,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val amount: Double,
    val dueDate: Long,
    val paid: Boolean = false
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun observeAll(): Flow<List<Transaction>>

    @Insert
    suspend fun insert(item: Transaction)

    @Update
    suspend fun update(item: Transaction)

    @Delete
    suspend fun delete(item: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun clear()
}

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY dueDate ASC")
    fun observeAll(): Flow<List<Bill>>

    @Insert
    suspend fun insert(item: Bill)

    @Update
    suspend fun update(item: Bill)

    @Delete
    suspend fun delete(item: Bill)
}

@Database(entities = [Transaction::class, Bill::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun billDao(): BillDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: android.content.Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meu_dinheiro.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
