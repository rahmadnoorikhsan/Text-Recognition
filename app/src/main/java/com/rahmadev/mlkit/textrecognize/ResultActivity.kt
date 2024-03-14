package com.rahmadev.mlkit.textrecognize

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.rahmadev.mlkit.textrecognize.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))
        imageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.resultImage.setImageURI(it)
        }

        val detectedText = intent.getStringExtra(EXTRA_RESULT)
        binding.resultText.text = detectedText

        binding.translateButton.setOnClickListener {
            binding.progressIndicator.visibility = View.VISIBLE
            translateText(detectedText)
        }
    }

    private fun translateText(detectedText: String?) {
        val option = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.INDONESIAN)
            .build()
        val indonesianEnglishTranslator = Translation.getClient(option)

        val condition = DownloadConditions.Builder()
            .requireWifi().build()
        indonesianEnglishTranslator.downloadModelIfNeeded(condition)
            .addOnSuccessListener {
                indonesianEnglishTranslator.translate(detectedText.toString())
                    .addOnSuccessListener { translateText ->
                        binding.translatedText.text = translateText
                        indonesianEnglishTranslator.close()
                        binding.progressIndicator.visibility = View.GONE
                    }
                    .addOnFailureListener { exception ->
                        showToast(exception.message.toString())
                        print(exception.printStackTrace())
                        indonesianEnglishTranslator.close()
                        binding.progressIndicator.visibility = View.GONE
                    }
            }
            .addOnFailureListener { exception ->
                showToast(getString(R.string.downloading_model_fail))
                binding.progressIndicator.visibility = View.GONE
            }
        lifecycle.addObserver(indonesianEnglishTranslator)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT = "extra_result"
    }
}