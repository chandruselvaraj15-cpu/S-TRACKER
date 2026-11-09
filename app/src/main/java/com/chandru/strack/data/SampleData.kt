package com.chandru.strack.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private val sampleSales = listOf(
    SaleEntity(brand = "Samsung", model = "Galaxy A17 5G", quantity = 1, timestampMillis = System.currentTimeMillis() - 86400000L),
    SaleEntity(brand = "Samsung", model = "Galaxy A26 5G", quantity = 1, timestampMillis = System.currentTimeMillis() - 86400000L + 1800000L),
    SaleEntity(brand = "Vivo", model = "V50", quantity = 1, timestampMillis = System.currentTimeMillis() - 86400000L + 3600000L),
    SaleEntity(brand = "Vivo", model = "Y31", quantity = 1, timestampMillis = System.currentTimeMillis() - 86400000L + 5400000L),
    SaleEntity(brand = "OPPO", model = "K13", quantity = 1, timestampMillis = System.currentTimeMillis() - 86400000L + 7200000L),
    SaleEntity(brand = "realme", model = "P4", quantity = 1, timestampMillis = System.currentTimeMillis() - 86400000L + 9000000L)
)

suspend fun seedDebugSampleData(database: STrackDatabase) = withContext(Dispatchers.IO) {
    val dao = database.saleDao()
    val current = dao.observeAll().first()
    if (current.isEmpty()) {
        sampleSales.forEach { dao.insert(it) }
    }
}
