package orm

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.toByteArray
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import models.TestData
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object Users : IntIdTable() {
    val name = varchar("name", length = 50) // Column<String>
    //    val cityId = (integer("city_id") references Cities.id).nullable() // Column<Int?>
    val city = reference("city_id", Cities).nullable()
}

class User(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<User>(Users)
    var name by Users.name
    var city by City optionalReferencedOn Users.city
}

object Cities : IntIdTable() {
    val name = varchar("name", 50) // Column<String>
}

class City(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<City>(Cities)
    var name by Cities.name
}

fun main() {
    val db = Database.connect("jdbc:mysql://localhost:3306/kotlintg", driver = "com.mysql.cj.jdbc.Driver",
        user = "root", password = "")

    transaction {
//        SchemaUtils.createMissingTablesAndColumns(Cities, Users)
//        SchemaUtils.drop(Cities, Users)
//        SchemaUtils.create(Cities, Users)
    }

    println("created")

//    val city1 = transaction {
//        City.new {
//            name = "Qarafandy"
//        }
//    }
//
//    val user = transaction {
//        User.new {
//            name = "aybjax"
//            city = city1
//        }
//    }

    transaction {
        User.findById(1)?.load(User::city)?.let {
            println(it)
            println(it.name)
            println(it.city?.id ?: 1)
        }
    }
}