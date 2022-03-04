package network.aws

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.toByteArray

suspend fun getObjectBytes(s3region: String, bucketName: String, keyName: String): String {

    val request =  GetObjectRequest {
        key = keyName
        bucket= bucketName
    }

    S3Client { region = s3region }.use { s3 ->
        return s3.getObject(request) { resp ->
            println("Successfully read $keyName from $bucketName")

            return@getObject String(
                resp.body?.toByteArray() ?: byteArrayOf()
            )
        }
    }
}
