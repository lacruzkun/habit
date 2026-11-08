package com.focusflow.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        CategoryEntity::class,
        TaskEntity::class,
        FocusSessionEntity::class,
        FocusSegmentEntity::class,
        HabitEntity::class,
        HabitCompletionEntity::class,
        DistractionEntity::class,
        ReflectionEntity::class,
        ReminderEntity::class,
        ActiveTimerEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class FocusFlowDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun taskDao(): TaskDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun habitDao(): HabitDao
    abstract fun distractionDao(): DistractionDao
    abstract fun reflectionDao(): ReflectionDao
    abstract fun reminderDao(): ReminderDao
    abstract fun activeTimerDao(): ActiveTimerDao

    companion object {
        @Volatile
        private var instance: FocusFlowDatabase? = null

        fun get(context: Context): FocusFlowDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FocusFlowDatabase::class.java,
                    "focusflow.db",
                )
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { instance = it }
            }
    }
}
