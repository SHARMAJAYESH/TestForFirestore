# TestForFirestore
delete it later
import com.google.api.core.ApiFuture
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.WriteResult
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
        val mockFuture: ApiFuture<WriteResult> = mock(ApiFuture::class.java) as ApiFuture<WriteResult>
        val mockWriteResult: WriteResult = mock(WriteResult::class.java)
        Mockito.`when`(mockFuture.get()).thenReturn(mockWriteResult)
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
        val mockFuture: ApiFuture<DocumentSnapshot> = mock(ApiFuture::class.java) as ApiFuture<DocumentSnapshot>
        Mockito.`when`(firestore.collection("myCollection").document("1").get()).thenReturn(mockFuture)
        Mockito.`when`(mockFuture.get()).thenReturn(mockDocumentSnapshot)

        val getRequest = Request(Method.GET, "/getData/1")
        val response: Response = firestoreHandler.getData(getRequest)
        assertEquals(Status.OK, response.status)
        assertEquals("{name=John}", response.bodyString())
    }

    @Test
    fun `getData should return NOT FOUND status if document does not exist`() {
        val mockDocumentSnapshot = mock(DocumentSnapshot::class.java)
        Mockito.`when`(mockDocumentSnapshot.exists()).thenReturn(false)
        val mockFuture: ApiFuture<DocumentSnapshot> = mock(ApiFuture::class.java) as ApiFuture<DocumentSnapshot>
        Mockito.`when`(firestore.collection("myCollection").document("1").get()).thenReturn(mockFuture)
        Mockito.`when`(mockFuture.get()).thenReturn(mockDocumentSnapshot)

        val getRequest = Request(Method.GET, "/getData/1")
        val response: Response = firestoreHandler.getData(getRequest)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("Document not found", response.bodyString())
    }

    @Test
    fun `addData should return INTERNAL SERVER ERROR status if an exception occurs`() {
        val exception = RuntimeException("Firestore exception")
        val mockFuture: ApiFuture<WriteResult> = mock(ApiFuture::class.java) as ApiFuture<WriteResult>
        Mockito.`when`(mockFuture.get()).thenThrow(exception)
        Mockito.`when`(firestore.collection("myCollection").document("1").set(mapOf("name" to "John"))).thenReturn(mockFuture)

        val request = Request(Method.POST, "/addData").body("""{"id":"1","content":{"name":"John"}}""")
        val response: Response = firestoreHandler.addData(request)
        assertEquals(Status.INTERNAL_SERVER_ERROR, response.status)
        assertEquals("Error adding data: Firestore exception", response.bodyString())
    }

    @Test
    fun `getData should return INTERNAL SERVER ERROR status if an exception occurs`() {
        val exception = RuntimeException("Firestore exception")
        val mockFuture: ApiFuture<DocumentSnapshot> = mock(ApiFuture::class.java) as ApiFuture<DocumentSnapshot>
        Mockito.`when`(mockFuture.get()).thenThrow(exception)
        Mockito.`when`(firestore.collection("myCollection").document("1").get()).thenReturn(mockFuture)

        val getRequest = Request(Method.GET, "/getData/1")
        val response: Response = firestoreHandler.getData(getRequest)
        assertEquals(Status.INTERNAL_SERVER_ERROR, response.status)
        assertEquals("Error retrieving data: Firestore exception", response.bodyString())
    }
}
