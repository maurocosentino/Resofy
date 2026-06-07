package com.resofy.music.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE LyricsEntity")
        database.execSQL("DROP TABLE BlackListStoreEntity")
    }
}

val MIGRATION_24_25 = object : Migration(24, 25) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `server_configs` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `url` TEXT NOT NULL,
                `username` TEXT NOT NULL,
                `password` TEXT NOT NULL,
                `type` TEXT NOT NULL DEFAULT 'subsonic'
            )
        """.trimIndent())
    }
}