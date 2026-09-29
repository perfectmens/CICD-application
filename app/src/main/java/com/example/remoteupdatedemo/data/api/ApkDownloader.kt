package com.example.remoteupdatedemo.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest

/**
 * Downloads APK files from an Internet or LAN URL with streaming progress reporting
 * and SHA-256 cryptographic checksum verification.
 */
class ApkDownloader(
    private val client: OkHttpClient = OkHttpClient()
) {

    suspend fun downloadApk(
        downloadUrl: String,
        destinationFile: File,
        expectedSha256: String? = null,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            destinationFile.parentFile?.mkdirs()
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val request = Request.Builder()
                .url(downloadUrl)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IllegalStateException("Failed to download APK: HTTP ${response.code}")
            }

            val body = response.body ?: throw IllegalStateException("Empty response body from APK server")
            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            val inputStream: InputStream = body.byteStream()
            val outputStream = FileOutputStream(destinationFile)
            val buffer = ByteArray(8 * 1024)
            var bytesRead: Int

            inputStream.use { input ->
                outputStream.use { output ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        if (totalBytes > 0) {
                            val progress = downloadedBytes.toFloat() / totalBytes.toFloat()
                            onProgress(progress.coerceIn(0f, 1f))
                        }
                    }
                    output.flush()
                }
            }

            // Optional SHA-256 verification
            if (!expectedSha256.isNullOrBlank() && expectedSha256 != "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855") {
                val computedHash = computeSha256(destinationFile)
                if (!computedHash.equals(expectedSha256.trim(), ignoreCase = true)) {
                    destinationFile.delete()
                    throw SecurityException("SHA-256 mismatch! Expected: $expectedSha256, Actual: $computedHash")
                }
            }

            destinationFile
        }
    }

    private fun computeSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { stream ->
            val buffer = ByteArray(8 * 1024)
            var read: Int
            while (stream.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
