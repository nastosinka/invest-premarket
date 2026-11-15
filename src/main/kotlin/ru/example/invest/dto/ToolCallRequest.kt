package ru.example.invest.dto

import ru.tinkoff.kora.json.common.annotation.Json

@Json
data class ToolCallRequest(
    val name: String,
    val argumentsJson: String
)
