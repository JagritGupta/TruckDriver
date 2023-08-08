package com.example.skillbee

import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
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
import java.io.FileOutputStream
import java.nio.ByteBuffer

class AudioFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAudioFormBinding
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAudioFormBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (isMicrophonePresent()) {
            getMicrophonePermission()
        }

        bindClicks()
    }

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


        uploadAudioRecording(onSuccess = {
            Log.d("JAGRIT", "Success" + it.toString())
        }, onFailure = {
            Log.d("JAGRIT", it)
        })
    }

    private fun retryRecording() {
        binding.btnStopRecording.isVisible = true
        binding.btnStartRecording.isVisible = false
        binding.llRecorder.isVisible = false
        binding.llMediaPlayer.visibility = View.INVISIBLE

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

    fun uploadAudioRecording(
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