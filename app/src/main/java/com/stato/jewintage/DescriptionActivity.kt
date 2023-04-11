package com.stato.jewintage

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.stato.jewintage.adapters.AddRcAdapter
import com.stato.jewintage.adapters.ImageAdapter
import com.stato.jewintage.constance.MainConst
import com.stato.jewintage.databinding.ActivityDescriptionBinding
import com.stato.jewintage.model.AddNom
import com.stato.jewintage.util.ImageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        fillImageArray(ad)
        fillTextViews(ad)
    }

    private fun fillTextViews(ad: AddNom) = with(binding){
        tvDesCat.text = ad.category
        tvDesPrcie.text = ad.price
        tvDesDesc.text = ad.description
        tvDesDate.text = ad.date
        tvDesQuantity.text = ad.quantity
        tvSum.text = getSumPriceItem(ad.price, ad.quantity).toString()
    }

    private fun getSumPriceItem(price: String?, quantity: String?): Int {
        val parsedPrice = price?.toIntOrNull() ?: 0
        val parsedQuantity = quantity?.toIntOrNull() ?: 0
        return parsedPrice * parsedQuantity
    }

    private fun fillImageArray(ad: AddNom){
        val listUris = listOf(
            ad.mainImage,
            ad.image2,
            ad.image3,
        )

        CoroutineScope(Dispatchers.Main).launch {
            val bitMapList = ImageManager.getBitmapForUris(listUris)
            adapter.update(bitMapList as ArrayList<Bitmap>)
        }
    }
}