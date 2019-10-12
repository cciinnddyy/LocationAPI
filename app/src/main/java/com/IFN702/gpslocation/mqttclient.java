package com.IFN702.gpslocation;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

//Mqtt libraries

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

import com.IFN702.gpslocation.MainActivity;

public class mqttclient extends AsyncTask<String , Void , String> {

public MqttAndroidClient mqttclient;

private final String serveruri = "xxxxxx";

private final String username = "xxx";

private final String password = "xxx";




public String mqttclientID = MqttClient.generateClientId();



    @Override
    protected String doInBackground(String... strings) {

    MqttConnectOptions options = new MqttConnectOptions();

        final Timer timer = new Timer();

        //final Handler hdler = new Handler();


        mqttclient = new MqttAndroidClient(MainActivity.getContext(),serveruri,mqttclientID);
        try {
            final String topic = "gps";



            options.setUserName(username);

            options.setPassword(password.toCharArray());


            IMqttToken tokens = mqttclient.connect(options);


            tokens.setActionCallback(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("Connection Goooooood","Goooooooooood");

                    timertask t = new timertask(mqttclient);
                    timer.scheduleAtFixedRate(t,0,5000);

                    //Runnable runnable = new runnable(mqttclient);
                    //hdler.postDelayed(runnable,5000);

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("fail connection",exception.toString());
                }
            });
        }
        catch (MqttException e){
            Log.e("connection error",e.toString());
        }
        //return the result from the background process
        return null;
    }

    @Override
    protected void onPostExecute(String strings){
        //the background task is finished and the result is passed as the parameter
        super.onPostExecute(strings);




    }

    public class timertask extends TimerTask {

        private MqttAndroidClient mqttClient;


        public timertask (MqttAndroidClient mqclient){
            this.mqttClient = mqclient;
        }
        @Override
        public void run(){

            String topic ="gps";
            String payloadMessage = MainActivity.locations;

            byte[] encodedMessage = new byte[0];


            try {

                encodedMessage = payloadMessage.getBytes();


            }catch (Exception e){
                Log.d ("Message getByte", e.toString());
            }

            final MqttMessage mgs = new MqttMessage(encodedMessage);
            try{
            mqttClient.publish(topic,mgs);}
            catch (MqttException e){
                Log.d("Error Timer",e.toString());
            }
        }

    }

    public class runnable implements Runnable{

        private MqttAndroidClient mclient;
        public runnable(MqttAndroidClient mqclient){
            this.mclient = mqclient;
        }

        @Override
        public void run(){
            String topic ="gps";
            String payloadMessage = MainActivity.locations;

            byte[] encodedMessage = new byte[0];


            try {

                encodedMessage = payloadMessage.getBytes();


            }catch (Exception e){
                Log.d ("Message getByte", e.toString());
            }

            final MqttMessage mgs = new MqttMessage(encodedMessage);
            try{
                this.mclient.publish(topic,mgs);}
            catch (MqttException e){
                Log.d("Error Timer",e.toString());
            }


        }
    }
}
