package com.woodnoisu.reader.persistence

import androidx.sqlite.db.SupportSQLiteDatabase
import com.woodnoisu.reader.di.PersistenceModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

class AppDataBaseMigrationTest {

    @Test
    fun migrateFrom1To2PreservesExistingTablesAndCreatesBookSources() {
        Class.forName("org.sqlite.JDBC")
        DriverManager.getConnection("jdbc:sqlite::memory:").use { connection ->
            connection.createStatement().use { statement ->
                createVersionOneSchema(statement)
                statement.executeUpdate(
                    """
                    INSERT INTO my_shelf (
                      name, url, category, status, cover, author, desc, shopName,
                      chaptersUrl, charCount, chapterCount, favorite, updateDate, bookFilePath
                    ) VALUES (
                      '旧书', 'https://book.example/1', '分类', '连载', '', '作者', '简介',
                      '全文阅读', 'https://book.example/toc', 0, 0, 1, '今天', ''
                    )
                    """.trimIndent()
                )
            }

            runMigration(connection)

            connection.createStatement().use { statement ->
                assertEquals(1, statement.queryInt("SELECT COUNT(*) FROM my_shelf"))
                assertTrue(statement.tableExists("book_sources"))
                statement.executeUpdate(
                    """
                    INSERT INTO book_sources (
                      bookSourceName, bookSourceUrl, bookSourceType, customOrder,
                      enabled, enabledExplore, lastUpdateTime, weight
                    ) VALUES (
                      '迁移源', 'https://source.example', 0, 1, 1, 1, 0, 0
                    )
                    """.trimIndent()
                )
                assertEquals(
                    1,
                    statement.queryInt("SELECT COUNT(*) FROM book_sources WHERE bookSourceUrl='https://source.example'")
                )
            }
        }
    }

    private fun runMigration(connection: Connection) {
        connection.createStatement().use { statement ->
            val db = Proxy.newProxyInstance(
                SupportSQLiteDatabase::class.java.classLoader,
                arrayOf(SupportSQLiteDatabase::class.java)
            ) { _, method, args ->
                when (method.name) {
                    "execSQL" -> {
                        statement.execute(args?.getOrNull(0) as String)
                        Unit
                    }
                    else -> throw UnsupportedOperationException(method.name)
                }
            } as SupportSQLiteDatabase
            PersistenceModule.MIGRATION_1_2.migrate(db)
        }
    }

    private fun createVersionOneSchema(statement: Statement) {
        statement.execute(
            """
            CREATE TABLE IF NOT EXISTS `my_shelf` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `name` TEXT NOT NULL,
              `url` TEXT NOT NULL,
              `category` TEXT NOT NULL,
              `status` TEXT NOT NULL,
              `cover` TEXT NOT NULL,
              `author` TEXT NOT NULL,
              `desc` TEXT NOT NULL,
              `shopName` TEXT NOT NULL,
              `chaptersUrl` TEXT NOT NULL,
              `charCount` INTEGER NOT NULL,
              `chapterCount` INTEGER NOT NULL,
              `favorite` INTEGER NOT NULL,
              `updateDate` TEXT NOT NULL,
              `bookFilePath` TEXT NOT NULL
            )
            """.trimIndent()
        )
        statement.execute(
            """
            CREATE TABLE IF NOT EXISTS `cached_chapters` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `shopName` TEXT NOT NULL,
              `bookUrl` TEXT NOT NULL,
              `url` TEXT NOT NULL,
              `name` TEXT NOT NULL,
              `index` INTEGER NOT NULL,
              `content` TEXT NOT NULL,
              `start` INTEGER NOT NULL,
              `end` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS `index_cached_chapters_url` ON `cached_chapters` (`url`)")
        statement.execute(
            """
            CREATE TABLE IF NOT EXISTS `my_signs` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `bookUrl` TEXT NOT NULL,
              `chapterUrl` TEXT NOT NULL,
              `chapterName` TEXT NOT NULL,
              `saveTime` INTEGER NOT NULL,
              `edit` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS `index_my_signs_chapterUrl` ON `my_signs` (`chapterUrl`)")
        statement.execute(
            """
            CREATE TABLE IF NOT EXISTS `my_read_records` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `bookUrl` TEXT NOT NULL,
              `bookMd5` TEXT NOT NULL,
              `chapterPos` INTEGER NOT NULL,
              `pagePos` INTEGER NOT NULL,
              `lastRead` TEXT NOT NULL
            )
            """.trimIndent()
        )
        statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS `index_my_read_records_bookMd5` ON `my_read_records` (`bookMd5`)")
    }

    private fun Statement.queryInt(sql: String): Int {
        return executeQuery(sql).use { result ->
            result.next()
            result.getInt(1)
        }
    }

    private fun Statement.tableExists(tableName: String): Boolean {
        return executeQuery(
            "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='$tableName'"
        ).use { result ->
            result.next()
            result.getInt(1) == 1
        }
    }
}
