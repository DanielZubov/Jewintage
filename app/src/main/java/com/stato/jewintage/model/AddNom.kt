package com.stato.jewintage.model

import java.io.Serializable

data class AddNom(
    val category: String? = null,
    val description: String? = null,
    val price: String? = null,
    val sum: String? = null,
    val date: String? = null,
    var quantity: String? = null,
    val mainImage: String? = null,
    val id: String? = null,
    val uid: String? = null
) : Serializable
