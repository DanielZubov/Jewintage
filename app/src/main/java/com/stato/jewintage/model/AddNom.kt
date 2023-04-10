package com.stato.jewintage.model

import java.io.Serializable

data class AddNom(
    val category: String? = null,
    val description: String? = null,
    val price: String? = null,
    val date: String? = null,
    val quantity: String? = null,
    val mainImage: String? = null,
    val image2: String? = null,
    val image3: String? = null,
    val id: String? = null,
    val uid: String? = null
) : Serializable
