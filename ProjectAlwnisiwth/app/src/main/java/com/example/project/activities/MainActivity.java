package com.example.project.activities;


import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.MemoryFile;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.project.R;
import com.example.project.extraThreads.InternetChecker;
import com.example.project.subscriber.MqttSubscriber;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.io.Serializable;

public class MainActivity extends AppCompatActivity {
    //code for the communication between intents
    static final int RESULT_CODE = 1;

    //the data received from the Java App
    private TextView dataReceived;

    //the Subscriber class -- mqtt functions in there
    private MqttSubscriber subscriberMQTT;

    //the Publisher class
    private MqttClient publishBackClient;

    //context helper field
    private Context context;

    //boolean for flash manipulation
    private boolean hasFlash;
    private boolean isFlashOn;

    //camera helping fields to get camera object of device
    private CameraManager cameraManager;
    private String cameraID;

    //button fields to get the buttons from View
    private Button buttonSound;
    private Button buttonCam;

    //media player field to get the media player of device
    MediaPlayer mediaPlayer;

    //mqtt broker
    private String broker;

    @RequiresApi(Build.VERSION_CODES.M)
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialization
        context = getApplicationContext();
        dataReceived = (TextView) findViewById(R.id.dataReceived);
        buttonSound = (Button) this.findViewById(R.id.buttonsnd);
        buttonCam = (Button) this.findViewById(R.id.buttonFlashlight);
        isFlashOn = false;
        hasFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        mediaPlayer = MediaPlayer.create(context, R.raw.tone);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraID = cameraManager.getCameraIdList()[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        //////////

        dataReceived.setText("There is no connection to an MQTT server \n in order to connect press the Settings button on the app menu");


        //2 listeners for our 2 buttons
        buttonSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    try {
                        mediaPlayer.prepareAsync();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mediaPlayer.start();
                }
            }
        });

        buttonCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFlashOn) {
                    turnOffFlashLight();
                    isFlashOn = false;
                } else {
                    turnOnFlashLight();
                    isFlashOn = true;
                }
            }
        });
        //////

        //start a background task to check if we have internet
        //connection --if we dont have enable it and show it to user
        InternetChecker internetChecker = new InternetChecker(getSystemService(Context.CONNECTIVITY_SERVICE), context);
        internetChecker.execute();


//        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mediaPlayer) {
//
//            }
//        });

    }


    //function to publish frequency back to Java Application
    private void publishFrequency(String ip, String port, String frequency) {
        if (ip == "" || port == "") {

        }
        broker = "tcp://" + ip + ":" + port;
        String clientId = "AndroidSamplePublisher";
        String topic = "MQTTExamplesSecond";
        try {
            MemoryPersistence memoryPersistence = new MemoryPersistence();
            publishBackClient = new MqttClient(broker, clientId, memoryPersistence);
            //create the publisher Client of the android
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            publishBackClient.connect(connOpts);


            //send the frequency to Java
            MqttMessage message = new MqttMessage(frequency.getBytes());
            message.setQos(2);
            message.setRetained(true);
            publishBackClient.publish(topic, message);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if we have returned from a particular activity --Options activity
        if (requestCode == RESULT_CODE) {
            if (resultCode == RESULT_OK) {

                //get the info from the Options activity
                String ip = data.getExtras().getString("iP");
                String port = data.getExtras().getString("port");
                String frequency = data.getExtras().getString("frequency");


                //publish the frequency back to the Java Application
                publishFrequency(ip, port, frequency);

                //create our client to the broker on the particular
                //ip and port
                if (subscriberMQTT != null) {
                    if (!subscriberMQTT.getMqttAndroidClient().isConnected())
                    {
                        subscriberMQTT = new MqttSubscriber(context, ip, port, dataReceived);
                        subscriberMQTT.setCallback(new MqttCallback() {
                            @Override
                            public void connectionLost(Throwable cause) {

                            }

                            //called each time we receive a message from broker
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void messageArrived(String topic, MqttMessage message) throws Exception {


                                //change the text for the user to see
                                dataReceived.setText(message.toString());


                                //string manipulation of our command
                                String musicData = null;
                                String torchData = null;

                                if (message.toString().equals("EyesOpened")) {
                                    musicData = "Play sound for 5 seconds";
                                    torchData = "Open torch for 5 seconds";
                                    String[] musicStrings = musicData.split(" ");
                                    String[] torchStrings = torchData.split(" ");

                                    if (mediaPlayer.isPlaying() || isFlashOn) {
                                        dataReceived.setText("Flash or music still playing from previews command --doing nothing on this one");
                                    } else {
                                        //function for playing music
                                        playMusic(musicStrings);

                                        //function for opening flashlight
                                        openFlash(torchStrings);
                                    }


                                } else if (message.toString().equals("EyesClosed")) {
                                    //close flash
                                    if (isFlashOn) {
                                        turnOffFlashLight();
                                        isFlashOn = false;
                                    }

                                    //stop sound
                                    if (mediaPlayer.isPlaying()) {
                                        mediaPlayer.stop();
                                        try {
                                            mediaPlayer.prepareAsync();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                }
                            }

                            @Override
                            public void deliveryComplete(IMqttDeliveryToken token) {

                            }

                        });
                    }
                }
                else {
                    subscriberMQTT = new MqttSubscriber(context, ip, port, dataReceived);
                    subscriberMQTT.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {

                        }

                        //called each time we receive a message from broker
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {


                            //change the text for the user to see
                            dataReceived.setText(message.toString());


                            //string manipulation of our command
                            String musicData = null;
                            String torchData = null;

                            if (message.toString().equals("EyesOpened")) {
                                musicData = "Play sound for 5 seconds";
                                torchData = "Open torch for 5 seconds";
                                String[] musicStrings = musicData.split(" ");
                                String[] torchStrings = torchData.split(" ");

                                if (mediaPlayer.isPlaying() || isFlashOn) {
                                    dataReceived.setText("Flash or music still playing from previews command --doing nothing on this one");
                                } else {
                                    //function for playing music
                                    playMusic(musicStrings);

                                    //function for opening flashlight
                                    openFlash(torchStrings);
                                }


                            } else if (message.toString().equals("EyesClosed")) {
                                //close flash
                                if (isFlashOn) {
                                    turnOffFlashLight();
                                    isFlashOn = false;
                                }

                                //stop sound
                                if (mediaPlayer.isPlaying()) {
                                    mediaPlayer.stop();
                                    try {
                                        mediaPlayer.prepareAsync();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {

                        }

                    });
                }


                //change the message presented to the user
//                dataReceived.setText("Connection established ... awaiting commands from broker");
            }
        }
    }

    //function that calls the second activity from where we will get
    //the user input for IP, Port of the broker and the frequency
    //for the commands
    private void getInfoFromOptions() {
        Intent intent = new Intent(this, Options.class);
//        intent.putExtra("Subscriber",(Parcelable) subscriberMQTT);
        startActivityForResult(intent, RESULT_CODE);
    }

    //function that plays our music for X seconds
    public void playMusic(final String[] strings) throws IOException {
        final int[] givenTime = {Integer.parseInt(strings[3])};


        //thread responsible for counting the X seconds
        //stop the music when thread wakes up
        //IMPORTANT -- we play the sound clip for exactly X seconds
        //regardless of its real duration
        //the setLooping function and the timer helps with that
        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(givenTime[0] * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    mediaPlayer.stop();
                    try {
                        mediaPlayer.prepareAsync();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        //start the thread
        timer.start();

    }


    //helper function to check if we must open the flash
    private boolean commandIsFlash(String[] strings) {
        if (strings[0].equalsIgnoreCase("Open") &&
                strings[1].equalsIgnoreCase("flash") &&
                strings[2].equalsIgnoreCase("for") &&
                strings[4].equalsIgnoreCase("seconds"))
            return true;

        return false;
    }


    //helper function to check if we must play sound
    private boolean commandIsSound(String[] strings) {
        if (strings[0].equalsIgnoreCase("Play") &&
                strings[1].equalsIgnoreCase("sound") &&
                strings[2].equalsIgnoreCase("for") &&
                strings[4].equalsIgnoreCase("seconds"))
            return true;


        return false;
    }

    //actual function that opens the flash
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void openFlash(String[] strings) {
        if (!hasFlash) {
            //no flash
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Error !!");
            alert.setMessage("Your device doesn't support flash light!");
            alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    finish();
                    System.exit(0);
                }
            });
            alert.show();
            return;
        } else {
            if (!isFlashOn) {
                turnOnFlashLight();
                isFlashOn = true;
            }
//            else {
//                turnOnFlashLight();
//                isFlashOn = true;
//            }
//

            //counter for flash to remain open
            CountDownTimer countDownTimer = new CountDownTimer(Integer.parseInt(strings[3]) * 1000, 1) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Log.w("timer", Long.toString(millisUntilFinished));
                }

                //called when the timer finishes
                //here we turn off the flash after X seconds
                @Override
                public void onFinish() {
                    try {
                        if (isFlashOn) {
                            turnOffFlashLight();
                            isFlashOn = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };
            //start the timer
            countDownTimer.start();


        }

    }

    //helper function to open the flash
    public void turnOnFlashLight() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraID, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //helper function to close the flash
    public void turnOffFlashLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraID, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //function for our menu creation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    //Exit button implementation
    // public void clickExit(View v){
    //     finish();
    //     try {
    //         if (subscriberMQTT != null)
    //             subscriberMQTT.mqttAndroidClient.disconnect();
    //     } catch (MqttException e) {
    //         e.printStackTrace();
    //     }
    // }


    //function for back Button pressed

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Do you want to exit?");
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {


            @Override
            public void onClick(DialogInterface dialog, int id) {
                try {
                    if (subscriberMQTT != null)
                        subscriberMQTT.getMqttAndroidClient().disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                android.os.Process.killProcess(android.os.Process.myPid());
                finish();
            }

        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    //function to check if user clicks on a button on our menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.quit) { //If exit button pressed then pop up prompt dialog
            onClickExit();

        } else if (id == R.id.action_settings) { //If settings button is pressed show message
            getInfoFromOptions();
            return true;
        }


        return super.onOptionsItemSelected(item);

    }

    //helper function to exit the app
    private void onClickExit() {
        if (subscriberMQTT != null)
            try {
                subscriberMQTT.getMqttAndroidClient().disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        android.os.Process.killProcess(android.os.Process.myPid());
        finish();
    }

    //function when user resets the app
    @Override
    public void onPause() {
        super.onPause();
        if (isFlashOn) {
            turnOffFlashLight();
            isFlashOn = false;
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


}
