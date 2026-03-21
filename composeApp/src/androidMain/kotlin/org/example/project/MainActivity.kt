package org.example.project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import org.example.project.auth.AuthRepositoryImpl
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var audioImporter: AudioImporter
    private lateinit var audioPlayer: AudioPlayer
    private lateinit var audioExporter: AudioExporter

    private lateinit var authRepo: AuthRepositoryImpl


    private val googleSignInLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)

                account?.idToken?.let { token ->
                    lifecycleScope.launch {
                        authRepo.firebaseAuthWithGoogle(token)
                    }
                }

            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContextHolder.context = applicationContext


        AudioImporter.currentActivity = this

        audioImporter = AudioImporter()
        audioPlayer = AudioPlayer(this)
        authRepo = AuthRepositoryImpl(this)
        audioExporter = AudioExporter(this)
        val context = this
        val settingsManager = SettingsManager(context)


        setContent {
            App(
                audioPlayer = audioPlayer,
                authRepository = authRepo,
                audioImporter = audioImporter,

                onGoogleSignInClick = {
                    val intent = authRepo.googleSignInClient.signInIntent
                    googleSignInLauncher.launch(intent)
                },
                    audioExporter = audioExporter,
                settingsManager = settingsManager

            )
        }
        requestMicPermission()
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        audioImporter.onActivityResult(requestCode, resultCode, data)
    }
    private fun requestMicPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                2001
            )
        }
    }
}