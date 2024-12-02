data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val isAdmin: Boolean = false,
    val profilePictureUrl: String? = null,
    val bankAccounts: List<String> = emptyList()
) 