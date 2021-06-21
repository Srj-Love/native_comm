package apero.technologies.native_comm

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Build.VERSION
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StringCodec

class MainActivity : FlutterActivity() {

    private val CHANNEL = "technologies.apero/battery"
    private val METHOD_BATTERY = "getBatteryLevel"

    private val CHANNEL_HELLO = "technologies.apero/hello"

    private val EVENT_CHANNEL = "technologies.apero/event"
    private lateinit var eventChannel: EventChannel

    private lateinit var channel: MethodChannel

    private val MESSAGE_CHANNEL = "technologies.apero/message"


    private lateinit var mSensorManager: SensorManager
    private lateinit var mAccelerometer: Sensor



    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // post data to flutter
        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        channel.setMethodCallHandler { call, result ->
            when (call.method) {
                METHOD_BATTERY -> {
                    val batteryLevel = getBatteryLevel()
                    if (batteryLevel != -1) {
                        result.success(batteryLevel)
                    } else {
                        result.error("UNAVAILABLE", "Battery level not available.", null)
                    }
                }
                else -> result.notImplemented()
            }

        }

        val helloChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_HELLO)
        helloChannel.invokeMethod("callMe",  listOf("a", "b"), object: MethodChannel.Result{
            override fun success(result: Any?) {
                Log.i("Android", "result: $result")
            }

            override fun error(errorCode: String?, errorMessage: String?, errorDetails: Any?) {
                Log.i("Android", "error: $errorCode, $errorMessage, $errorDetails")
            }

            override fun notImplemented() {
                Log.i("Android", "notImplemented")
            }
        })

        // checck orientation with event channel
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        eventChannel = EventChannel(flutterEngine.dartExecutor.binaryMessenger, EVENT_CHANNEL)
        eventChannel.setStreamHandler(object: EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, eventSink: EventChannel.EventSink?) {
                emitDeviceOrientation(eventSink)

            }

            override fun onCancel(arguments: Any?) {
                Log.i("Android", "EventChannel onCancel called")
            }
        })

        // basic channel
        val messageChannel = BasicMessageChannel(flutterEngine.dartExecutor.binaryMessenger, MESSAGE_CHANNEL, StringCodec.INSTANCE)
        messageChannel.setMessageHandler { message, reply ->
            Log.i("Android", "Received message: $message")
            reply.reply("Reply from Android!!")
        }

    }

    private fun getBatteryLevel(): Int {
        val batteryLevel: Int
        if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            val intent = ContextWrapper(applicationContext).registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            batteryLevel = intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        }

        return batteryLevel
    }

    private fun emitDeviceOrientation(events: EventChannel.EventSink?) {
        mSensorManager.registerListener(object : SensorEventListener {
            override fun onSensorChanged(sensorEvent: SensorEvent?) {
                if (sensorEvent?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    if (Math.abs(sensorEvent.values[1]) > Math.abs(sensorEvent.values[0])) {
                        //Mainly portrait
                        if (sensorEvent.values[1] > 0.75) {
                            events?.success("Portrait")
                        } else if (sensorEvent.values[1] < -0.75) {
                            events?.success("Portrait Upside down")
                        }
                    } else {
                        //Mainly landscape
                        if (sensorEvent.values[0] > 0.75) {
                            events?.success("Landscape Right")
                        } else if (sensorEvent.values[0] < -0.75) {
                            events?.success("Landscape Left")
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }
        }, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

}

