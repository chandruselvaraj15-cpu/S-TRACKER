package com.chandru.strack.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chandru.strack.data.SaleEntity
import com.chandru.strack.data.SaleRepository
import kotlinx.coroutines.flow.Flow

class SalesViewModel(private val repository: SaleRepository) : ViewModel() {
    val sales: Flow<List<SaleEntity>> = repository.observeSales()

    suspend fun addSale(brand: String, model: String, quantity: Int) {
        if (brand.isBlank() || model.isBlank() || quantity <= 0) return
        repository.addSale(
            SaleEntity(
                brand = brand.trim(),
                model = model.trim(),
                quantity = quantity,
                timestampMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteSale(id: Long) {
        repository.deleteSale(id)
    }
}

class SalesViewModelFactory(
    private val repository: SaleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SalesViewModel(repository) as T
        }
        error("Unknown view model class: ${modelClass.name}")
    }
}
