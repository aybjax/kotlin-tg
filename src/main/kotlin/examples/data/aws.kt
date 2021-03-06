package examples.data

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.toByteArray
import com.squareup.moshi.Moshi
import examples.models.TestData

suspend fun getObjectBytes(s3region: String, bucketName: String, keyName: String) {

    val request =  GetObjectRequest {
        key = keyName
        bucket= bucketName
    }

    S3Client { region = s3region }.use { s3 ->
        s3.getObject(request) { resp ->
            println("Successfully read $keyName from $bucketName")

            val result = String(
                resp.body?.toByteArray() ?: byteArrayOf()
            )

            println(result)

            val moshi: Moshi = Moshi.Builder().build()
            val jsonAdapter = moshi.adapter(TestData::class.java)

            val test = jsonAdapter.fromJson(result)
            println(test.toString())
        }
    }
}


//fun main() = runBlocking {
//    getObjectBytes(
//        bucketName = "telegrambotaybjax",
//        keyName = "telegram.json",
//    )
//}
