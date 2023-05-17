package com.stato.jewintage.model


data class Category(
    var id: String? = null,
    var uid: String? = null,
    var name: String? = null,
    var commission: Float? = null,
    var commissionCard: Float? = null,
    var commissionCash: Float? = null
)