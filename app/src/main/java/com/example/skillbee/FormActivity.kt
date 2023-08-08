package com.example.skillbee

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.skillbee.databinding.ActivityFormBinding
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.S)
class FormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFormBinding
    private var id = UUID.randomUUID().toString()
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityFormBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bindClicks()
    }

    private fun bindClicks() {
        binding.apply {
            button.setOnClickListener {
                val name = etFullName.editText?.text.toString()
                val phone = etPhoneNumber.editText?.text.toString()
                val state = etState.editText?.text.toString()
                val years = etExperience.editText?.text.toString()
                val country = etCountry.editText?.text.toString()

                progressBar.isVisible = true
                scrollView.isVisible = false

                if (name.isNotEmpty() && phone.isNotEmpty() && state.isNotEmpty() && years.isNotEmpty() && country.isNotEmpty()) {
                    postFormProcess(
                        formData = FormData(
                            name,
                            phone,
                            state,
                            years,
                            id = id,
                            Content = country
                        ),
                        onSuccess = {
                            val intent = Intent(this@FormActivity, AudioFormActivity::class.java)
                            intent.putExtra("uuid_string", id)
                            startActivity(intent)
                        },
                        onFailure = { error ->
                            Toast.makeText(this@FormActivity, error, Toast.LENGTH_LONG).show()
                        }
                    )
                } else {
                    Toast.makeText(
                        this@FormActivity,
                        "Input Fields cannot be empty",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        }
    }

    private fun postFormProcess(
        formData: FormData = FormData(),
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("Name", formData.Name)
            .addFormDataPart("Phone", formData.Phone)
            .addFormDataPart("State", formData.State)
            .addFormDataPart("Experience", formData.Experience)
            .addFormDataPart("id", formData.id)
            .addFormDataPart("sheetName", formData.sheetName)
            .addFormDataPart("Content", formData.Content)
            .addFormDataPart("Param", formData.Param)
            .addFormDataPart("URL", formData.URL)
            .build()

        val service = RetrofitService.apiService.postFormData(
            "https://script.google.com/macros/s/AKfycbxF0wmA4OobTK1JuIvse27v8T76brtSwOV25a7TtPcKf6fihpz_MdEGeku-vK5pne3Z1w/exec",
            requestBody,
            requestBody,
            requestBody,
            requestBody,
            requestBody,
            requestBody,
            requestBody,
            requestBody
        )

        // Use enqueue to perform network operations on a separate thread
        service.enqueue(object : Callback<FormResponseData> {
            override fun onResponse(
                call: Call<FormResponseData>,
                response: Response<FormResponseData>
            ) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure("FAILED")
                }
            }

            override fun onFailure(call: Call<FormResponseData>, t: Throwable) {
                onFailure(t.localizedMessage.orEmpty())
            }
        })
    }
}
