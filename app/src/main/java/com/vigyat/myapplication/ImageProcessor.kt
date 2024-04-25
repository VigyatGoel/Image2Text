package com.vigyat.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import java.io.File
import java.io.FileOutputStream

class ImageProcessor(private val context: Context) {

    fun bitmapToInputImage(bitmap: Bitmap): InputImage {
        return InputImage.fromBitmap(bitmap, 0) // Assuming 0 degrees rotation
    }

    fun isBitmap(image: Any): Boolean {
        return image is Bitmap
    }

    fun saveBitmapAndGetUri(bitmap: Bitmap): Uri {
        val file = File(context.externalCacheDir, "${System.currentTimeMillis()}.jpg")
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.close()
        return Uri.fromFile(file)
    }

    fun optimizeImage(bitmap: Bitmap): Bitmap {
        // Resize the bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 1024, 768, true)
        // Convert to grayscale
        val grayScaleBitmap =
            Bitmap.createBitmap(scaledBitmap.width, scaledBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayScaleBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter
        canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
        return grayScaleBitmap
    }
}