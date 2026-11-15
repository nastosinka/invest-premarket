
package ru.example.invest.service

import ru.example.invest.client.TInvestMcpClient
import ru.tinkoff.kora.common.Component

@Component
class MarketDataService(
    private val mcpClient: TInvestMcpClient
) {

    fun getAvailableTools(): String {
        return mcpClient.listTools()
    }

    fun callTool(
        name: String,
        argumentsJson: String
    ): String {
        return mcpClient.callTool(name, argumentsJson)
    }
}
