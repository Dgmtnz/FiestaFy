import com.google.firebase.firestore.GeoPoint

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = 0,
    val location: GeoPoint? = null,
    val organizer: String = "",
    val participants: List<String> = listOf(),
    val price: Double = 0.0
) 