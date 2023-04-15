package com.stato.jewintage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.stato.jewintage.adapters.ImageAdapter
import com.stato.jewintage.constance.MainConst
import com.stato.jewintage.databinding.ActivityDescriptionBinding
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.util.ImageManager

class DescriptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityDescriptionBinding
    lateinit var adapter: ImageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init(){
        adapter = ImageAdapter()
        binding.apply {
            vpDes.adapter = adapter
        }
        getIntentFromMainAct()
    }

    private fun getIntentFromMainAct(){
        val ad = intent.getSerializableExtra(MainConst.AD) as AddNom
        updateUi(ad)
    }

    private fun updateUi(ad: AddNom){
        ImageManager.fillImageArray(ad, adapter)
        fillTextViews(ad)
    }

    private fun fillTextViews(ad: AddNom) = with(binding){
        tvDesCat.text = ad.category
        "₾ ${ad.price}".also { tvDesPrcie.text = it }
        tvDesDesc.text = ad.description
        tvDesDate.text = ad.date
        "${ad.quantity} шт.".also { tvDesQuantity.text = it }
        "₾ ${getSumPriceItem(ad.price, ad.quantity)}".also { tvSum.text = it }
    }

    private fun getSumPriceItem(price: String?, quantity: String?): Int {
        val parsedPrice = price?.toIntOrNull() ?: 0
        val parsedQuantity = quantity?.toIntOrNull() ?: 0
        return parsedPrice * parsedQuantity
    }


}