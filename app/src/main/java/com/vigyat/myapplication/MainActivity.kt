package com.vigyat.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.google.mlkit.vision.common.InputImage
import com.vigyat.myapplication.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var image: ImageView
    private lateinit var addPhotoBtn: ImageView
    private lateinit var extractTextBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var extractedText: TextView
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var imageUri: Uri
    private lateinit var imageBitmap: Bitmap
    private lateinit var takePictureLauncher: ActivityResultLauncher<Void?>
    private lateinit var getContent: ActivityResultLauncher<String>
    private var imageProcessor = ImageProcessor(this)
    private var textExtractor = TextExtractor(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        image = mainBinding.imageView
        addPhotoBtn = mainBinding.addPhotoBtn
        extractTextBtn = mainBinding.extractTextBtn
        progressBar = mainBinding.progressBar
        extractedText = mainBinding.extractedTextTV

        progressBar.visibility = View.GONE

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                image.setImageURI(uri)
                imageUri = uri
                addPhotoBtn.visibility = View.GONE
            }
        }

        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
                if (bitmap != null) {
                    image.setImageBitmap(bitmap)
                    imageUri = imageProcessor.saveBitmapAndGetUri(bitmap)
                    addPhotoBtn.visibility = View.GONE
                }
            }


        extractTextBtn.setOnClickListener {
            try {

                val finalImage: InputImage = if (imageProcessor.isBitmap(image)) {
                    val optimizedImage = imageProcessor.optimizeImage(imageBitmap)
                    imageProcessor.bitmapToInputImage(optimizedImage)
                } else {
                    InputImage.fromFilePath(this, imageUri)
                }
                textExtractor.extractText(finalImage, progressBar, extractedText)

            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
        addPhotoBtn.setOnClickListener {
            try {
                showImagePickDialog()
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }


    }

    private fun showImagePickDialog() {
        val options = arrayOf("Gallery", "Camera")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Image From")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> getContent.launch("image/*") // Gallery option
                1 -> {
                    if (ContextCompat.checkSelfPermission(
                            this, Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.CAMERA),
                            CAMERA_PERMISSION_REQUEST_CODE
                        )
                    } else {
                        takePictureLauncher.launch(null) // Camera option
                    }
                }
            }
        }
        builder.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    takePictureLauncher.launch(null)
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
                return
            }

            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


}