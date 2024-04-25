package com.vigyat.myapplication

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TextExtractor(private val context: Context) {

    fun extractText(finalImage: InputImage, progressBar: ProgressBar, extractedText: TextView) {

        progressBar.visibility = View.VISIBLE
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        CoroutineScope(Dispatchers.IO).launch {

            try {
                val result = recognizer.process(finalImage).await()
                withContext(Dispatchers.Main) {

                    if (result.text.isEmpty()) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, "No text found in the image", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        progressBar.visibility = View.GONE
                        extractedText.text = result.text
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        context,
                        "Error processing image: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("Taggy", "cannot extract the text...", e)
                }
            }
        }
    }
}