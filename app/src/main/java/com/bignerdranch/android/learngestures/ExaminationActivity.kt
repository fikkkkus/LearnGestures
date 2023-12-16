package com.bignerdranch.android.learngestures

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bignerdranch.android.learngestures.db.data.GesturesEntity
import com.bignerdranch.android.learngestures.ui.theme.LearnGesturesTheme
import com.bignerdranch.android.learngestures.ui.theme.Purple40
import com.chaquo.python.Python
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors



class ExaminationActivity : ComponentActivity() {
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private var shouldShowCamera: MutableState<Boolean> = mutableStateOf(true)

    private lateinit var photoUri: Uri
    private var shouldShowPhoto: MutableState<Boolean> = mutableStateOf(false)

    private val COUNT_GESTURES = 10
    private val MAX_INDEX_GESTURE = 25
    private var GESTURES_INDEXES = (0 until MAX_INDEX_GESTURE)
        .toList()
        //.shuffled()
        .take(COUNT_GESTURES)

    var indexLetter by mutableStateOf(0)
    var thisLetter = ""

    var openDialogExit by mutableStateOf(false)
    var openDialogLoser by mutableStateOf(false)
    var openDialogWinner by mutableStateOf(false)


    var countHeartOn by mutableStateOf(3)
    var countHeartOff by mutableStateOf(0)

    var showInstructionDialog by mutableStateOf(true)

    var showCheckmark by mutableStateOf(false)
    var showCross by mutableStateOf(false)

    override fun onBackPressed() {
        openDialogExit = true
        return; super.onBackPressed()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {

            showInstructionDialog()

            val mainViewModel: MainViewModel = viewModel(factory = MainViewModel.factory)
            val itemsList = mainViewModel.getAllGestures().collectAsState(listOf())

            LearnGesturesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        TopBar(LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher)

                        MainContent(
                            shouldShowCamera,
                            outputDirectory,
                            cameraExecutor,
                            itemsList,
                            indexLetter,
                            countHeartOn,
                            countHeartOff,
                        )

                        if (openDialogExit) {
                            DialogExit(LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher)
                        }

                        if (openDialogLoser) {
                            DialogLoser(LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher) {
                                indexLetter = 0
                                countHeartOn = 3
                                countHeartOff = 0
                                openDialogLoser = false
                            }
                        }

                        if (openDialogWinner) {
                            DialogWinner()
                        }
                    }
                }
            }
        }
    }

    private fun predictGesture(image_path: String): String {
        val python = Python.getInstance()
        val pythonFile = python.getModule("prediction")
        return pythonFile.callAttr("predictGesture", image_path).toString()
    }

    private fun handleImageCapture(uri: Uri) {
        shouldShowCamera.value = false
        photoUri = uri;

        lifecycleScope.launch {
            delay(100)
            shouldShowCamera.value = true
        }

        val predict = predictGesture(image_path = photoUri.path.toString());

        //shouldShowPhoto.value = true;
        
        if (predict == thisLetter) {
            indexLetter++

            showCheckmark = true
            lifecycleScope.launch {
                delay(1500)
                showCheckmark = false
            }

        } else {
            countHeartOff++
            countHeartOn--

            showCross = true
            lifecycleScope.launch {
                delay(1500)
                showCross = false
            }
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    @Composable
    private fun showInstructionDialog() {
        if (showInstructionDialog) {
            AlertDialog(
                onDismissRequest = {
                    showInstructionDialog = false
                },
                title = {
                    Text(
                        text = "Проверка знаний",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Изобразите жест, соответствующий букве.",
                            style = TextStyle(fontSize = 20.sp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Сделайте снимок с задержкой в 3 секунды для проверки.",
                            style = TextStyle(fontSize = 20.sp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "У вас есть только 3 жизни!",
                            style = TextStyle(fontSize = 20.sp),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showInstructionDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(Color(128, 255, 128))
                    ) {
                        Text("ПОГНАЛИ")
                    }
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TopBar(onBackPressedDispatcher: OnBackPressedDispatcher) {
        CenterAlignedTopAppBar(
            modifier = Modifier.fillMaxWidth(),
            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Purple40),
            title = {
                Text(
                    text = "Режим испытания",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            navigationIcon = {
                IconButton(onClick = { openDialogExit = true }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
            },
            actions = {
                Text(
                    text = "$indexLetter/$COUNT_GESTURES",
                    fontSize = 23.sp,
                    modifier = Modifier.padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        )
    }

    @Composable
    private fun MainContent(
        shouldShowCamera: MutableState<Boolean>,
        outputDirectory: File,
        cameraExecutor: ExecutorService,
        itemsList: State<List<GesturesEntity>>,
        indexLetter: Int,
        countHeartOn: Int,
        countHeartOff: Int,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (shouldShowCamera.value) {
                CameraView(
                    outputDirectory = outputDirectory,
                    executor = cameraExecutor,
                    onImageCaptured = ::handleImageCapture,
                    onError = { Log.e("error", "View error:", it) }
                )
            }

            if (showCheckmark) {
                Checkmark()
            }

            if (showCross) {
                Cross()
            }

//            if (shouldShowPhoto.value) {
//                Image(
//                painter = rememberImagePainter(photoUri),
//                contentDescription = null,
//                modifier = Modifier.fillMaxSize()
//                )
//            }

            if (indexLetter > COUNT_GESTURES - 1) {
                openDialogWinner = true
            } else {
                itemsList.value?.let { letter ->
                    if (letter.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .align(Alignment.TopCenter),
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .align(Alignment.CenterVertically),
                                ) {
                                    thisLetter = letter[GESTURES_INDEXES[indexLetter]].name;
                                    Text(
                                        text = letter[GESTURES_INDEXES[indexLetter]].name,
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontSize = 60.sp,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .align(Alignment.CenterVertically),
                                ) {
                                    Row() {
                                        HeartsRow(countHeartOn = countHeartOn, countHeartOff = countHeartOff)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Checkmark() {
        Image(
            painter = painterResource(id = R.drawable.true1),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
        )
    }

    @Composable
    private fun Cross() {
        Image(
            painter = painterResource(id = R.drawable.false1),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
        )
    }


    @Composable
    private fun HeartsRow(countHeartOn: Int, countHeartOff: Int) {
        val painter1 = painterResource(id = R.drawable.hearton)
        val painter2 = painterResource(id = R.drawable.heartoff)

        if (countHeartOn == 0) {
            openDialogLoser = true
        }

        repeat(countHeartOn) {
            Image(
                painter = painter1,
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )
        }

        repeat(countHeartOff) {
            Image(
                painter = painter2,
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )
        }
    }

    @Composable
    private fun DialogExit(
        onBackPressedDispatcher: OnBackPressedDispatcher
    ) {
        AlertDialog(
            onDismissRequest = {
                openDialogExit = false
            },
            text = {
                Column() {
                    Text(
                        "Вы хотите выйти из режима",
                        style = TextStyle(fontSize = 20.sp)
                    )
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = "испытания?",
                        style = TextStyle(fontSize = 20.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { openDialogExit = false },
                    colors = ButtonDefaults.buttonColors(Color(128, 255, 128))
                ) {
                    Text("Нет")
                }
            },
            dismissButton = {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onBackPressedDispatcher.onBackPressed() },
                    colors = ButtonDefaults.buttonColors(Color(255, 128, 128))
                ) {
                    Text("Да")
                }
            },
        )
    }

    @Composable
    private fun DialogLoser(
        onBackPressedDispatcher: OnBackPressedDispatcher,
        onRetry: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = {
                openDialogLoser = false
            },
            text = {
                Column() {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(start = 18.dp),
                        text = "В этот раз не получилось",
                        style = TextStyle(fontSize = 20.sp)
                    )
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = "Сыграем еще раз?",
                        style = TextStyle(fontSize = 20.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onBackPressedDispatcher.onBackPressed() },
                    colors = ButtonDefaults.buttonColors(Color(255, 128, 128))
                ) {
                    Text("Нет")
                }
            },
            dismissButton = {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        GESTURES_INDEXES = (0 until MAX_INDEX_GESTURE)
                        .toList()
                        //.shuffled()
                        .take(COUNT_GESTURES);
                        onRetry() },
                    colors = ButtonDefaults.buttonColors(Color(128, 255, 128))
                ) {
                    Text("Да")
                }
            },
        )
    }

    @Composable
    private fun DialogWinner() {
        AlertDialog(
            onDismissRequest = {
                openDialogWinner = false
            },
            text = {
                Column() {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(start = 18.dp),
                        text = "Поздравляем!",
                        style = TextStyle(fontSize = 18.sp)
                    )
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(start = 20.dp),
                        text = "Вы прошли испытание!",
                        style = TextStyle(fontSize = 20.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onBackPressedDispatcher.onBackPressed() },
                    colors = ButtonDefaults.buttonColors(Color(128, 255, 128))
                ) {
                    Text("Принять поздравления")
                }
            },
        )
    }
}


