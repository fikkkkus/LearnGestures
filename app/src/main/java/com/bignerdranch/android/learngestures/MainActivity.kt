package com.bignerdranch.android.learngestures

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.bignerdranch.android.learngestures.ui.theme.LearnGesturesTheme
import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class MainActivity : ComponentActivity() {

    private var shouldShowCamera: MutableState<Boolean> = mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            shouldShowCamera.value = true
        }
    }

    var openError by mutableStateOf(false)

    private fun initPython() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
    }

    private fun requestCameraPermission() {

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                shouldShowCamera.value = true
            }
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initPython()

        super.onCreate(savedInstanceState)
        setContent {
            LearnGesturesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MenuScreen(this)
                }
            }
        }
    }

    @Composable
    fun Header() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Learn",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                )
            )
            Text(
                text = "Gestures",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                )
            )
        }
    }

    @Composable
    fun MenuButton(icon: ImageVector, text: String, onClick: () -> Unit) {
        Button(
            onClick = {
                if (text == "Испытание") {
                    onClick()
                }
                else{
                   openError = true;
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null)
                Text(text)
            }
        }
    }


    @Composable
    fun LearnGestureButton(onClick: () -> Unit) {
        MenuButton(Icons.Default.School, "Научиться различать жесты", onClick)
    }

    @Composable
    fun ShowGestureButton(onClick: () -> Unit) {
        MenuButton(Icons.Default.Tv, "Научиться показывать жесты", onClick)
    }

    @Composable
    fun ExaminationButton(activity: MainActivity) {
        MenuButton(Icons.Default.Whatshot, "Испытание") {
            if (shouldShowCamera.value == true) {
                val intent = Intent(activity, ExaminationActivity::class.java)
                activity.startActivity(intent)
            } else {
                    requestCameraPermission()
                    if (shouldShowCamera.value == true) {
                        val intent = Intent(activity, ExaminationActivity::class.java)
                        activity.startActivity(intent)
                    }
            }
        }
    }

    @Composable
    fun GuideButton(activity: MainActivity) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    val intent = Intent(activity, GestureGuideActivity::class.java)
                    activity.startActivity(intent)
                },
                modifier = Modifier
                    .height(50.dp)
            ) {
                Text("Справочник жестов")
            }
        }
    }

    @Composable
    fun Unavailable(){
        AlertDialog(
            onDismissRequest = {
                openError = false
            },
            text = {
                Column() {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(start = 18.dp),
                        text = "Функционал в разработке",
                        style = TextStyle(fontSize = 18.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { openError = false },
                    colors = ButtonDefaults.buttonColors(Color(128, 255, 128))
                ) {
                    Text("ОК")
                }
            },
        )
    }

    @Composable
    fun MenuScreen(activity: MainActivity) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            if (openError == true) {
                Unavailable()
            }

            Header()

            Spacer(modifier = Modifier.height(36.dp))

            LearnGestureButton {

            }

            Spacer(modifier = Modifier.height(8.dp))

            ShowGestureButton {

            }

            Spacer(modifier = Modifier.height(8.dp))

            ExaminationButton(activity)

            Spacer(modifier = Modifier.weight(0.1f))

            GuideButton(activity)
        }
    }
}







