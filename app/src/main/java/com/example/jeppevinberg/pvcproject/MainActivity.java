package com.example.jeppevinberg.pvcproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_NAME = "com.example.jeppevinberg.pvcproject.NAME";
    public final static String EXTRA_PASS = "com.example.jeppevinberg.pvcproject.PASS";
    private Firebase mainFirebase;
    private HashMap<String,Long> map;
    private boolean authenticated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);
        mainFirebase = new Firebase("https://glowing-heat-5041.firebaseio.com/");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        EditText nameText = (EditText) findViewById(R.id.name);
        String name = nameText.getText().toString();
        EditText passText = (EditText) findViewById(R.id.pass);
        String pass = passText.getText().toString();

        mainFirebase.child("pass").addValueEventListener(new ValueEventListener() {
            private Intent innerIntent;
            private String innerName;
            private String innerPass;

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                map = (HashMap<String, Long>) snapshot.getValue();
                if(map != null){
                    innerName = innerName.toLowerCase();
                    innerPass = innerPass.toLowerCase();

                    if(innerPass.equals(""+map.get(innerName))) {
                        Log.d("PvCProject", "Succesfully Authenticated");
                        innerIntent.putExtra(EXTRA_NAME, innerName);
                        innerIntent.putExtra(EXTRA_PASS, innerPass);
                        startActivity(innerIntent);
                    }else{
                        Log.d("PvCProject", "Authentication Failed");
                    }
                }

            }

            @Override
            public void onCancelled(FirebaseError error) {}

            public ValueEventListener init(Intent intent, String name, String pass){
                innerIntent = intent;
                innerName = name;
                innerPass = pass;
                return this;
            }

        }.init(intent, name, pass));

    }
}
