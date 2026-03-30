package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutPage(onClose: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)) // dim background
    ) {

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.85f)
                .background(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    1.dp,
                    Color.Black.copy(alpha = 0.15f),
                    RoundedCornerShape(20.dp)
                )
                .padding(30.dp, 20.dp, 30.dp, 30.dp)
                .verticalScroll(rememberScrollState()), // scroll support
            horizontalAlignment = Alignment.Start
        ) {

            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "About",
                fontSize = 22.sp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "TAAL is a modern music creation app designed to help users compose beats, experiment with sounds, and export their creations effortlessly.",
                color = Color.LightGray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Modes",
                color = Color.White,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "• Beginner – Simple and intuitive interface for quick music creation\n" +
                        "• Intermediate – More control over beats and editing\n" +
                        "• Advanced – Full access to all features and customization",
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Why TAAL?",
                color = Color.White,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "• User-friendly and easy to navigate\n" +
                        "• Real-time beat creation and playback\n" +
                        "• Multiple instruments and editors\n" +
                        "• Export to WAV and MIDI formats",
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Support",
                color = Color.White,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "For issues, feedback, or contributions, visit our GitHub repository:",
                color = Color.Gray,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "https://github.com/Tanishq172006/TAAL",
                color = Color(0xFF64B5F6),
                fontSize = 13.sp,
                modifier = Modifier.clickable {
                    openUrl("https://github.com/Tanishq172006/TAAL")
                }
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Version 1.0.0",
                color = Color.White,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "Made with ❤️ by Tanishq, Ananya, Anshul and Lavanya(TAAL)",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}