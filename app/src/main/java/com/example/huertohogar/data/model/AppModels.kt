package com.example.huertohogar.data.model

/**
 * Represents a product in the store.
 *
 * @property id A unique identifier for the product.
 * @property name The name of the product.
 * @property description A brief description of the product.
 * @property price The price of the product.
 * @property imageUrl The URL for the product's image.
 */
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String
)

/**
 * Represents an item within the shopping cart.
 * It holds a product and the quantity of that product selected by the user.
 *
 * @property product The product details.
 * @property quantity The number of units of the product in the cart.
 */
data class CartItem(
    val product: Product,
    var quantity: Int
)
