package com.example.jeppevinberg.pvcproject;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String TAG = MapsActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private MarkerOptions marker;
    private String userName, userPass;
    private String dBID;
    private Firebase mainFirebase;
    private Firebase locationFirebase;
    private Firebase clientFirebase;


    /* This method is called once the activity is created for the first time.
        It handles setting up the map, registering the user as an GoogleApiClient and setting the location request rate.
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mainFirebase = new Firebase("https://glowing-heat-5041.firebaseio.com/");
        locationFirebase = mainFirebase.child("locations");
        clientFirebase = locationFirebase.push();
        dBID = clientFirebase.getKey();
        Intent intent = getIntent();
        userName = intent.getStringExtra(MainActivity.EXTRA_NAME);
        userPass = intent.getStringExtra(MainActivity.EXTRA_PASS);
        setUpMapIfNeeded();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
    }
    // This method is called when the activity resumes after the device goes into hibernation.
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        locationFirebase.child(dBID).removeValue();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {}

    //This method is called once there has been a succesful connection the google API services
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location Services Connected.");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location == null){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else{
            handleNewLocation(location);
            locationFirebase.child(dBID).setValue(locationToMap(location.getLatitude(), location.getLongitude()));
        }

    }

    public void handleNewLocation(Location location){
        //Log.i(TAG, location.toString());
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        locationFirebase.child(dBID).updateChildren(locationToMap(currentLatitude, currentLongitude));

        if(marker == null){
            setupFirstMarker(latLng);
        }else{
            mMap.clear();
            updateOwnMarker(latLng);
            updateOtherMarkers();
        }


    }

    public Map<String,Object> locationToMap(double lat, double lng){
        Map<String,Object> loc = new HashMap<String,Object>();
        loc.put("name", userName);
        loc.put("lat", "" + lat);
        loc.put("lng", "" + lng);
        return loc;
    }

    public void setupFirstMarker(LatLng latLng){
        marker = new MarkerOptions()
                .position(latLng)
                .title(userName);
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
    }

    public void updateOwnMarker(LatLng latLng){
        marker = new MarkerOptions()
                .position(latLng)
                .title(userName);
        mMap.addMarker(marker);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom));
    }

    public void updateOtherMarkers(){

        locationFirebase.addValueEventListener(new ValueEventListener() {
            MarkerOptions mark;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ss : dataSnapshot.getChildren()) {
                        if(ss.getValue().getClass().equals(HashMap.class)){
                            if(!(ss.getKey().equals(dBID))){
                                HashMap<String, Object> m = (HashMap<String,Object>)ss.getValue();
                                String name = (String) m.get("name");
                                double lat = Double.parseDouble((String) m.get("lat"));
                                double lng = Double.parseDouble((String) m.get("lng"));
                                mark = new MarkerOptions().position(new LatLng(lat, lng)).title(name);
                                mMap.addMarker(mark);
                            }


                        }




                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location Services Suspended. Please Reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }
    //This method is called when the API reports of a change in location
    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        locationFirebase.child(dBID).removeValue();

    }


}
