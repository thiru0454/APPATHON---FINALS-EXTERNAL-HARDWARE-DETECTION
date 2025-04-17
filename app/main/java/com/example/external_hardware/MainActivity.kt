package com.example.external_hardware

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }

        setContent {
            ExternalHardwareApp()
        }
    }
}

@Composable
fun ExternalHardwareApp() {
    var showMainScreen by remember { mutableStateOf(false) }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6A11CB),
                            Color(0xFF2575FC)
                        )
                    )
                )
        ) {
            if (showMainScreen) {
                DeviceListView()
            } else {
                GetStartedScreen {
                    showMainScreen = true
                }
            }
        }
    }
}

@Composable
fun GetStartedScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Devices,
            contentDescription = "App Icon",
            modifier = Modifier.size(120.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Device Monitor",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Discover all your connected devices in one place",
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.White.copy(alpha = 0.9f)),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF2575FC)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Get Started", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun DeviceListView() {
    val context = LocalContext.current
    var devicesList by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(Unit) {
        devicesList = fetchConnectedDevices(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Connected Devices",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
        )

        if (devicesList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            LazyColumn {
                items(devicesList) { device ->
                    DeviceCard(device = device)
                }
            }
        }
    }
}

@Composable
fun DeviceCard(device: String) {
    val icon = when {
        device.contains("Wi-Fi") -> Icons.Default.Wifi
        device.contains("Bluetooth") -> Icons.Default.Bluetooth
        device.contains("USB") -> Icons.Default.Usb
        else -> Icons.Default.Devices
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF2575FC),
                modifier = Modifier.size(24.dp).padding(end = 16.dp)
            )
            Text(
                text = device,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// Keep your existing fetchConnectedDevices function exactly the same
fun fetchConnectedDevices(context: Context): List<String> {
    val connectedDevices = mutableListOf<String>()

    // Add Wi-Fi information
    try {
        val wifiManager = ContextCompat.getSystemService(context, WifiManager::class.java)
        wifiManager?.let {
            val wifiInfo: WifiInfo = it.connectionInfo
            val ssid = wifiInfo.ssid.replace("\"", "")
            val ipAddress = android.text.format.Formatter.formatIpAddress(wifiInfo.ipAddress)
            connectedDevices.add("📶 Wi-Fi Network: $ssid")
            connectedDevices.add("🖥 IP Address: $ipAddress")
        }
    } catch (e: Exception) {
        connectedDevices.add("⚠ Wi-Fi info unavailable: ${e.localizedMessage}")
    }

    // Add Bluetooth devices
    try {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null) {
                connectedDevices.add("❌ Bluetooth not supported")
            } else {
                connectedDevices.add("\n📱 Local Bluetooth: ${bluetoothAdapter.name ?: "Unknown"}")

                if (!bluetoothAdapter.isEnabled) {
                    connectedDevices.add("🔴 Bluetooth is disabled")
                } else {
                    val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
                    if (pairedDevices.isEmpty()) {
                        connectedDevices.add("🔵 No paired Bluetooth devices")
                    } else {
                        connectedDevices.add("\n🟢 Paired Bluetooth Devices:")
                        pairedDevices.forEach { device ->
                            connectedDevices.add("🔹 ${device.name ?: "Unknown"} (${device.address})")
                        }
                    }
                }
            }
        } else {
            connectedDevices.add("\n⚠ Bluetooth permission not granted")
        }
    } catch (e: Exception) {
        connectedDevices.add("⚠ Bluetooth info unavailable: ${e.localizedMessage}")
    }

    // Add USB devices
    try {
        val usbManager = ContextCompat.getSystemService(context, android.hardware.usb.UsbManager::class.java)
        usbManager?.let {
            val deviceList = it.deviceList
            if (deviceList.isEmpty()) {
                connectedDevices.add("\n🔌 No USB devices connected")
            } else {
                connectedDevices.add("\n🖥 Connected USB Devices:")
                deviceList.values.forEach { device ->
                    connectedDevices.add("🔹 ${device.productName ?: "USB Device"} (Vendor: ${device.vendorId})")
                }
            }
        }
    } catch (e: Exception) {
        connectedDevices.add("⚠ USB info unavailable: ${e.localizedMessage}")
    }

    return connectedDevices
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ExternalHardwareApp()
}