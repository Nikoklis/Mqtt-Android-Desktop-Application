package com.example.project.subscriber;


import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttSubscriber implements MqttCallback {
    private MqttAndroidClient mqttAndroidClient;
    private String topic = "MQTTExamples";
    private TextView data;

    //info for connecting
    private String broker;
    private String clientId;

//    public MqttSubscriber(Context context,String iP, String port, String frequency)
//    {
//        broker = "tcp://" + iP + ":" + port;
//        clientId = "AndroidSamplePublisher";
//
////        MqttConnectOptions connectOptions = new MqttConnectOptions();
////        connectOptions.setCleanSession(true);
//        mqttAndroidClient = new MqttAndroidClient(context, broker, clientId);
//
//        //call the main publish method
//        connectPublish(frequency);
//    }

    public MqttSubscriber(Context context, String iP, String port, TextView dataReceived) {

        broker = "tcp://" + iP + ":" + port;
        clientId = "AndroidSampleSubscriber";
        data = dataReceived;
        //MemoryPersistence persistence = new MemoryPersistence();
        //Connect to MQTT broker
        //MqttSubscriber sampleClient = new MqttSubscriber( broker,clientId,persistence);
//        MqttConnectOptions connectOptions = new MqttConnectOptions();
//        connectOptions.setCleanSession(true);
        mqttAndroidClient = new MqttAndroidClient(context, broker, clientId);


        //important!! --implemented in mqtt() function in MainActivity
//        mqttAndroidClient.setCallback(new MqttCallback() {
//            @Override
//            public void connectionLost(Throwable cause) {
//
//            }
//
//            @Override
//            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                //Log.w("Mqtt", message.toString());
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken token) {
//
//            }
//        });

        //call the main subscribe method
        connectSubscribe();

    }


    //MQTT FUNCTIONS --we have to implement these functions
    //from mqtt
    @Override
    public void connectionLost(Throwable cause) {
        //This method is called when the connection to the server is lost.
        System.out.println("Connection is lost ! " + cause);
        System.exit(1);

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        //this method is called when a message arrives from the server

        //Log.w("Mqtt",new String(message.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        //Called when delivery for a message has been completed
        //and all acknowledgments have been received
    }
    //END OF MQTT FUNCTIONS


    //function to initialize connection (wrappper)
    public void setCallback(MqttCallback callback) {
        mqttAndroidClient.setCallback(callback);
    }


    //function to connect as a publisher to the broker
//    private void connectPublish(String frequency) {
//        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
//        mqttConnectOptions.setAutomaticReconnect(true);
//        mqttConnectOptions.setCleanSession(false);
//
//        try {
//            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//
//                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
//                    //disconnectedBufferOptions.setBufferEnabled(true);
//                    //disconnectedBufferOptions.setBufferSize(100);
//                    //disconnectedBufferOptions.setPersistBuffer(false);
//                    disconnectedBufferOptions.setDeleteOldestMessages(false);
//                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
//                    publishToTopic();
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.w("Mqtt", "Failed to connectSubscribe to: " + exception.toString());
//                }
//            });
//
//
//        } catch (MqttException ex) {
//            ex.printStackTrace();
//        }
//    }

//    private void publishToTopic ()
//    {
//        try {
//            mqttAndroidClient.publish(topic,frequency)
//            mqttAndroidClient.publish(topic, 2, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.w("Mqtt", "Publish fail!");
//                }
//            });
//
//        } catch (MqttException ex) {
//            System.err.println("Exceptionst subscribing");
//            ex.printStackTrace();
//        }
//    }

    //main connectSubscribe function that leads to subscription
    //we get some extra options here
    //MOST IMPORTANT is setCleanSession (resets the session everytime we connectSubscribe)
    private void connectSubscribe() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);

        try {
            //the IMqttActionListener provides with onSuccess method
            //without it the programm doesnt work
            //because the communication is asynchronus and there is a change
            //we will get a NullException
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    //disconnectedBufferOptions.setBufferEnabled(true);
                    //disconnectedBufferOptions.setBufferSize(100);
                    //disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    //helper method to subscribe to topic
                    data.setText("Connection established ... awaiting commands from broker");
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connectSubscribe to: " + exception.toString());
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    //helper Subscribe method
    //IMqttActionListener ---> same as connectSubscribe one
    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(topic, 2, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exceptionst subscribing");
            ex.printStackTrace();
        }
    }


    public MqttAndroidClient getMqttAndroidClient() {
        return mqttAndroidClient;
    }

}