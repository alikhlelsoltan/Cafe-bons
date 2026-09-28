package com.smartmenu.cafeapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartmenu.cafeapp.ui.theme.DarkCard
import com.smartmenu.cafeapp.ui.theme.GoldPrimary
import com.smartmenu.cafeapp.ui.theme.TextGray
import com.smartmenu.cafeapp.ui.theme.TextWhite

enum class TopNavTab {
    MENU,
    CASHIER,
    SETTINGS
}

@Composable
fun TopNavHeader(
    currentTab: TopNavTab,
    pendingOrdersCount: Int,
    onMenuClick: () -> Unit,
    onCashierClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Settings Gear Button on Left
        val isSettingsActive = currentTab == TopNavTab.SETTINGS
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSettingsActive) GoldPrimary else DarkCard)
                .border(
                    width = 1.dp,
                    color = if (isSettingsActive) GoldPrimary else Color(0xFF2C2C2E),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onSettingsClick() }
                .testTag("top_nav_settings_btn"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "الإعدادات والضبط",
                tint = if (isSettingsActive) Color.Black else GoldPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Segmented Switcher on Right: [ الكاشير (1) | مينيو ]
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF1C1C1E))
                .border(1.dp, Color(0xFF2C2C2E), RoundedCornerShape(26.dp))
                .padding(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Menu Pill
                val isMenuActive = currentTab == TopNavTab.MENU
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isMenuActive) GoldPrimary else Color.Transparent)
                        .clickable { onMenuClick() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("top_nav_menu_tab"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = if (isMenuActive) Color.Black else TextGray,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "مينيو",
                        color = if (isMenuActive) Color.Black else TextWhite,
                        fontSize = 13.sp,
                        fontWeight = if (isMenuActive) FontWeight.Bold else FontWeight.Medium
                    )
                }

                // Cashier Pill
                val isCashierActive = currentTab == TopNavTab.CASHIER
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isCashierActive) GoldPrimary else Color.Transparent)
                        .clickable { onCashierClick() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("top_nav_cashier_tab"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Red Count Badge
                    if (pendingOrdersCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color(0xFFEF4444), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pendingOrdersCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = "الكاشير",
                        color = if (isCashierActive) Color.Black else TextWhite,
                        fontSize = 13.sp,
                        fontWeight = if (isCashierActive) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
