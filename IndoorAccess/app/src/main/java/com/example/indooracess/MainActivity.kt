package com.example.indooracess

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.indooracess.ui.theme.IndoorAcessTheme
import kotlin.math.log


val TAG : String = "WIFI"
lateinit var ctx: Context
private val TAG1 = "MAIN"
private lateinit var  sensorManager: SensorManager

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx  = applicationContext

        // [start] Permissions Check
        permissionsResultLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                if (result[Manifest.permission.ACCESS_FINE_LOCATION] != null) {
                    isLocFinePermissionGranted =
                        true == result[Manifest.permission.ACCESS_FINE_LOCATION]
                }
                if (result[Manifest.permission.ACCESS_NETWORK_STATE] != null) {
                    isNetworkPermissionGranted =
                        true == result[Manifest.permission.ACCESS_NETWORK_STATE]
                }
                if (result[Manifest.permission.ACCESS_WIFI_STATE] != null) {
                    isWifiPermissionGranted =
                        true == result[Manifest.permission.ACCESS_WIFI_STATE]
                }
            }

        if (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // If all permissions are granted, Proceed
            getWifiInfo(ctx)
        }
        else{
            requestPermission(applicationContext)
        }
// [end] Permissions Check

        //SensorManager: Magnetometer Block

        // Flow for Identifiying Sensors
        // https://developer.android.com/guide/topics/sensors/sensors_overview#sensors-identify
        // Initialize the SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        //List all of the Sensors on this device
        // returns a List that contains the type 'Sensor'
        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        // Use the List's Iterator method to write all the sensors to the log
        // .forEach method uses 'it' to represent the value at the iterator's index
        // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/for-each.html#foreach

        deviceSensors.forEach {
            // the '$' inside the quotes allows one to insert a variable into the string
            // String Templates - https://kotlinlang.org/docs/strings.html#string-templates
            Log.d(TAG1, "Device Sensor:$it ")
        }

        //End of SensorManager: Magnetometer Block

        setContent {
            IndoorAcessTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }

        getWifiInfo(this)
    }
}
var bssid: String = "";
var ssid: String = "";

private fun getWifiInfo(context: Context) {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    if (ActivityCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        // If all permissions are granted, Proceed
//        wifiManager.scanResults
//        Log.d(TAG, "getWifiInfo: ${wifiManager.scanResults}")
        val wifiInfo = wifiManager.connectionInfo
        val bssid = wifiInfo.bssid
        Log.d(TAG, "getWifiInfo: BSSID=$bssid")
    }
    else{
        requestPermission(ctx)
    }

    //WifiInfo is depricated and doesnt work
    //connectionInfo requires Wifi info and hence doesnt work
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!, $bssid, $ssid",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IndoorAcessTheme {
        Greeting("Android")
    }
}