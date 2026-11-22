package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookmarkEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.Obsidian
import com.example.ui.theme.PureWhite

@Composable
fun StartPageContent(
    profile: UserProfileEntity?,
    bookmarks: List<BookmarkEntity>,
    safeBrowsingEnabled: Boolean,
    onOpenUrl: (String) -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Full-bleed green rolling hills & sunlit meadow wallpaper matching the screenshot
        MeadowLandscapeBackdrop(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Header matching the right phone in the screenshot:
            // [K]  Браузер K  ...  [Profile/Shield]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    onClick = onOpenProfile,
                    shape = CircleShape,
                    color = PureWhite.copy(alpha = 0.22f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("start_logo_badge"),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "K",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }

                Text(
                    text = "Браузер K",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite,
                    fontWeight = FontWeight.SemiBold,
                )

                Surface(
                    onClick = onOpenProfile,
                    shape = CircleShape,
                    color = PureWhite.copy(alpha = 0.22f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .size(38.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("start_profile_button"),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val initials = buildProfileInitials(profile)
                        if (initials.isNotBlank() && initials != "K") {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.labelMedium,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Профиль ID K",
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // White floating card(s) matching the screenshot's top quick-access tiles:
            // Featured sole site: http://sitefortress.ru/
            val featuredBookmarks = if (bookmarks.isNotEmpty()) {
                bookmarks
            } else {
                listOf(
                    BookmarkEntity(
                        id = 1L,
                        title = "SiteFortress",
                        url = "http://sitefortress.ru/",
                        shortBadge = "SF",
                        accentColorHex = 0xFF0EA5E9,
                    )
                )
            }

            if (featuredBookmarks.size == 1) {
                val site = featuredBookmarks.first()
                SiteFortressFeaturedTile(
                    site = site,
                    onClick = { onOpenUrl(site.url) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 720.dp),
                )
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 720.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(featuredBookmarks, key = { it.id }) { item ->
                        CompactQuickTile(
                            bookmark = item,
                            onClick = { onOpenUrl(item.url) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtle translucent info strip under the cards (mirroring city/weather row in screenshot)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = PureWhite.copy(alpha = 0.92f),
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (safeBrowsingEnabled) "Защита активна" else "Защита выкл.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PureWhite.copy(alpha = 0.92f),
                        fontWeight = FontWeight.Medium,
                    )
                }

                val locationOrUser = profile?.city?.takeIf { it.isNotBlank() }
                    ?: profile?.displayName?.takeIf { it.isNotBlank() }
                    ?: "ID K"
                Text(
                    text = locationOrUser,
                    style = MaterialTheme.typography.bodySmall,
                    color = PureWhite.copy(alpha = 0.92f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SiteFortressFeaturedTile(
    site: BookmarkEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = PureWhite.copy(alpha = 0.96f),
        shadowElevation = 6.dp,
        modifier = modifier.testTag("bookmark_tile_SF"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(site.accentColorHex)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = site.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Obsidian,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Оформить домен • Полностью бесплатно",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF475569),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ElectricSky.copy(alpha = 0.14f),
            ) {
                Text(
                    text = "Открыть",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF0284C7),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun CompactQuickTile(
    bookmark: BookmarkEntity,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = PureWhite.copy(alpha = 0.96f),
        shadowElevation = 5.dp,
        modifier = Modifier
            .width(114.dp)
            .height(78.dp)
            .testTag("bookmark_tile_${bookmark.shortBadge}"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(bookmark.accentColorHex)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = bookmark.shortBadge,
                    style = MaterialTheme.typography.labelLarge,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = bookmark.title,
                style = MaterialTheme.typography.labelMedium,
                color = Obsidian,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MeadowLandscapeBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Crisp sky gradient (top third matching the blue-azure sky in screenshot)
        drawRect(
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFF2177C7),
                    0.22f to Color(0xFF3E95DC),
                    0.38f to Color(0xFF7BC0EC),
                    0.48f to Color(0xFFA9DCB5),
                    1.0f to Color(0xFF2D6A1E),
                )
            )
        )

        // Distant forest horizon ridge
        val farHill = Path().apply {
            moveTo(0f, h * 0.42f)
            cubicTo(w * 0.25f, h * 0.36f, w * 0.55f, h * 0.44f, w, h * 0.34f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = farHill,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF1F5422), Color(0xFF2F7225)),
                startY = h * 0.34f,
                endY = h * 0.72f,
            )
        )

        // Mid sunlit rolling meadow hill
        val midHill = Path().apply {
            moveTo(0f, h * 0.46f)
            cubicTo(w * 0.35f, h * 0.52f, w * 0.72f, h * 0.40f, w, h * 0.48f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = midHill,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF77BC2D), Color(0xFF4F941E)),
                startY = h * 0.42f,
                endY = h * 0.85f,
            )
        )

        // Foreground lush green slope with warm sunlight glow
        val frontHill = Path().apply {
            moveTo(0f, h * 0.62f)
            cubicTo(w * 0.30f, h * 0.56f, w * 0.68f, h * 0.66f, w, h * 0.58f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = frontHill,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF6BB324), Color(0xFF2F6E12)),
                startY = h * 0.56f,
                endY = h,
            )
        )

        // Soft sunlit highlights & stylized tree clusters along the ridge
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x38D9F99D), Color.Transparent),
                center = Offset(w * 0.38f, h * 0.56f),
                radius = w * 0.45f,
            ),
            center = Offset(w * 0.38f, h * 0.56f),
            radius = w * 0.45f,
        )
        drawCircle(
            color = Color(0xFF22571C),
            radius = w * 0.11f,
            center = Offset(w * 0.18f, h * 0.58f),
        )
        drawCircle(
            color = Color(0xFF266220),
            radius = w * 0.085f,
            center = Offset(w * 0.27f, h * 0.60f),
        )
        drawCircle(
            color = Color(0xFF235B1D),
            radius = w * 0.12f,
            center = Offset(w * 0.84f, h * 0.52f),
        )
    }
}

fun buildProfileInitials(profile: UserProfileEntity?): String {
    if (profile == null) return "K"
    val first = profile.firstName.firstOrNull()?.uppercaseChar()
    val last = profile.lastName.firstOrNull()?.uppercaseChar()
    if (first != null && last != null) return "$first$last"
    if (first != null) return "$first"
    return profile.displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "K"
}
