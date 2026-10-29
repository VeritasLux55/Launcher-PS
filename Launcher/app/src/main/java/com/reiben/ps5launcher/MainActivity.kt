
package com.reiben.ps5launcher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

private val PsBlue = Color(0xFF0755C9)
private val PsDark = Color(0xFF071426)
private val PsCard = Color(0xFF132844)
private val PsWhite = Color(0xFFF4F7FF)
private val PsGray = Color(0xFF9EADC2)

data class LauncherApp(
    val name: String,
    val packageName: String,
    val resolveInfo: ResolveInfo
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PsDark
                ) {
                    LauncherScreen()
                }
            }
        }
    }
}

@Composable
fun LauncherScreen() {
    val context = LocalContext.current

    var apps by remember {
        mutableStateOf<List<LauncherApp>>(emptyList())
    }

    var selectedTab by remember {
        mutableStateOf("Inicio")
    }

    LaunchedEffect(Unit) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        apps = context.packageManager
            .queryIntentActivities(intent, 0)
            .map { resolveInfo ->
                LauncherApp(
                    name = resolveInfo.loadLabel(
                        context.packageManager
                    ).toString(),
                    packageName = resolveInfo.activityInfo.packageName,
                    resolveInfo = resolveInfo
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.name.lowercase() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PsDark)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PsDark)
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PS",
                color = PsWhite,
                fontSize = 27.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "5",
                color = PsBlue,
                fontSize = 27.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "LAUNCHER",
                color = PsGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "●",
                color = Color(0xFF4DE2A8),
                fontSize = 13.sp
            )
        }

        Column(
            modifier = Modifier.padding(
                horizontal = 22.dp
            )
        ) {
            Text(
                text = "Bienvenido",
                color = PsWhite,
                fontSize = 29.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Tu centro de juegos y aplicaciones",
                color = PsGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    "Inicio",
                    "Juegos",
                    "Aplicaciones"
                ).forEach { tab ->
                    val active = selectedTab == tab

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (active) PsBlue
                                else PsCard,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedTab = tab
                            }
                            .padding(
                                horizontal = 13.dp,
                                vertical = 11.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = PsWhite,
                            fontSize = 12.sp,
                            fontWeight = if (active) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = when (selectedTab) {
                    "Juegos" -> "TUS JUEGOS"
                    "Aplicaciones" -> "TUS APLICACIONES"
                    else -> "CONTINUAR"
                },
                color = PsWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            val visibleApps = when (selectedTab) {
                "Juegos" -> apps.filter {
                    isLikelyGame(it.name)
                }

                "Aplicaciones" -> apps.filterNot {
                    isLikelyGame(it.name)
                }

                else -> apps
            }

            if (visibleApps.isEmpty()) {
                Text(
                    text = if (apps.isEmpty()) {
                        "Buscando aplicaciones..."
                    } else {
                        "No se encontraron aplicaciones en esta sección."
                    },
                    color = PsGray,
                    fontSize = 14.sp
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(
                        items = visibleApps,
                        key = { it.packageName }
                    ) { app ->
                        AppCard(
                            app = app,
                            onClick = {
                                val launchIntent =
                                    context.packageManager
                                        .getLaunchIntentForPackage(
                                            app.packageName
                                        )

                                if (launchIntent != null) {
                                    context.startActivity(launchIntent)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "ACCESO RÁPIDO",
                color = PsWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            QuickAction(
                title = "Todos los juegos",
                subtitle = "Explora las aplicaciones identificadas como juegos",
                symbol = "△",
                onClick = {
                    selectedTab = "Juegos"
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            QuickAction(
                title = "Todas las aplicaciones",
                subtitle = "Consulta las aplicaciones disponibles en el teléfono",
                symbol = "□",
                onClick = {
                    selectedTab = "Aplicaciones"
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PS5 LAUNCHER  •  ANDROID",
                modifier = Modifier.fillMaxWidth(),
                color = PsGray,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
fun AppCard(
    app: LauncherApp,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .width(145.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .background(
                    PsCard,
                    RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { viewContext ->
                    ImageView(viewContext).apply {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        setPadding(22, 22, 22, 22)
                    }
                },
                update = { imageView ->
                    imageView.setImageDrawable(
                        app.resolveInfo.loadIcon(
                            context.packageManager
                        )
                    )
                },
                modifier = Modifier.size(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = app.name,
            color = PsWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            lineHeight = 17.sp
        )

        Text(
            text = "Abrir aplicación",
            color = PsGray,
            fontSize = 11.sp
        )
    }
}

@Composable
fun QuickAction(
    title: String,
    subtitle: String,
    symbol: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                PsCard,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    PsBlue,
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol,
                color = PsWhite,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = PsWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = PsGray,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        Text(
            text = "›",
            color = PsWhite,
            fontSize = 28.sp
        )
    }
}

private fun isLikelyGame(name: String): Boolean {
    val gameKeywords = listOf(
        "game",
        "dragon ball",
        "legends",
        "clash",
        "call of duty",
        "minecraft",
        "roblox",
        "pubg",
        "free fire",
        "genshin",
        "fortnite",
        "play games"
    )

    return gameKeywords.any {
        name.contains(it, ignoreCase = true)
    }
}
