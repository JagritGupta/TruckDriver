package com.example.skillbee

import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.skillbee.databinding.ActivityAudioFormBinding
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AudioFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAudioFormBinding
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var mediaPlayer: MediaPlayer
    private var retryCount = 0
    private lateinit var id: String
    private var score: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAudioFormBinding.inflate(layoutInflater)
        id = intent.getStringExtra("uuid_string").orEmpty()
        Log.d("JAGRIT", "uuid- $id")
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (isMicrophonePresent()) {
            getMicrophonePermission()
        }

        bindClicks()
    }

    override fun onBackPressed() {}

    private fun bindClicks() {
        binding.apply {
            btnStartRecording.setOnClickListener {
                startRecording()
                btnStartRecording.isVisible = false
                btnStopRecording.isVisible = true
            }

            btnStopRecording.setOnClickListener {
                stopRecording()
                btnStopRecording.isVisible = false
                btnStartRecording.isVisible = false
                llRecorder.isVisible = true
                llMediaPlayer.isVisible = true

            }

            btnRetry.setOnClickListener {
                retryRecording()
            }

            imgPausePlay.setOnClickListener {
                pausePlayAudio()
            }

            btnPlayPauseRec.setOnClickListener {
                pausePlayAudio()
            }

            imgRePlay.setOnClickListener {
                restartAudio()
            }
        }
    }

    private fun startRecording() {
        try {
            mediaRecorder = MediaRecorder()
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder.setOutputFile(getFilePath())
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder.prepare()
            mediaRecorder.start()

            Toast.makeText(this, "Recording started", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        mediaRecorder.stop()
        mediaRecorder.release()

        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(getFilePath())
        mediaPlayer.prepare()

        Toast.makeText(this, "Recording Stopped", Toast.LENGTH_LONG).show()


        uploadAudioRecordingApi2(onSuccess = {
            Log.d("JAGRIT", "Success - api2 : $it")

        }, onFailure = {
            Log.d("JAGRIT", "Failed - api2: $it")
        })

        getUrlApi3(
            onSuccess = { url ->
                Log.d("JAGRIT", "Success - api3 : $url")

                uploadFinalFormApi4(url, id, score, retryCount, onSuccess = {
                    Log.d("JAGRIT", "Success - api4")

                }, onFailure =  {
                    Log.d("JAGRIT", "Error - api4 : $it")
                })
            },
            onFailure = {
                Log.d("JAGRIT", "Error - api3 : $it")
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        )
    }


    private fun retryRecording() {
        binding.btnStopRecording.isVisible = true
        binding.btnStartRecording.isVisible = false
        binding.llRecorder.isVisible = false
        binding.llMediaPlayer.visibility = View.INVISIBLE
        retryCount += 1
        startRecording()
    }

    private fun pausePlayAudio() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            binding.imgPausePlay.setImageDrawable(
                ContextCompat.getDrawable(
                    this@AudioFormActivity,
                    R.drawable.ic_play
                )
            )

            binding.btnPlayPauseRec.setImageDrawable(
                ContextCompat.getDrawable(
                    this@AudioFormActivity,
                    R.drawable.ic_play_rec
                )
            )
        } else {
            mediaPlayer.start()
            binding.imgPausePlay.setImageDrawable(
                ContextCompat.getDrawable(
                    this@AudioFormActivity,
                    R.drawable.ic_pause
                )
            )

            binding.btnPlayPauseRec.setImageDrawable(
                ContextCompat.getDrawable(
                    this@AudioFormActivity,
                    R.drawable.ic_pause_rec
                )
            )
        }
    }

    private fun restartAudio() {
        mediaPlayer.seekTo(0)
        mediaPlayer.start()
        binding.imgPausePlay.setImageDrawable(
            ContextCompat.getDrawable(
                this@AudioFormActivity,
                R.drawable.ic_pause
            )
        )

        binding.btnPlayPauseRec.setImageDrawable(
            ContextCompat.getDrawable(
                this@AudioFormActivity,
                R.drawable.ic_pause_rec
            )
        )
    }

    private fun getFilePath(): String {
        val contextWrapper = ContextWrapper(applicationContext)
        val dir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val file = File(dir, "recordFile" + ".mp3")

        return file.path
    }

    private fun getWebMFilePath(): String {
        val contextWrapper = ContextWrapper(applicationContext)
        val dir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val file = File(dir, "recordFileWEbM" + ".webm")

        return file.path
    }

    private fun isMicrophonePresent() =
        this.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)

    private fun getMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(
                this@AudioFormActivity,
                "android.permission.RECORD_AUDIO"
            ) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(
                this@AudioFormActivity,
                "android.permission.READ_EXTERNAL_STORAGE"
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                listOf<String>(
                    "android.permission.RECORD_AUDIO",
                    "android.permission.READ_EXTERNAL_STORAGE"
                ).toTypedArray(),
                200
            )
        }
    }

    fun uploadAudioRecordingApi2(
        onSuccess: (AudioSubmitResponseData) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val webMFilePath = getWebMFilePath()
        convertToWebM(getFilePath(), webMFilePath)
        val webMFile = File(webMFilePath)
        val mediaType = MediaType.parse("audio/webm")
        val requestFile = RequestBody.create(mediaType, webMFile)
        val audioPart = MultipartBody.Part.createFormData("audio", webMFile.name, requestFile)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addPart(audioPart)
            .addFormDataPart(
                "text",
                "Hello everyone I am a truck driver from India, who wants to go to Europe for truck driver job and earn good money for me and my family. I love driving trucks and I want to travel to get more experience."
            )
            .build()

        val service = RetrofitService.apiService.submitAudioFile(
            "https://skillbee.com/api/speech-evaluator",
            requestBody,
            requestBody
        )

        // Use enqueue to perform network operations on a separate thread
        service.enqueue(object : Callback<AudioSubmitResponseData> {
            override fun onResponse(
                call: Call<AudioSubmitResponseData>,
                response: Response<AudioSubmitResponseData>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) }
                } else {
                    onFailure("FAILED")
                }
            }

            override fun onFailure(call: Call<AudioSubmitResponseData>, t: Throwable) {
                onFailure(t.localizedMessage.orEmpty())
            }
        })
    }


    private fun getUrlApi3(
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("id", id)
            .build()

        val service = RetrofitService.apiService.getHitUrl(
            "https://internationaldriversgroup.com/api/speech",
            requestBody,
        )

        // Use enqueue to perform network operations on a separate thread
        service.enqueue(object : Callback<UrlResponseData> {
            override fun onResponse(
                call: Call<UrlResponseData>,
                response: Response<UrlResponseData>
            ) {
                if (response.isSuccessful) {
                    onSuccess(response.body()?.url.orEmpty())
                } else {
                    onFailure("FAILED")
                }
            }

            override fun onFailure(call: Call<UrlResponseData>, t: Throwable) {
                onFailure(t.localizedMessage.orEmpty())
            }
        })
    }

    private fun uploadFinalFormApi4(
        url: String, id: String, score: Int, retryCount: Int, onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("id", id)
            .addFormDataPart("formNo", "2")
            .addFormDataPart("sheetName", "SpeechDriverPage")
            .addFormDataPart("score", score.toString())
            .addFormDataPart("retry_no", retryCount.toString())
            .build()

        val service = RetrofitService.apiService.submitEntireForm(
//            url,
            "https://script.google.com/macros/s/AKfycbwMVLYhsETXfhunclY7WWAbb3fL9xZdjHsvz2bpWo9Bk__Rk6WTMgU2jpQWztnyVxcBbQ/exec",
            requestBody,
            requestBody,
            requestBody,
            requestBody,
            requestBody,
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

    fun convertToWebM(inputFilePath: String, outputFilePath: String) {
        val command = "-i $inputFilePath -c:v libvpx -c:a libvorbis $outputFilePath"
        val returnCode = FFmpeg.execute(command)

        if (returnCode == Config.RETURN_CODE_SUCCESS) {
            // Conversion successful
        } else {
            // Conversion failed
        }
    }
}