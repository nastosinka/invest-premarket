package ru.example.invest.controller

import ru.example.invest.dto.ToolCallRequest
import ru.example.invest.dto.McpResultResponse
import ru.example.invest.service.MarketDataService
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json

@Component
@HttpController
class MarketDataController(
    private val marketDataService: MarketDataService
) {

    @Json
    @HttpRoute(method = "GET", path = "/api/mcp/tools")
    fun getTools(): McpResultResponse {
        return McpResultResponse(
            payload = marketDataService.getAvailableTools()
        )
    }

    @Json
    @HttpRoute(method = "POST", path = "/api/mcp/call")
    fun callTool(
        @Json request: ToolCallRequest
    ): McpResultResponse {
        return McpResultResponse(
            payload = marketDataService.callTool(
                request.name,
                request.argumentsJson
            )
        )
    }
}
