package com.stato.jewintage

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.stato.jewintage.adapters.AddRcAdapter
import com.stato.jewintage.adapters.ImageAdapter
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
        val addNom = intent.getSerializableExtra("addNom") as AddNom
        fillImageArray(addNom)
    }

    private fun fillImageArray(addNom: AddNom){
        val listUris = listOf(
            addNom.mainImage,
            addNom.image2,
            addNom.image3,
        )
        CoroutineScope(Dispatchers.Main).launch {
            val bitMapList = ImageManager.getBitmapForUris(listUris)
            adapter.update(bitMapList as ArrayList<Bitmap>)
        }
    }
}