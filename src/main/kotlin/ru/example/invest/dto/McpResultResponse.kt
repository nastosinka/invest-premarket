package ru.example.invest.dto

import ru.tinkoff.kora.json.common.annotation.Json

@Json
data class McpResultResponse(
    val payload: String
)
