package ru.example.invest.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import ru.tinkoff.kora.common.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.UUID
import ru.example.invest.config.TInvestConfig


@Component
class TInvestMcpClientImpl(
    private val config: TInvestConfig
) : TInvestMcpClient {

    private val mapper = ObjectMapper()

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    private val url = config.mcpUrl()
    private val token = config.token()

    override fun listTools(): String {
        val session = initialize()

        return send(
            method = "tools/list",
            params = emptyMap<String, Any>(),
            sessionId = session
        ).toPrettyString()
    }

    override fun callTool(
        name: String,
        argumentsJson: String
    ): String {
        val session = initialize()

        val arguments = mapper.readTree(argumentsJson)

        require(arguments.isObject) {
            "argumentsJson must be a JSON object"
        }

        return send(
            method = "tools/call",
            params = mapOf(
                "name" to name,
                "arguments" to arguments
            ),
            sessionId = session
        ).toPrettyString()
    }

    private fun initialize(): String? {
        val requestId = UUID.randomUUID().toString()

        val body = mapOf(
            "jsonrpc" to "2.0",
            "id" to requestId,
            "method" to "initialize",
            "params" to mapOf(
                "protocolVersion" to "2025-03-26",
                "capabilities" to emptyMap<String, Any>(),
                "clientInfo" to mapOf(
                    "name" to "kora-tinvest-adapter",
                    "version" to "1.0.0"
                )
            )
        )

        val response = post(body, null)
        val result = parseResponse(response.body(), requestId)

        if (result.has("error")) {
            error("MCP initialize failed: $result")
        }

        val sessionId = response.headers()
            .firstValue("Mcp-Session-Id")
            .orElse(null)

        // MCP initialization notification
        post(
            mapOf(
                "jsonrpc" to "2.0",
                "method" to "notifications/initialized"
            ),
            sessionId
        )

        return sessionId
    }

    private fun send(
        method: String,
        params: Any,
        sessionId: String?
    ): JsonNode {
        val id = UUID.randomUUID().toString()

        val response = post(
            mapOf(
                "jsonrpc" to "2.0",
                "id" to id,
                "method" to method,
                "params" to params
            ),
            sessionId
        )

        val json = parseResponse(response.body(), id)

        if (json.has("error")) {
            error("MCP error: ${json["error"]}")
        }

        return json.path("result")
    }

    private fun post(
        body: Any,
        sessionId: String?
    ): HttpResponse<String> {

        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(30))
            .header(
                "Authorization",
                "Bearer $token"
            )
            .header(
                "Content-Type",
                "application/json"
            )
            .header(
                "Accept",
                "application/json, text/event-stream"
            )
            .header(
                "MCP-Protocol-Version",
                "2025-03-26"
            )

        if (sessionId != null) {
            requestBuilder.header(
                "Mcp-Session-Id",
                sessionId
            )
        }

        val request = requestBuilder
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    mapper.writeValueAsString(body)
                )
            )
            .build()

        val response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        )

        if (response.statusCode() !in 200..299) {
            error(
                "MCP HTTP ${response.statusCode()}: " +
                        response.body().take(500)
            )
        }

        return response
    }

    private fun parseResponse(
        body: String,
        expectedId: String
    ): JsonNode {
        // MCP Streamable HTTP may return JSON or SSE.
        if (body.trimStart().startsWith("{")) {
            return mapper.readTree(body)
        }

        // Minimal SSE support: extract JSON-RPC messages
        // from data: lines.
        val events = body.lineSequence()
            .filter { it.startsWith("data:") }
            .map { it.removePrefix("data:").trim() }
            .mapNotNull {
                runCatching { mapper.readTree(it) }.getOrNull()
            }

        return events.firstOrNull {
            it.path("id").asText() == expectedId
        } ?: error("No matching MCP response received")
    }
}