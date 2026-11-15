package ru.example.invest.client

interface TInvestMcpClient {
    fun listTools(): String

    fun callTool(
        name: String,
        argumentsJson: String
    ): String
}