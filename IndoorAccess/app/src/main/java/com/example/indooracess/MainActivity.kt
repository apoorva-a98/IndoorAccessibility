package com.example.indooracess

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
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
private const val TAG1 = "SensorRead"
private lateinit var  sensorManager: SensorManager

private lateinit var directionVector: Sensor
/* To the right of the ':' below is the class that MainActivity inherits. After the comma we have SensorEventListener which is an Interface
Inheritance - https://kotlinlang.org/docs/inheritance.html
Interface -  https://kotlinlang.org/docs/interfaces.html
 */
private var compassAngle: Float = 0f
private var magneticDeclination: Float = 0f

class MainActivity : ComponentActivity(), SensorEventListener {
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

        // Todo cleaned this up by placing it in it's own function
        listSensors(sensorManager = sensorManager)
//        directionVector = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        directionVector = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        /*In Android, the TYPE_MAGNETIC_FIELD sensor is typically used to measure the Earth's magnetic field, which can be used to determine the device's orientation and direction.
        The TYPE_ORIENTATION sensor was deprecated in API level 3 and is not recommended for use. The TYPE_GEOMAGNETIC_ROTATION_VECTOR and TYPE_MAGNETIC_FIELD_UNCALIBRATED sensors are alternative options that provide more advanced features, such as higher accuracy or uncalibrated readings, but are not necessary for basic compass functionality.
        Therefore, the recommended sensor to use for a compass application in Android is the TYPE_MAGNETIC_FIELD sensor.*/

        // Obtain magnetic declination for current location (in degrees)
        // This value can be obtained from a magnetic declination map or a third-party API
        magneticDeclination = -12.37f // In Brooklyn Heights NYC https://www.magnetic-declination.com/#

        //TODO This registration activates an Observer that is listening for state changes in the sensor data
        sensorManager.registerListener(this, directionVector, SensorManager.SENSOR_DELAY_NORMAL)
        /*This delay indicates the frequency with which the sensor readings are updated and provided to the app. In particular, SENSOR_DELAY_NORMAL specifies a delay of around 200,000 microseconds or 200 milliseconds, which means that the sensor readings will be delivered at a rate of approximately 5 times per second.*/

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

        58override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            Log.d(TAG, "onAccuracyChanged: DO Something")
        }
        /* This observer function is listening for changes in value, once you register the listener above, it will
        continually update
        */

        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                /* TODO These if statements will make the device LESS sensitive to small movements...
                I am doing this to illustrate how we can use the phone orientation as a trigger vs. a controller
                Play with these values. The response range should be tuned to the interaction you are trying to author
                 */
                // the 'f' after the number value means the number is a Float - https://kotlinlang.org/docs/numbers.html#literal-constants-for-numbers
                // Log.d(TAG, (event.values[0] + event.values[1] + event.values[2]).toString())
                val dx = event.values[0];
                val dy = event.values[1];
                val dz = event.values[2];

                var toDegrees : Double = Math.toDegrees(dz.toDouble())
                Log.d(TAG, "x: ${dx} y: ${dy} z: ${toDegrees}")

                // Calculate total magnetic field strength (in microtesla)
//            val magnetic_field : Double = sqrt((dx * dx + dy * dy + dz * dz).toDouble())

                // Calculate inclination angle (in radians)
                val inclination_angle = kotlin.math.atan(dy / kotlin.math.sqrt((dx * dx + dz * dz).toDouble()))

                // Calculate azimuth angle (in degrees)
                compassAngle = ((360 + kotlin.math.atan(dx / (dy * kotlin.math.sin(inclination_angle) + dz * kotlin.math.cos(inclination_angle))) % 360 - magneticDeclination).toFloat())
//                Log.d(TAG, compassAngle.toString())
            }
        }

        /* Applications have lifecycles. When we launch the app, it is in the Foreground, if we switch to another app, it moves to the background.
        Activity Lifecycle - https://developer.android.com/guide/components/activities/activity-lifecycle
        When the app is in the background it is paused. onPause, therefore we stop listening to the sensor.
         */
        override fun onPause() {
            super.onPause()
            sensorManager.unregisterListener(this)
        }
        /* When we bring the already running app back to the foreground
         we are resuming the app. onResume(), executes the code that restarts the listener by calling the .registerListener method
         */
        override fun onResume() {
            super.onResume()
            directionVector.also { directionVector ->
                sensorManager.registerListener(this, directionVector, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        TODO("Not yet implemented")
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