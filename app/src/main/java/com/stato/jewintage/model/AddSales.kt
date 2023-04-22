package com.stato.jewintage.model

import java.io.Serializable

data class AddSales(
    val category: String? = null,
    val description: String? = null,
    val price: String? = null,
    val date: String? = null,
    val mainImage: String? = null,
    val image2: String? = null,
    val image3: String? = null,
    val soldQuantity: String? = null,
    val paymentMethod: String? = null,
    val id: String? = null,
    val uid: String? = null,
    var idItem: String? = null
) : Serializable {

    // Добавьте функцию для преобразования AddSales в Map
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "category" to category,
            "description" to description,
            "price" to price,
            "date" to date,
            "mainImage" to mainImage,
            "image2" to image2,
            "image3" to image3,
            "soldQuantity" to soldQuantity,
            "paymentMethod" to paymentMethod,
            "id" to id,
            "uid" to uid,
            "idItem" to idItem
        )
    }
}


