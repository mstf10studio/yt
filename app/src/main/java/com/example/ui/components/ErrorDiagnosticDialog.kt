package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.LogEntry
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RedLight
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextSecondary

@Composable
fun ErrorDiagnosticDialog(
    logs: List<LogEntry>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
                .padding(8.dp)
                .testTag("error_diagnostic_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Hata Denetimi",
                        tint = RedLight
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sıkı Hata & Tanılama Monitörü",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "Sistemdeki API, stok video ve ses sentezleme adımlarının canlı tanı günlükleri:",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(DarkBackground, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    if (logs.isEmpty()) {
                        item {
                            Text(
                                text = "Henüz sistem günlüğü bulunmuyor.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        items(logs) { log ->
                            LogItemRow(log = log)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_close_diagnostic"),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Anladım & Kapat", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun LogItemRow(log: LogEntry) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (log.isError) RedPrimary.copy(alpha = 0.15f) else DarkSurface
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (log.isError) Icons.Default.Error else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (log.isError) RedLight else StatusGreen,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = "[${log.tag}]",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (log.isError) RedLight else StatusGreen
                )
            }
            Text(
                text = log.message,
                fontSize = 13.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp)
            )

            log.suggestion?.let { sug ->
                Text(
                    text = "💡 Öneri: $sug",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
