package com.example.logonrmlocal.demomqttandroid

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttCallback
import android.widget.Toast
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.MqttClient
import java.io.UnsupportedEncodingException
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var client: MqttAndroidClient
    private val mqttServiceURI = "http://10.3.8.37"
    private val topicoLED = "vini16"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connectMQTTClient()
    }

    fun alterarStatus(v: View) {
        if (switcher.displayedChild === 0) {
            ligar()
        } else {
            desligar()
        }
    }
    private fun connectMQTTClient() {
        val clientId = MqttClient.generateClientId()
        client = MqttAndroidClient(this.applicationContext,
                mqttServiceURI,
                clientId)
        try {
            val token = client.connect()
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    subscribeLed()
                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Toast.makeText(this@MainActivity, exception.message,
                            Toast.LENGTH_LONG).show()
                }
            }
            client.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable) {
                }
                @Throws(Exception::class)
                override fun messageArrived(topic: String, message: MqttMessage) {
                    if (topic == topicoLED) {
                        if (message.toString() == "1") {
                            if (switcher.displayedChild != 1)
                                switcher.showPrevious()
                        } else if (message.toString() == "0") {
                            if (switcher.displayedChild != 0)
                                switcher.showNext()
                        }
                    }
                }
                override fun deliveryComplete(token: IMqttDeliveryToken) {
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
    fun disconnectMQTTClient() {
        try {
            val disconToken = client.disconnect()
            disconToken.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                }
                override fun onFailure(asyncActionToken: IMqttToken,
                                       exception: Throwable) {
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribeLed() {
        val qos = 1
        try {
            val subToken = client.subscribe(topicoLED, qos)
            subToken.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.i("TAG", "SUCESSO")
                }
                override fun onFailure(asyncActionToken: IMqttToken,
                                       exception: Throwable) {
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun unsubscribeLed() {
        try {
            val unsubToken = client.unsubscribe(topicoLED)
            unsubToken.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    disconnectMQTTClient()
                }
                override fun onFailure(asyncActionToken: IMqttToken,
                                       exception: Throwable) {
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unsubscribeLed()
    }

    private fun ligar() {
        val payload = "1"
        val encodedPayload: ByteArray
        try {
            encodedPayload = payload.toByteArray(charset("UTF-8"))
            val message = MqttMessage(encodedPayload)
            message.isRetained = true
            client.publish(topicoLED, message)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
    private fun desligar() {
        val payload = "0"
        val encodedPayload: ByteArray
        try {
            encodedPayload = payload.toByteArray(charset("UTF-8"))
            val message = MqttMessage(encodedPayload)
            message.isRetained = true
            client.publish(topicoLED, message)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}


