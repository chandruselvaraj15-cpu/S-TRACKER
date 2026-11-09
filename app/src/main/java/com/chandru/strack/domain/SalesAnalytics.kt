package com.chandru.strack.domain

import com.chandru.strack.data.SaleEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class SalesStats(
    val ftdSales: Int,
    val mtdSales: Int,
    val storeTotal: Int,
    val samsungSales: Int,
    val samsungShare: Double
)

data class BrandBreakdown(
    val brand: String,
    val units: Int,
    val share: Double
)

fun summarizeSales(sales: List<SaleEntity>): SalesStats {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)

    val ftdSales = sales.filter {
        Instant.ofEpochMilli(it.timestampMillis).atZone(zone).toLocalDate() == today
    }.sumOf { it.quantity }

    val mtdSales = sales.filter {
        val localDate = Instant.ofEpochMilli(it.timestampMillis).atZone(zone).toLocalDate()
        localDate.year == today.year && localDate.month == today.month
    }.sumOf { it.quantity }

    val samsungSales = sales.filter {
        val localDate = Instant.ofEpochMilli(it.timestampMillis).atZone(zone).toLocalDate()
        localDate.year == today.year && localDate.month == today.month && it.brand.equals("Samsung", ignoreCase = true)
    }.sumOf { it.quantity }

    val total = mtdSales
    val samsungShare = if (total <= 0) 0.0 else (samsungSales.toDouble() / total.toDouble()) * 100.0

    return SalesStats(
        ftdSales = ftdSales,
        mtdSales = mtdSales,
        storeTotal = total,
        samsungSales = samsungSales,
        samsungShare = samsungShare
    )
}

fun summarizeBrands(sales: List<SaleEntity>): List<BrandBreakdown> {
    val grouped = sales.groupBy { it.brand.trim().ifBlank { "Unknown" } }
        .mapValues { (_, entries) -> entries.sumOf { it.quantity } }
        .entries
        .sortedByDescending { it.value }

    val totalUnits = grouped.sumOf { it.value }
    return grouped.map { (brand, units) ->
        BrandBreakdown(
            brand = brand,
            units = units,
            share = if (totalUnits <= 0) 0.0 else (units.toDouble() / totalUnits.toDouble()) * 100.0
        )
    }
}
