import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.decodeToString
import aws.smithy.kotlin.runtime.content.toByteArray
import aws.smithy.kotlin.runtime.content.writeToFile
import kotlinx.coroutines.runBlocking
import java.io.File

suspend fun getObjectBytes(bucketName: String, keyName: String, path: String) {

    val request =  GetObjectRequest {
        key = keyName
        bucket= bucketName
    }

    println("hello")
    S3Client { region = "eu-central-1" }.use { s3 ->
        s3.getObject(request) { resp ->
            val myFile = File(path)
//            resp.body?.writeToFile(myFile)
            println("Successfully read $keyName from $bucketName")

            val byteArray = resp.body?.toByteArray()
            if(byteArray != null) {
                byteArray.forEach { println(it) }
                println(byteArray.size)
                println(String(byteArray, 0, byteArray.size))
                println("end")
            }
        }
    }
}


fun main() = runBlocking {
    getObjectBytes(
        bucketName = "telegrambotaybjax",
        keyName = "telegram.json",
        path = "C:\\Users\\aybja\\Desktop\\hello.json"
    )
}
//{
//    val bucket_name = "telegrambotaybjax"
//    val key_name = "telegram.json"
//
//    System.out.format("Downloading %s from S3 bucket %s...\n", key_name, bucket_name);
//    val s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
////    try {
//        val o = s3.getObject(bucket_name, key_name);
//        val s3is = o.objectContent;
//        val fos = FileOutputStream(File(key_name));
//        val read_buf = ByteArray(1024);
//        var read_len = 0;
//
//        read_len = s3is.read(read_buf)
//        while (read_len > 0) {
//            fos.write(read_buf, 0, read_len);
//            read_len = s3is.read(read_buf)
//        }
//
//        s3is.close();
//        fos.close();
//
//        println(read_buf.toString())
//    } catch (AmazonServiceException e) {
//        System.err.println(e.getErrorMessage());
//        System.exit(1);
//    } catch (FileNotFoundException e) {
//        System.err.println(e.getMessage());
//        System.exit(1);
//    } catch (IOException e) {
//        System.err.println(e.getMessage());
//        System.exit(1);
//    }
//}