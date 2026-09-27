package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.MyraViewModel
import com.example.ui.screens.AutomationAppsScreen
import com.example.ui.screens.ConnectIntentsScreen
import com.example.ui.screens.CoreHudScreen
import com.example.ui.screens.MemoryFilesScreen
import com.example.ui.screens.PrivacySosScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.MyraBorder
import com.example.ui.theme.MyraCardBg
import com.example.ui.theme.MyraCyberCyan
import com.example.ui.theme.MyraDeepBlack
import com.example.ui.theme.MyraMatrixGreen
import com.example.ui.theme.MyraNeonGreen
import com.example.ui.theme.MyraTextSecondary

sealed class MyraNavTab(val index: Int, val title: String, val icon: ImageVector, val tag: String) {
    object Core : MyraNavTab(0, "Core", Icons.Default.GraphicEq, "nav_core")
    object Automation : MyraNavTab(1, "Automation", Icons.Default.Apps, "nav_automation")
    object Connect : MyraNavTab(2, "Connect", Icons.Default.Public, "nav_connect")
    object Vault : MyraNavTab(3, "Vault", Icons.Default.Folder, "nav_vault")
    object Security : MyraNavTab(4, "Security", Icons.Default.Security, "nav_security")
}

class MainActivity : ComponentActivity() {
    private val viewModel: MyraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ ->
                    viewModel.refreshTelemetry()
                }

                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                MyraMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MyraMainApp(viewModel: MyraViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    // Handle back button: if not on Core tab (0), return to Core tab
    BackHandler(enabled = selectedTab != 0) {
        viewModel.setSelectedTab(0)
    }

    val tabs = listOf(
        MyraNavTab.Core,
        MyraNavTab.Automation,
        MyraNavTab.Connect,
        MyraNavTab.Vault,
        MyraNavTab.Security
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MyraDeepBlack),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MyraBorder, RoundedCornerShape(16.dp))
            ) {
                NavigationBar(
                    containerColor = MyraCardBg,
                    tonalElevation = 0.dp
                ) {
                    tabs.forEach { tab ->
                        val isSelected = selectedTab == tab.index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedTab(tab.index) },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF031A0D),
                                selectedTextColor = MyraNeonGreen,
                                indicatorColor = MyraNeonGreen,
                                unselectedIconColor = MyraTextSecondary,
                                unselectedTextColor = MyraTextSecondary
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }
        },
        containerColor = MyraDeepBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> CoreHudScreen(viewModel = viewModel)
                1 -> AutomationAppsScreen(viewModel = viewModel)
                2 -> ConnectIntentsScreen(viewModel = viewModel)
                3 -> MemoryFilesScreen(viewModel = viewModel)
                4 -> PrivacySosScreen(viewModel = viewModel)
            }
        }
    }
}
