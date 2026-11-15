package ru.example.invest.config

import ru.tinkoff.kora.config.common.annotation.ConfigSource

@ConfigSource("t-invest")
interface TInvestConfig {
    fun mcpUrl(): String
    fun token(): String
}