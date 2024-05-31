import com.google.auth.oauth2.GoogleCredentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

fun main() {
    val projectId = "your-project-id"
    val databaseId = "jayesh"
    val locationId = "us-central" // Specify the location ID

    // Obtain the default credentials
    val credentials = GoogleCredentials.getApplicationDefault()
        .createScoped(listOf("https://www.googleapis.com/auth/datastore"))

    // Refresh the credentials to get a valid access token
    credentials.refreshIfExpired()
    val accessToken = credentials.accessToken.tokenValue

    // Prepare the HTTP client
    val client = OkHttpClient()

    // Define the URL for the REST API call
    val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases?databaseId=$databaseId"

    // Define the JSON payload with locationId
    val jsonPayload = """
        {
          "type": "FIRESTORE_NATIVE",
          "locationId": "$locationId"
        }
    """.trimIndent()

    // Create the request body
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = RequestBody.create(mediaType, jsonPayload)

    // Create the HTTP request
    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .addHeader("Authorization", "Bearer $accessToken")
        .build()

    // Make the HTTP call
    val response: Response = client.newCall(request).execute()

    // Check the response
    if (response.isSuccessful) {
        println("Database created successfully: ${response.body?.string()}")
    } else {
        println("Error creating database: ${response.code} - ${response.message}")
        println("Response body: ${response.body?.string()}")
    }
}


// Another way - 

import com.google.auth.oauth2.GoogleCredentials
import org.http4k.client.OkHttp
import org.http4k.core.*
import org.http4k.core.Method.POST
import org.http4k.core.body.Body
import org.http4k.format.Gson.auto
import org.http4k.lens.Header
import org.http4k.lens.RequestContextLens
import java.io.IOException

data class FirestoreDatabaseRequest(val type: String, val locationId: String)

fun main() {
    val projectId = "your-project-id"
    val databaseId = "jayesh"
    val locationId = "us-central" // Specify the location ID

    // Obtain the default credentials
    val credentials = GoogleCredentials.getApplicationDefault()
        .createScoped(listOf("https://www.googleapis.com/auth/datastore"))

    // Refresh the credentials to get a valid access token
    credentials.refreshIfExpired()
    val accessToken = credentials.accessToken.tokenValue

    // Define the URL for the REST API call
    val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases?databaseId=$databaseId"

    // Define the JSON payload with locationId
    val firestoreDatabaseRequest = FirestoreDatabaseRequest("FIRESTORE_NATIVE", locationId)

    // Create the request
    val request = Request(POST, url)
        .with(
            Header.AUTHORIZATION of "Bearer $accessToken",
            Body.auto<FirestoreDatabaseRequest>().toLens() of firestoreDatabaseRequest
        )

    // Prepare the HTTP client
    val client = OkHttp()

    // Make the HTTP call
    val response = client(request)

    // Check the response
    if (response.status == Status.OK) {
        println("Database created successfully: ${response.bodyString()}")
    } else {
        println("Error creating database: ${response.status.code} - ${response.status.description}")
        println("Response body: ${response.bodyString()}")
    }
}
