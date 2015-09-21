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
    public final static String EXTRA_DBID = "com.example.jeppevinberg.pvcproject.DBID";
    private Firebase mainFirebase;
    private Firebase usersFirebase;
    private HashMap<String,Long> map;
    private HashMap<String,HashMap<String,String>> testMap;
    private boolean authenticated = false;
    private boolean userExists = false;
    private String name;
    private String password;
    private String dBID;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);
        mainFirebase = new Firebase("https://glowing-heat-5041.firebaseio.com/");
        usersFirebase = new Firebase("https://glowing-heat-5041.firebaseio.com/users");
        intent = new Intent(this, MapsActivity.class);


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
    public void login(View view) {
        EditText nameText = (EditText) findViewById(R.id.name);
        name = nameText.getText().toString().toLowerCase();
        EditText passText = (EditText) findViewById(R.id.pass);
        password = passText.getText().toString().toLowerCase();

        usersFirebase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                authenticated = false;
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    if (user.getPassword().equals(password) && user.getName().equals(name)) {
                        authenticated = true;
                        dBID = postSnapshot.getKey();
                        break;
                    }
                }


                //remove eventlistener to ignore redundant callbacks
                usersFirebase.removeEventListener(this);
                if(authenticated){
                    Log.i("PvCProject", "Authentication Successful");
                    intent.putExtra(EXTRA_NAME, name);
                    intent.putExtra(EXTRA_PASS, password);
                    intent.putExtra(EXTRA_DBID, dBID);
                    startActivity(intent);

                }else{
                   Log.i("PvCProject", "Authentication Failed");
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }

        });

    }

    public void register(View view) {
        EditText nameText = (EditText) findViewById(R.id.name);
        name = nameText.getText().toString().toLowerCase();
        EditText passText = (EditText) findViewById(R.id.pass);
        password = passText.getText().toString().toLowerCase();

        usersFirebase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userExists = false;
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    if(user.getName().equals(name)){
                        userExists = true;
                        break;
                    }
                }
                //remove eventlistener to ignore redundant callbacks
                usersFirebase.removeEventListener(this);
                if(userExists){
                    Log.i("PvCProject", "User Exists");
                }else{
                    User user = new User(name, password, null, null);
                    usersFirebase.push().setValue(user);
                    Log.i("PvCProject", "User Created");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}

        });



    }

}
