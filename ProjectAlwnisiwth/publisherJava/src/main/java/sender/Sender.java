package sender;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.BlockingQueue;

public class Sender implements Runnable {
    private final BlockingQueue<String> queue;
    private String topic = "MQTTExamples";
    private String topicSecond = "MQTTExamplesSecond";
    private String broker = "tcp://localhost:1883";
    private String clientIdPublisher = "JavaPublish";
    private String clientIdSubscriber = "JavaSubscriber";
    private MemoryPersistence persistence = new MemoryPersistence();
    private int qos = 2;
    private int frequency = 0;

    public Sender(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        subscribe();
        publish();
    }

    private void subscribe() {
        MqttClient subscribeClient = null;
        try {
            subscribeClient = new MqttClient(broker, clientIdSubscriber, persistence);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            subscribeClient.connect(connOpts);

            subscribeClient.subscribe(topicSecond, 2);
            subscribeClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String stringFrequency = message.toString();
                    frequency = Integer.parseInt(stringFrequency);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    private void publish() {
        try {
            MqttClient publishClient = null;
            publishClient = new MqttClient(broker, clientIdPublisher, persistence);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            publishClient.connect(connOpts);

            MqttMessage message = null;
            String answer = null;

            while (true) {

                answer = queue.take();
                //a minor check to see if we have a frequency from the user
                //default value goes to 3 seconds if user has set it to
                //0 in the android app
                if (frequency == 0) {
                    frequency = 3;
                }
                Thread.sleep(1000);
                System.out.println(answer);
                System.out.println(frequency);

                //get the real value of the classification
                //there are also information in the string
                //'answer' about the success/fail of the experiment
                //the real answer is stored before the first whitespace character
                String[] realAnswer = answer.split(" ");
                answer = realAnswer[0];

                message = new MqttMessage(answer.getBytes());
                message.setQos(qos);
//                message.setRetained(true);
                publishClient.publish(topic, message);
                Thread.sleep(frequency * 1000);
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Interrupted while waiting in queue");
        } catch (MqttSecurityException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
}
