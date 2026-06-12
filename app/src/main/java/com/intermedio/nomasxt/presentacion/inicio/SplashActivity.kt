package com.intermedio.nomasxt.presentacion.inicio

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.intermedio.nomasxt.ui.theme.NoMasXTTheme
import com.intermedio.nomasxt.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoMasXTTheme {
                Surface(modifier = Modifier.fillMaxSize()){
                    SplashScreenContenido(onNavegarAMain = { navegarAMain() })

/*
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val context = LocalContext.current

                        val painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(context)
                                .data("file:///android_asset/splash_gif.gif")
                                .decoderFactory(GifDecoder.Factory())
                                .build()
                        )

                        Image(
                            painter = painter,
                            contentDescription = "No Mas XT",
                            modifier = Modifier.fillMaxSize()

                        )

                        LaunchedEffect(Unit) {
                            delay(3000)
                            startActivity(Intent(context, MainActivity::class.java))
                            finish()
                        }
                    }
*/
                }
            }
        }
    }

    private fun navegarAMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
private fun SplashScreenContenido(onNavegarAMain: () -> Unit) {
    //Obtiene el viewModel usando HiltViewModel
    val viewModel: SplashViewModel = hiltViewModel()

    val estanCargadosLosDatos by viewModel.estanCargadosLosDatos.collectAsState()

    //Dispara una acción basada en el estado estanCargadosLosDatos
    LaunchedEffect(key1 = estanCargadosLosDatos) {
        if(estanCargadosLosDatos) {
            Log.d("NoMasXT", "Estuvieron cargados los datos")
            //Si están cargados los datos llama a la acción de navegación a Principal
            onNavegarAMain()
        }
    }

    //La UI que teníamos.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val context = LocalContext.current

        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/splash_gif.gif")
                .decoderFactory(GifDecoder.Factory())
                .build()
        )

        Image(
            painter = painter,
            contentDescription = "No Mas XT",
            modifier = Modifier.fillMaxSize()

        )
    }
}
