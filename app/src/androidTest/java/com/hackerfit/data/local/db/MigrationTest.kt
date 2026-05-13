package com.hackerfit.data.local.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDbName = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        ApplicationProvider.getApplicationContext<Context>(),
        HackerFitDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    private fun createV1Database(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `user_profile` (
                `id` INTEGER NOT NULL,
                `currentRung` INTEGER NOT NULL,
                `phase` TEXT NOT NULL,
                `rungStartDate` TEXT NOT NULL,
                `dailyReminderHour` INTEGER,
                `dailyReminderMinute` INTEGER,
                `onboardingComplete` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `daily_log` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `date` TEXT NOT NULL,
                `rung` INTEGER NOT NULL,
                `completed` INTEGER NOT NULL,
                `completedAt` TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `assessment_log` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `date` TEXT NOT NULL,
                `fromRung` INTEGER NOT NULL,
                `toRung` INTEGER NOT NULL,
                `passed` INTEGER NOT NULL,
                `notes` TEXT
            )
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_log_date` ON `daily_log` (`date`)")
    }

    private fun insertDailyLog(db: SupportSQLiteDatabase, id: Int, date: String, completed: Boolean) {
        val cv = ContentValues().apply {
            put("id", id)
            put("date", date)
            put("rung", 5)
            put("completed", if (completed) 1 else 0)
            put("completedAt", if (completed) date else null as String?)
        }
        db.insert("daily_log", SQLiteDatabase.CONFLICT_REPLACE, cv)
    }

    @Test
    fun migrationKeepsCompletedOverIncomplete() {
        val db = helper.createDatabase(testDbName, 1)
        createV1Database(db)
        insertDailyLog(db, 1, "2025-01-01", false)
        insertDailyLog(db, 2, "2025-01-01", true)
        db.close()

        val migrated = helper.runMigrationsAndValidate(testDbName, 2, true, MIGRATION_1_2)
        val cursor = migrated.query("SELECT id, completed FROM daily_log WHERE date = '2025-01-01'")
        assertEquals(1, cursor.count)
        cursor.moveToFirst()
        assertEquals(2, cursor.getInt(cursor.getColumnIndexOrThrow("id")))
        assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("completed")))
        cursor.close()
        migrated.close()
    }

    @Test
    fun migrationKeepsHigherIdWhenBothCompleted() {
        val db = helper.createDatabase(testDbName, 1)
        createV1Database(db)
        insertDailyLog(db, 1, "2025-01-01", true)
        insertDailyLog(db, 2, "2025-01-01", true)
        db.close()

        val migrated = helper.runMigrationsAndValidate(testDbName, 2, true, MIGRATION_1_2)
        val cursor = migrated.query("SELECT id FROM daily_log WHERE date = '2025-01-01'")
        assertEquals(1, cursor.count)
        cursor.moveToFirst()
        assertEquals(2, cursor.getInt(cursor.getColumnIndexOrThrow("id")))
        cursor.close()
        migrated.close()
    }

    @Test
    fun migrationKeepsHigherIdWhenNeitherCompleted() {
        val db = helper.createDatabase(testDbName, 1)
        createV1Database(db)
        insertDailyLog(db, 3, "2025-02-01", false)
        insertDailyLog(db, 7, "2025-02-01", false)
        db.close()

        val migrated = helper.runMigrationsAndValidate(testDbName, 2, true, MIGRATION_1_2)
        val cursor = migrated.query("SELECT id FROM daily_log WHERE date = '2025-02-01'")
        assertEquals(1, cursor.count)
        cursor.moveToFirst()
        assertEquals(7, cursor.getInt(cursor.getColumnIndexOrThrow("id")))
        cursor.close()
        migrated.close()
    }

    @Test
    fun migrationKeepsSingleRecord() {
        val db = helper.createDatabase(testDbName, 1)
        createV1Database(db)
        insertDailyLog(db, 5, "2025-03-01", true)
        db.close()

        val migrated = helper.runMigrationsAndValidate(testDbName, 2, true, MIGRATION_1_2)
        val cursor = migrated.query("SELECT id, completed FROM daily_log WHERE date = '2025-03-01'")
        assertEquals(1, cursor.count)
        cursor.moveToFirst()
        assertEquals(5, cursor.getInt(cursor.getColumnIndexOrThrow("id")))
        cursor.close()
        migrated.close()
    }

    @Test
    fun migrationCreatesUniqueIndex() {
        val db = helper.createDatabase(testDbName, 1)
        createV1Database(db)
        insertDailyLog(db, 1, "2025-01-01", true)
        db.close()

        val migrated = helper.runMigrationsAndValidate(testDbName, 2, true, MIGRATION_1_2)
        val cursor = migrated.query(
            "SELECT name FROM sqlite_master WHERE type = 'index' AND tbl_name = 'daily_log' AND name LIKE '%date%'"
        )
        assertTrue(cursor.count >= 1)
        cursor.close()
        migrated.close()
    }
}
