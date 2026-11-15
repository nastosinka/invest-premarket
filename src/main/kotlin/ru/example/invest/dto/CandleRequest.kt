data class CandleRequest(
    val ticker: String,
    val from: String,
    val to: String,
    val interval: String
)