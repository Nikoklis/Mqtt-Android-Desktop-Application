package com.example.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project.R;
import com.example.project.subscriber.MqttSubscriber;

public class Options extends AppCompatActivity {

    //information from the user
    EditText port, iP, frequency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        port = findViewById(R.id.portField);
        iP = findViewById(R.id.ipField);
        frequency = findViewById(R.id.frequencyField);

    }

    //sends the information got from the user to the MainActivity
    public void sendAttributes(View view) {

        if (iP.getText().toString().isEmpty() || port.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please provide all the fields", Toast.LENGTH_SHORT).show();
        } else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("iP", iP.getText().toString());
            returnIntent.putExtra("port", port.getText().toString());
            returnIntent.putExtra("frequency", frequency.getText().toString());
            setResult(MainActivity.RESULT_OK, returnIntent);
            finish();
        }

    }
}
