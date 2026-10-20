package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Satellite
import com.example.data.model.Transponder
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceVariant
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLock
import com.example.ui.theme.GoldSat
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TranspondersScreen(
    satellites: List<Satellite>,
    selectedSatellite: Satellite,
    onSelectSatellite: (Satellite) -> Unit,
    onToggleFavorite: (Satellite) -> Unit,
    onAddCustomSatellite: (String, Double, String, Int, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") } // "All", "C-Band", "Ku-Band", "Favorites"
    var expandedSatId by remember { mutableStateOf<Int?>(selectedSatellite.id) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredSatellites = remember(satellites, searchQuery, selectedCategory) {
        satellites.filter { sat ->
            val matchesQuery = sat.name.contains(searchQuery, ignoreCase = true) ||
                sat.popularChannels.contains(searchQuery, ignoreCase = true) ||
                sat.orbitalPositionFormatted.contains(searchQuery, ignoreCase = true) ||
                sat.transponders.any { tp -> tp.toDisplayText().contains(searchQuery) }

            val matchesCategory = when (selectedCategory) {
                "C-Band" -> sat.band.contains("C-Band")
                "Ku-Band" -> sat.band.contains("Ku-Band")
                "Favorites" -> sat.isFavorite
                else -> true
            }

            matchesQuery && matchesCategory
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SpaceBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CyanAccent,
                contentColor = SpaceBlack,
                modifier = Modifier.testTag("add_custom_sat_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Custom Satellite")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by satellite, channel, or TP...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanAccent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tp_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface,
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = BorderCyan,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Favorites", "C-Band", "Ku-Band").forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) CyanAccent else CardSurface,
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) SpaceBlack else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Satellites with Transponders List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSatellites, key = { it.id }) { sat ->
                    val isSelected = sat.id == selectedSatellite.id
                    val isExpanded = expandedSatId == sat.id

                    SatelliteTransponderCard(
                        satellite = sat,
                        isSelected = isSelected,
                        isExpanded = isExpanded,
                        onSelect = { onSelectSatellite(sat) },
                        onToggleExpand = {
                            expandedSatId = if (isExpanded) null else sat.id
                        },
                        onToggleFavorite = { onToggleFavorite(sat) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomSatelliteDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, pos, band, lnb, dish, strongTp, notes ->
                onAddCustomSatellite(name, pos, band, lnb, dish, strongTp, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SatelliteTransponderCard(
    satellite: Satellite,
    isSelected: Boolean,
    isExpanded: Boolean,
    onSelect: () -> Unit,
    onToggleExpand: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sat_card_${satellite.id}"),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(14.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, CyanAccent) else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CardSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Satellite,
                        contentDescription = null,
                        tint = if (isSelected) CyanAccent else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = satellite.name,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = if (satellite.band.contains("C-Band")) GoldSat.copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = satellite.orbitalPositionFormatted,
                                color = if (satellite.band.contains("C-Band")) GoldSat else CyanAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "${satellite.band} • LNB L.O: ${satellite.defaultLnbLo} MHz",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (satellite.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (satellite.isFavorite) GoldSat else TextMuted
                    )
                }

                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Strong TP Banner
            Surface(
                color = CardSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        clipboardManager.setText(AnnotatedString(satellite.strongTransponder))
                        Toast.makeText(context, "Copied TP: ${satellite.strongTransponder}", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = GoldSat.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("STRONG TP", color = GoldSat, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = satellite.strongTransponder,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Quick Select / Align Action
            if (!isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = SpaceBlack),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select for Live Alignment", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Expanded Transponders & Details Section
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                // Recommended Dish & Coverage
                Text("Recommended Dish: ${satellite.recommendedDishSize}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(satellite.coverageNotes, color = TextMuted, fontSize = 11.sp)

                if (satellite.popularChannels.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Channels: ${satellite.popularChannels}", color = TextSecondary, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Transponders (Frequencies):", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    satellite.transponders.forEach { tp ->
                        TransponderItemRow(tp = tp)
                    }
                }
            }
        }
    }
}

@Composable
fun TransponderItemRow(tp: Transponder) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Surface(
        color = SpaceBlack,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val copyText = "${tp.frequencyMhz} ${tp.polarization} ${tp.symbolRate}"
                clipboardManager.setText(AnnotatedString(copyText))
                Toast.makeText(context, "Copied: $copyText", Toast.LENGTH_SHORT).show()
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${tp.frequencyMhz} ${tp.polarization} ${tp.symbolRate}",
                        color = if (tp.isStrong) GoldSat else CyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${tp.standard} • FEC ${tp.fec}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                if (tp.description.isNotBlank()) {
                    Text(text = tp.description, color = TextSecondary, fontSize = 11.sp)
                }
            }

            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy TP",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun AddCustomSatelliteDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, pos: Double, band: String, lnb: Int, dish: String, strongTp: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var positionStr by remember { mutableStateOf("") }
    var isEast by remember { mutableStateOf(true) }
    var band by remember { mutableStateOf("Ku-Band") }
    var strongTp by remember { mutableStateOf("") }
    var dishSize by remember { mutableStateOf("2ft - 3ft Offset") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SpaceBlack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Add Custom Satellite", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Satellite Name (e.g. PakSat 2)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = BorderCyan
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = positionStr,
                        onValueChange = { positionStr = it },
                        label = { Text("Orbital Position (e.g. 52.5)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderCyan
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isEast) CyanAccent else CardSurface,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .clickable { isEast = !isEast }
                    ) {
                        Text(
                            text = if (isEast) "°EAST" else "°WEST",
                            color = if (isEast) SpaceBlack else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = strongTp,
                    onValueChange = { strongTp = it },
                    label = { Text("Strong TP (e.g. 11843 V 27500)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = BorderCyan
                    )
                )

                Button(
                    onClick = {
                        val posVal = positionStr.toDoubleOrNull() ?: 0.0
                        val finalPos = if (isEast) posVal else -posVal
                        val lnbLo = if (band.contains("C-Band")) 5150 else 9750
                        onAdd(name.ifBlank { "Custom Satellite" }, finalPos, band, lnbLo, dishSize, strongTp, notes)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldLock, contentColor = SpaceBlack),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Satellite", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
