package network.aws

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.toByteArray
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

/**
 * S3 interaction class
 */
class S3BucketReader(private val s3region: String, private val bucketName: String)
{
    /**
     * Read file as String
     */
    suspend fun getBucketObjectAsString(file_path: String): String {

        val request =  GetObjectRequest {
            key = file_path
            bucket= bucketName
        }

        S3Client { region = s3region }.use { s3 ->
            return s3.getObject(request) { resp ->
                println("Successfully read $file_path from $bucketName")

                return@getObject String(
                    resp.body?.toByteArray() ?: byteArrayOf()
                )
            }
        }
    }

    /**
     * Dynamically create and cash moshi and get file as object
     */
    suspend inline fun <reified T> getBucketObject(file_path: String): T?
    {
        val adapter = getClassAdapter<T>()
        val response = getBucketObjectAsString(file_path)

        return adapter.fromJson(response);
    }

    companion object {
        /**
         * Local cash of Moshi Adapters
         */
        val class_adapters = mutableMapOf<String, JsonAdapter<Any>>()

        /**
         * Get and cache Moshi Adapters
         */
        inline fun <reified T> getClassAdapter(): JsonAdapter<T>
        {
            (class_adapters[T::class.java.name] as? JsonAdapter<T>)
                ?.let { return it }

            val adapter = Moshi.Builder().build().adapter(T::class.java)

            class_adapters[T::class.java.name] = adapter as JsonAdapter<Any>

            return adapter;
        }
    }
}
