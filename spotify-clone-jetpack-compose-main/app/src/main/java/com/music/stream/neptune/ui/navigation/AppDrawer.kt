package com.music.stream.neptune.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.music.stream.neptune.ui.theme.AppBackground
import com.music.stream.neptune.ui.theme.AppPalette

data class DrawerItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

private val drawerItems = listOf(
    DrawerItem(Icons.Default.Home, "Home", Routes.Home.route),
    DrawerItem(Icons.Default.Search, "Search", Routes.Search.route),
    DrawerItem(Icons.Default.LibraryMusic, "Your Library", Routes.Library.route),
    DrawerItem(Icons.Default.Podcasts, "Podcasts", Routes.Podcast.route),
    DrawerItem(Icons.Default.Radio, "Radio", Routes.Radio.route),
    DrawerItem(Icons.Default.MusicNote, "Available Tracks", Routes.AvailableTracks.route),
)

@Composable
fun AppDrawer(
    currentRoute: String?,
    navController: NavController,
    onClose: () -> Unit
) {
    val bgColor = Color(AppBackground.toArgb())
    val accentColor = Color(AppPalette.toArgb())

    ModalDrawerSheet(
        drawerContainerColor = Color(0xFF0D0D18),
        modifier = Modifier.width(280.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(Color(0xFF0D0D18))
                .statusBarsPadding()
        ) {
            // App branding header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 24.dp, 24.dp, 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Neptune",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Music Streaming",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }

            Divider(
                color = Color(0xFF2A2A3A),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(12.dp))

            // Navigation items
            drawerItems.forEach { item ->
                val isSelected = currentRoute == item.route
                DrawerNavItem(
                    item = item,
                    isSelected = isSelected,
                    accentColor = accentColor,
                    onClick = {
                        onClose()
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(Routes.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }

            Spacer(Modifier.weight(1f))

            // Footer
            Divider(
                color = Color(0xFF2A2A3A),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Neptune v1.0",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun DrawerNavItem(
    item: DrawerItem,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Selected indicator bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isSelected) accentColor else Color.Transparent)
        )
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (isSelected) accentColor else Color.Gray,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = item.label,
            color = if (isSelected) Color.White else Color(0xFFAAAAAA),
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
