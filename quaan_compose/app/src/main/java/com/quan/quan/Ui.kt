package com.quan.quan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 全局字体：苹方黑体·纤细（res/font/pingfang_thin.ttf） */
val AppFontFamily = FontFamily(
    Font(R.font.pingfang_thin, FontWeight.Thin),
    Font(R.font.pingfang_thin, FontWeight.Light),
    Font(R.font.pingfang_thin, FontWeight.Normal),
    Font(R.font.pingfang_thin, FontWeight.Medium),
    Font(R.font.pingfang_thin, FontWeight.SemiBold),
    Font(R.font.pingfang_thin, FontWeight.Bold)
)

/** 深色强调色（浅色主题=近黑，深色主题=近白） */
@Composable
fun accentInk(): Color =
    if (themeIsLight()) Color(0xFF1C1C1E) else Color(0xFFF2F2F2)

/** 深色底上应该用的文字色 */
@Composable
fun onAccentInk(): Color =
    if (themeIsLight()) Color.White else Color(0xFF1C1C1E)

@Composable
fun themeIsLight(): Boolean = MaterialTheme.colorScheme.background.luminance() > 0.5f

private fun Color.luminance(): Float = 0.299f * red + 0.587f * green + 0.114f * blue

@Composable
fun CardC(content: @Composable ColumnScope.() -> Unit) {
    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun CardTitle(t: String) {
    Text(t, fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface)
}

@Composable
fun Body(t: String, color: Color = Color.Unspecified, size: Int = 13) {
    Text(
        t,
        fontSize = size.sp,
        lineHeight = (size + 5).sp,
        color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else color
    )
}

@Composable
fun KvRow(k: String, v: String, vColor: Color = Color.Unspecified) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(k, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f))
        Text(
            v, fontSize = 14.sp, fontWeight = FontWeight.Medium,
            color = if (vColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else vColor
        )
    }
}

/** 上下结构的信息行：标签一行、值一行（长文本不会挤压） */
@Composable
fun StackedRow(label: String, value: String, vColor: Color = Color.Unspecified) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(
            value, fontSize = 15.sp, fontWeight = FontWeight.Medium,
            lineHeight = 21.sp,
            color = if (vColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else vColor
        )
    }
}

@Composable
fun HairLine() {
    Box(
        Modifier.fillMaxWidth().height(1.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
fun DarkButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(54.dp)
            .background(
                if (enabled) accentInk() else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(22.dp)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
            color = if (enabled) onAccentInk() else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LightButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(50.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface)
    }
}

/** 单选卡片：选中 = 深底 + 白字 + 实心圆 */
@Composable
fun OptionRadio(title: String, sub: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) accentInk() else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) onAccentInk() else MaterialTheme.colorScheme.onSurface
    Row(
        Modifier.fillMaxWidth()
            .background(bg, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(22.dp).border(2.dp, fg, CircleShape)
            )
            if (selected) Box(Modifier.size(9.dp).background(fg, CircleShape))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = fg, fontWeight = FontWeight.Medium)
            if (sub.isNotEmpty()) {
                Text(sub, fontSize = 12.5.sp, lineHeight = 17.sp, color = fg.copy(alpha = 0.65f))
            }
        }
    }
}

@Composable
fun ChipC(text: String, color: Color) {
    Box(
        Modifier.background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 12.sp, color = color)
    }
}

/** 图标行：左图标 + 标题 + 副标题 */
@Composable
fun IconRow(title: String, sub: String, icon: ImageVector) {
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface)
            if (sub.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(sub, fontSize = 13.sp, lineHeight = 19.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** 简单的空行占位 */
@Composable
fun VGap(h: Int) {
    Spacer(Modifier.height(h.dp))
}

/** 分组小标题 */
@Composable
fun SectionLabel(t: String) {
    Text(t, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.defaultMinSize(minHeight = 18.dp))
}
