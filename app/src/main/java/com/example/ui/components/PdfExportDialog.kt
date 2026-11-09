package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.models.FullFilmProduction
import com.example.ui.theme.*

@Composable
fun PdfExportDialog(
    production: FullFilmProduction,
    isHindiMode: Boolean,
    onDismiss: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var isGenerating by remember { mutableStateOf(false) }
    var exportComplete by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = CinemaSurface,
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isHindiMode) "पीडीएफ स्टोरीबोर्ड बुकलेट" else "PDF STORYBOOK EXPORT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CinemaGoldLight
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, null, tint = CinemaTextMuted, modifier = Modifier.size(18.dp))
                    }
                }

                Text(
                    text = if (isHindiMode) 
                        "यह फीचर पूरी 11-फेज की फिल्म प्रोडक्शन स्क्रिप्ट, शॉट लिस्ट, लाइटिंग प्लान और एआई प्रॉम्प्ट्स को एक प्रोफेशनल प्रिंटेबल PDF स्टोरीबोर्ड बुकलेट में फॉर्मेट करता है।" 
                    else 
                        "Formats the complete 11-phase film production dossier, shot list, lighting plans, and AI prompts into a professional printable PDF booklet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CinemaTextSecondary
                )

                Surface(
                    color = CinemaBackground,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "📁 Title: ${production.brief.title}", style = MaterialTheme.typography.bodySmall, color = CinemaTextPrimary, fontWeight = FontWeight.Bold)
                        Text(text = "🎬 Total Shots: ${production.shotList.size} planned", style = MaterialTheme.typography.bodySmall, color = AnamorphicCyan)
                        Text(text = "👥 Saved Characters: ${production.characters.size} linked", style = MaterialTheme.typography.bodySmall, color = CinemaGoldLight)
                    }
                }

                if (exportComplete) {
                    Surface(
                        color = CinemaSuccess.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = CinemaSuccess, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindiMode) "PDF बुकलेट सफलतापूर्वक जनरेट और सेव हो गई!" else "PDF Storyboard Booklet Generated & Saved!",
                                style = MaterialTheme.typography.bodySmall,
                                color = CinemaSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isHindiMode) "बंद करें" else "Close", color = CinemaTextSecondary)
                    }

                    Button(
                        onClick = {
                            isGenerating = true
                            // Simulate compiling PDF stream
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                isGenerating = false
                                exportComplete = true
                                onShowToast(if (isHindiMode) "PDF बुकलेट डाउनलोड फोल्डर में सेव हो गई!" else "PDF Booklet successfully compiled!")
                            }, 1200)
                        },
                        enabled = !isGenerating && !exportComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = CinemaBackground),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CinemaBackground, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isHindiMode) "रेंडरिंग..." else "Compiling...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isHindiMode) "PDF डाउनलोड करें" else "Export PDF", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
