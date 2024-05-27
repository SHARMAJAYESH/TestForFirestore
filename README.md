# TestForFirestore
delete it later
import com.google.api.core.ApiFuture
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.util.concurrent.CompletableFuture

class FirestoreHandlerTest {

    private lateinit var firestoreClient: FirestoreClient
    private lateinit var firestoreHandler: FirestoreHandler
    private lateinit var firestore: Firestore

    @BeforeEach
    fun setUp() {
        firestore = mock(Firestore::class.java)
        firestoreClient = FirestoreClient(firestore)
        firestoreHandler = FirestoreHandler(firestoreClient)
    }

    @Test
    fun `addData should return OK status and success message`() {
        val mockFuture: ApiFuture<Void> = CompletableFuture.completedFuture(null)
        val mockDocumentReference = mock(Firestore::class.java)
        Mockito.`when`(firestore.collection("myCollection").document("1").set(mapOf("name" to "John"))).thenReturn(mockFuture)

        val request = Request(Method.POST, "/addData").body("""{"id":"1","content":{"name":"John"}}""")
        val response: Response = firestoreHandler.addData(request)
        assertEquals(Status.OK, response.status)
        assertEquals("Data added successfully", response.bodyString())
    }

    @Test
    fun `getData should return OK status and data`() {
        val mockDocumentSnapshot = mock(DocumentSnapshot::class.java)
        Mockito.`when`(mockDocumentSnapshot.exists()).thenReturn(true)
        Mockito.`when`(mockDocumentSnapshot.data).thenReturn(mapOf("name" to "John"))
        val mockFuture: ApiFuture<DocumentSnapshot> = CompletableFuture.completedFuture(mockDocumentSnapshot)
        Mockito.`when`(firestore.collection("myCollection").document("1").get()).thenReturn(mockFuture)

        val getRequest = Request(Method.GET, "/getData/1")
        val response: Response = firestoreHandler.getData(getRequest)
        assertEquals(Status.OK, response.status)
        assertEquals("{name=John}", response.bodyString())
    }

    @Test
    fun `getData should return NOT FOUND status if document does not exist`() {
        val mockDocumentSnapshot = mock(DocumentSnapshot::class.java)
        Mockito.`when`(mockDocumentSnapshot.exists()).thenReturn(false)
        val mockFuture: ApiFuture<DocumentSnapshot> = CompletableFuture.completedFuture(mockDocumentSnapshot)
        Mockito.`when`(firestore.collection("myCollection").document("1").get()).thenReturn(mockFuture)

        val getRequest = Request(Method.GET, "/getData/1")
        val response: Response = firestoreHandler.getData(getRequest)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("Document not found", response.bodyString())
    }

    @Test
    fun `addData should return INTERNAL SERVER ERROR status if an exception occurs`() {
        val exception = RuntimeException("Firestore exception")
        val mockFuture: ApiFuture<Void> = CompletableFuture.failedFuture(exception)
        Mockito.`when`(firestore.collection("myCollection").document("1").set(mapOf("name" to "John"))).thenReturn(mockFuture)

        val request = Request(Method.POST, "/addData").body("""{"id":"1","content":{"name":"John"}}""")
        val response: Response = firestoreHandler.addData(request)
        assertEquals(Status.INTERNAL_SERVER_ERROR, response.status)
        assertEquals("Error adding data: Firestore exception", response.bodyString())
    }

    @Test
    fun `getData should return INTERNAL SERVER ERROR status if an exception occurs`() {
        val exception = RuntimeException("Firestore exception")
        val mockFuture: ApiFuture<DocumentSnapshot> = CompletableFuture.failedFuture(exception)
        Mockito.`when`(firestore.collection("myCollection").document("1").get()).thenReturn(mockFuture)

        val getRequest = Request(Method.GET, "/getData/1")
        val response: Response = firestoreHandler.getData(getRequest)
        assertEquals(Status.INTERNAL_SERVER_ERROR, response.status)
        assertEquals("Error retrieving data: Firestore exception", response.bodyString())
    }
}
