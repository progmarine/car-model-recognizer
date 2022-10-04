package face.recognition.ai.common

data class CustomResponse<T>(
    val status: Int,
    val action: CustomReponseAction? = null,
    val data: T? = null
) {
    enum class CustomReponseAction {
        SUCCESSFUL,
        RETRY_REQUEST,
        TEMPORARILY_NOT_WORKING,
        FAILED
    }
}