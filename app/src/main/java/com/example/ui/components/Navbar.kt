package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ShortsNavbar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = DarkSurface,
        contentColor = Color.White
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = RedPrimary,
            selectedTextColor = RedPrimary,
            indicatorColor = RedPrimary.copy(alpha = 0.2f),
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
        )

        NavigationBarItem(
            modifier = Modifier.testTag("nav_tab_studio"),
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Stüdyo") },
            label = { Text("Stüdyo") },
            colors = itemColors
        )

        NavigationBarItem(
            modifier = Modifier.testTag("nav_tab_preview"),
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.PlayCircle, contentDescription = "Önizleme") },
            label = { Text("Önizleme") },
            colors = itemColors
        )

        NavigationBarItem(
            modifier = Modifier.testTag("nav_tab_python"),
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.Code, contentDescription = "Python/Colab") },
            label = { Text("Python") },
            colors = itemColors
        )

        NavigationBarItem(
            modifier = Modifier.testTag("nav_tab_gallery"),
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(Icons.Default.FolderSpecial, contentDescription = "Galeri") },
            label = { Text("Galeri") },
            colors = itemColors
        )
    }
}
