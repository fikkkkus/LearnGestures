package com.bignerdranch.android.learngestures

import android.content.res.AssetManager
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import coil.size.Scale
import com.bignerdranch.android.learngestures.db.data.GesturesEntity
import com.bignerdranch.android.learngestures.ui.theme.LearnGesturesTheme
import com.bignerdranch.android.learngestures.ui.theme.Purple40


class GestureGuideActivity:  ComponentActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LearnGesturesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GestureGuideScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureGuideTopAppBar(
    onBackPressedDispatcher: OnBackPressedDispatcher,
    title: String
) {
    CenterAlignedTopAppBar(
        modifier = Modifier
            .fillMaxWidth(),
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Purple40),
        title = {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            IconButton(onClick = { onBackPressedDispatcher.onBackPressed() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Назад")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureGuideScreen(
    mainViewModel: MainViewModel = viewModel(factory = MainViewModel.factory)
) {

    val itemsList = mainViewModel.getAllGestures().collectAsState(listOf())

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {

            val context = LocalContext.current
            val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher

            GestureGuideTopAppBar(
                onBackPressedDispatcher = onBackPressedDispatcher,
                title = "Cправочник жестов"
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(itemsList.value) {gestureItem ->
                    GestureCard(gestureItem, context.assets)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureCard(gestureItem: GesturesEntity, assetManager: AssetManager) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = gestureItem.name,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(end = 8.dp),
                    fontSize = 60.sp,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            GesturePhotoPlaceholder(gestureItem.path, assetManager)
        }
    }
}


@OptIn(ExperimentalCoilApi::class)
@Composable
fun GesturePhotoPlaceholder(path: String, assetManager: AssetManager) {
    val imageUrl = "file:///android_asset/gestures/$path"
    val painter = rememberImagePainter(
        data = imageUrl,
        builder = {
            scale(Scale.FILL)
        }
    )

    if (painter.state is ImagePainter.State.Error) {
        Column(
            modifier = Modifier
                .size(130.dp)
        ) {
            Text(
                text = "Coming",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(top = 16.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                text = "Soon",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.CenterHorizontally)

            )
        }
    } else {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(130.dp)
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.primary)
                .scale(1.3f)
        )
    }
}


