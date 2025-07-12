package com.ap.watchtogive.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavigation(
    currentRoute: String?,
    onNavSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Color(0xFF1C1C1E),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomNavIcon(
            route = Screen.CharityStats.route,
            icon = Icons.Default.AccountCircle,
            contentDescription = "Stats",
            isSelected = currentRoute == Screen.CharityStats.route,
            onIconClick = onNavSelected,
        )

        BottomNavIcon(
            route = Screen.CharityWatch.route,
            icon = Icons.Default.PlayArrow,
            contentDescription = "Watch",
            isSelected = currentRoute == Screen.CharityWatch.route,
            onIconClick = onNavSelected,
        )

        BottomNavIcon(
            route = Screen.CharitySelect.route,
            icon = Icons.Default.Search,
            contentDescription = "Select",
            isSelected = currentRoute == Screen.CharitySelect.route,
            onIconClick = onNavSelected,
        )
    }
}

@Composable
private fun BottomNavIcon(
    route: String,
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onIconClick: (String) -> Unit,
) {
    IconButton(
        onClick = { onIconClick(route) },
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) Color.White else Color.Gray,
        )
    }
}
