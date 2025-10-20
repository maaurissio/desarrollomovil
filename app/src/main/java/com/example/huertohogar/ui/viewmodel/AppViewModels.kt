package com.example.huertohogar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar.data.model.CartItem
import com.example.huertohogar.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing the state and logic for the Product List screen.
 */
class ProductViewModel : ViewModel() {

    // A private mutable state flow to hold the search query.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // A private list of all available products (mock data).
    // In a real app, this would be fetched from a repository.
    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())

    // A public state flow that emits a filtered list of products based on the search query.
    val filteredProducts = searchQuery.combine(_allProducts) { query, products ->
        if (query.isBlank()) {
            products
        } else {
            products.filter {
                it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
            }
        }
    }

    init {
        // Load mock products when the ViewModel is created.
        loadProducts()
    }

    /**
     * Updates the search query state.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /**
     * Simulates loading products from a data source.
     */
    private fun loadProducts() {
        viewModelScope.launch {
            // In a real app, you would fetch this from an API or local database.
            _allProducts.value = listOf(
                Product("1", "Laptop Gamer Pro", "Potente laptop para juegos con RTX 4080", 1899.99, "https://placehold.co/600x400/5E5E5E/white?text=Laptop"),
                Product("2", "Smartphone Pixel 8", "El último smartphone con la mejor cámara", 999.50, "https://placehold.co/600x400/3E3E3E/white?text=Smartphone"),
                Product("3", "Auriculares Inalámbricos", "Cancelación de ruido y sonido Hi-Fi", 249.00, "https://placehold.co/600x400/7E7E7E/white?text=Auriculares"),
                Product("4", "Teclado Mecánico RGB", "Teclado con switches cherry-mx red", 120.00, "https://placehold.co/600x400/4E4E4E/white?text=Teclado"),
                Product("5", "Monitor Curvo 4K", "Monitor de 32 pulgadas para máxima inmersión", 750.80, "https://placehold.co/600x400/6E6E6E/white?text=Monitor"),
                Product("6", "Mouse Ergonómico", "Mouse inalámbrico con diseño ergonómico", 65.00, "https://placehold.co/600x400/8E8E8E/white?text=Mouse")
            )
        }
    }
}

/**
 * ViewModel responsible for managing the state and logic of the Shopping Cart.
 */
class CartViewModel : ViewModel() {
    // Private mutable state flow to hold the list of items in the cart.
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    /**
     * Adds a product to the cart. If the product already exists, it increases its quantity.
     * This simulates persistence within the ViewModel's lifecycle. For real persistence,
     * you would save this data to SharedPreferences, DataStore, or a Room database.
     */
    fun addProductToCart(product: Product) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItem = currentItems.find { it.product.id == product.id }

        if (existingItem != null) {
            existingItem.quantity++
        } else {
            currentItems.add(CartItem(product = product, quantity = 1))
        }
        _cartItems.value = currentItems
    }

    /**
     * Removes a CartItem from the cart.
     */
    fun removeProductFromCart(cartItem: CartItem) {
        val currentItems = _cartItems.value.toMutableList()
        currentItems.remove(cartItem)
        _cartItems.value = currentItems
    }

    /**
     * Updates the quantity of a specific item in the cart.
     */
    fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeProductFromCart(cartItem)
        } else {
            val currentItems = _cartItems.value.toMutableList()
            val itemToUpdate = currentItems.find { it.product.id == cartItem.product.id }
            itemToUpdate?.let {
                it.quantity = newQuantity
            }
            _cartItems.value = currentItems
        }
    }

    /**
     * Simulates sending the final sale to a backend.
     * In a real app, this function would make a network request (e.g., using Retrofit)
     * to an API endpoint to register the sale in a database.
     */
    fun checkout() {
        viewModelScope.launch {
            val saleData = _cartItems.value
            if (saleData.isNotEmpty()) {
                println("Enviando venta al backend: $saleData")
                // Here you would call your repository/API service to send the data.
                // For example: `salesRepository.registerSale(saleData)`
                _cartItems.value = emptyList() // Clear cart after successful checkout
            } else {
                println("El carro está vacío, no se puede registrar la venta.")
            }
        }
    }
}
