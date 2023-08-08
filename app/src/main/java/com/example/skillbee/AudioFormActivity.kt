package com.example.skillbee

import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.skillbee.databinding.ActivityAudioFormBinding
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

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
    }

    private fun retryRecording() {
        binding.btnStopRecording.isVisible = true
        binding.btnStartRecording.isVisible = false
        binding.llRecorder.isVisible = false
        binding.llMediaPlayer.visibility = View.INVISIBLE

//        mediaRecorder.reset()
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

    private fun playAudio() {
        Toast.makeText(this, "Playing Audio", Toast.LENGTH_LONG).show()

        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(getFilePath())
        mediaPlayer.prepare()
        mediaPlayer.start()
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

    fun uploadFileToApi() {
        val webMFilePath = getWebMFilePath()
        convertToWebM(getFilePath(), webMFilePath)
        val webMFile = File(webMFilePath)
        val mediaType = MediaType.parse("audio/webm")
        val requestFile = RequestBody.create(mediaType, webMFile)
        val audioPart = MultipartBody.Part.createFormData("audio", webMFile.name, requestFile)

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