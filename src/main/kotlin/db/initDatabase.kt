package db

import io.github.cdimascio.dotenv.dotenv
import mechanicum.db.transactions.initMechanicumTables
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

fun initDatabase() {
    Database.connect("jdbc:mysql://localhost:3306/kotlintg", driver = "com.mysql.cj.jdbc.Driver",
        user = "root", password = dotenv()["ps"]
    )

    transaction {
        initMechanicumTables()
    }
}
