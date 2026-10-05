package com.example.nearbymonitor.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DeviceEntity::class, ObservationEntity::class, LocationMeasurementEntity::class],
    version = 5,
    exportSchema = false
)
abstract class MonitorDatabase : RoomDatabase() {
    abstract fun monitorDao(): MonitorDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE devices ADD COLUMN deviceCategory TEXT NOT NULL DEFAULT 'Inconnu'")
                db.execSQL("ALTER TABLE devices ADD COLUMN identificationConfidence INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE devices ADD COLUMN identificationEvidenceCsv TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE devices ADD COLUMN manufacturerSource TEXT")
                db.execSQL("ALTER TABLE devices ADD COLUMN manufacturerConfidence INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE devices ADD COLUMN manufacturerPrefix TEXT")
                db.execSQL("ALTER TABLE devices ADD COLUMN macAddress TEXT")
                db.execSQL("ALTER TABLE devices ADD COLUMN macLocallyAdministered INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE devices ADD COLUMN identityKey TEXT")
                db.execSQL("ALTER TABLE devices ADD COLUMN identitySource TEXT")
                db.execSQL("ALTER TABLE devices ADD COLUMN identityConfidence INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE devices ADD COLUMN addressRandomized INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE devices ADD COLUMN identityAggregated INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE devices ADD COLUMN observedAddressesCsv TEXT NOT NULL DEFAULT ''")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_devices_identityKey ON devices(identityKey)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `location_measurements` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `deviceId` TEXT NOT NULL,
                        `xMeters` REAL NOT NULL,
                        `yMeters` REAL NOT NULL,
                        `rssi` INTEGER NOT NULL,
                        `referenceRssiAt1m` REAL NOT NULL,
                        `pathLossExponent` REAL NOT NULL,
                        `estimatedDistanceMeters` REAL NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_location_measurements_deviceId` " +
                        "ON `location_measurements` (`deviceId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_location_measurements_createdAt` " +
                        "ON `location_measurements` (`createdAt`)"
                )
            }
        }

        @Volatile
        private var INSTANCE: MonitorDatabase? = null

        fun getInstance(context: Context): MonitorDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MonitorDatabase::class.java,
                    "nearby_monitor.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
