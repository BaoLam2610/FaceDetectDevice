package com.ml.quaterion.facenetdetection.detect

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ml.quaterion.facenetdetection.BitmapUtils
import com.ml.quaterion.facenetdetection.FrameAnalyserV2
import com.ml.quaterion.facenetdetection.databinding.ActivityDetectBinding
import com.ml.quaterion.facenetdetection.model.FaceNetModel
import com.ml.quaterion.facenetdetection.model.Models

class DetectActivity : AppCompatActivity(), FrameAnalyserV2.OnFaceResult {

    private lateinit var binding: ActivityDetectBinding
    private lateinit var frameAnalyser: FrameAnalyserV2
    private lateinit var faceNetModel: FaceNetModel

    private val useGpu = true
    private val useXNNPack = true
    private val modelInfo = Models.FACENET

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetectBinding.inflate(layoutInflater)
        setContentView(binding.root)


        faceNetModel = FaceNetModel(this, modelInfo, useGpu, useXNNPack)
        frameAnalyser = FrameAnalyserV2(this, this)
        /*val imageFrameAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(480, 640))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
        imageFrameAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), frameAnalyser)*/

        binding.button.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        Manifest.permission.READ_MEDIA_IMAGES
                    else Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestGalleryPermission()
            } else {
                doOpenGallery()
            }
        }
    }

    private fun requestGalleryPermission() {
        galleryPermissionLauncher.launch(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_IMAGES
            else Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private val galleryPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                doOpenGallery()
            } else {
                val alertDialog = AlertDialog.Builder(this).apply {
                    setTitle("Gallery Permission")
                    setMessage("The app couldn't function without the gallery permission.")
                    setCancelable(false)
                    setPositiveButton("ALLOW") { dialog, which ->
                        dialog.dismiss()
                        requestGalleryPermission()
                    }
                    setNegativeButton("CLOSE") { dialog, which ->
                        dialog.dismiss()
                        finish()
                    }
                    create()
                }
                alertDialog.show()
            }

        }

    fun doOpenGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        if (intent.resolveActivity(packageManager) != null) {
            pickImageLauncher.launch(Intent.createChooser(intent, "Select Picture"))
        }
    }

    val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                if (it.data != null) {
                    frameAnalyser.processImage(it.data!!)
                }
            }
        }
    }

    override fun onResult(bitmap: ArrayList<Bitmap>) {
        bitmap.forEach {
//            val i = Intent(Intent.ACTION_SEND)
//            i.type = "image/*"
            BitmapUtils.saveMediaToStorage(it, this) { uri ->
//                i.putExtra(Intent.EXTRA_STREAM, uri)
            }
//            startActivity(Intent.createChooser(i, "Shared"))
        }
    }
}