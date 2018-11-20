package com.example.anik.tripmate;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RoutingListener
{
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseReference;
    FirebaseUser mCurrentUser;

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    MarkerOptions markerOptions;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    Marker mCurrLocationMarker, friendLocationMarker;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.btn_logut_bg, R.color.orange};


    String name, userName, uid;
    Intent intent;
    Boolean zoomState = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                //Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //centerMapOnLocation(lastKnownLocation, "Your Location");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                }
            }
        }
    }

    public void centerMapOnLocation(Location location, String title, int id) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        //if (id == 2)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        polylines = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

        intent = getIntent();
        uid = intent.getStringExtra("uid");
        name = intent.getStringExtra("name");

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {

                Map map = (Map) polyline.getTag();

                final android.support.v7.app.AlertDialog diag = new android.support.v7.app.AlertDialog.Builder(MapsActivity.this)
                        .setView(R.layout.polyline_distance)
                        .create();
                diag.show();

                TextView distance = (TextView) diag.findViewById(R.id.distance);
                TextView duration = (TextView) diag.findViewById(R.id.duration);

                String dist = "Distance:", dura = "Duration:";

                if(map.get("Distance") != null) {
                    Double val = Double.parseDouble(map.get("Distance").toString());
                    val /= 1000;

                    dist += " " + String.valueOf(String.format(Locale.getDefault(), "%.2f", val)) + "km";
                }

                distance.setText(dist);

                if(map.get("Duration") != null) {
                    Double val = Double.parseDouble(map.get("Duration").toString());
                    Double min = val / 60;
                    Double hr = Math.floor(min / 60);

                    Double val2 = hr*60;

                    Double m = Math.floor(min - val2);

                    dura += " " + String.valueOf(String.format(Locale.getDefault(), "%.0f", hr)) + "hr " + String.valueOf(String.format(Locale.getDefault(), "%.0f", m)) + "min";
                }

                duration.setText(dura);
            }
        });

//        Double longitude = intent.getDoubleExtra("longitude", 0.0);
//        Double latitude = intent.getDoubleExtra("latitude", 0.0);
//        String name = intent.getStringExtra("name");
//
//        Location userLocation = new Location(LocationManager.GPS_PROVIDER);
//        userLocation.setLatitude(latitude);
//        userLocation.setLongitude(longitude);
//
//        LatLng ltln = new LatLng(latitude, longitude);
//        centerMapOnLocation(userLocation, name, 2);
//        mMap.addMarker(new MarkerOptions().position(ltln).title(name));
//
//        Double myLongitude = intent.getDoubleExtra("MyLongitude", 0.0);
//        Double myLatitude = intent.getDoubleExtra("MyLatitude", 0.0);
//        String myLocationName = intent.getStringExtra("myLocationName");
//
//        if (myLongitude != 0.0 && myLatitude != 0.0) {
//            Location myLocation = new Location(LocationManager.GPS_PROVIDER);
//            myLocation.setLatitude(myLatitude);
//            myLocation.setLongitude(myLongitude);
//
//            LatLng myLtLn = new LatLng(myLatitude, myLongitude);
//            centerMapOnLocation(myLocation, name, 1);
//            mMap.addMarker(new MarkerOptions().position(myLtLn).title(myLocationName));
//        }
//
//
//        Toast.makeText(this, "Location Tracked", Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if(mGoogleApiClient != null) {
            requestLocationUpdates();
        }

        else {
            buildGoogleApiClient();
        }
    }

    public void requestLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;

                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                String locationName = getUserLocation();

                //Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                markerOptions.title(userName);

                mCurrLocationMarker = mMap.addMarker(markerOptions);
                mCurrLocationMarker.showInfoWindow();

                //move map camera
                //if(mCurrentUser.getUid().equals(uid))
                  //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));

                mAuth = FirebaseAuth.getInstance();
                mCurrentUser = mAuth.getCurrentUser();
                mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

                Map map = new HashMap();
                map.put("location", locationName);
                map.put("latitude", String.valueOf(location.getLatitude()));
                map.put("longitude", String.valueOf(location.getLongitude()));

                mDatabaseReference.updateChildren(map).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        //Toast.makeText(UserProfile.this, "Location Tracked", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(UserProfile.this, "Cann't track your location. Check your internet connection and GPS setting", Toast.LENGTH_LONG).show();
                    }
                });

                mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserInfo info = dataSnapshot.getValue(UserInfo.class);

                        if(info.getName() != null) {
                            userName = info.getName();
                            markerOptions.title(info.getName());
                        }
                        else {
                            userName = info.getEmail();
                            //markerOptions.title(info.getEmail());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            getFriendLocationUpdate();
        }
    };

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    public String getUserLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        assert locationManager != null;
        List<String> providerList = locationManager.getAllProviders();
        if(null!=mLastLocation && null!=providerList && providerList.size()>0){
            final double longitude = mLastLocation.getLongitude();
            final double latitude = mLastLocation.getLatitude();
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
                String _Location;
                if(null!=listAddresses&&listAddresses.size()>0){
                    _Location = listAddresses.get(0).getAddressLine(0);
                    return _Location;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "Your Location";
    }

    public void getFriendLocationUpdate() {
        intent = getIntent();
        uid = intent.getStringExtra("uid");

        if(uid == null) {
            //Toast.makeText(this, "NULL", Toast.LENGTH_LONG).show();
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);

                if(userInfo.getName() != null)
                    name = userInfo.getName();
                else
                    name = userInfo.getEmail();

                if(userInfo.getLocationShare() != null && userInfo.getLocationShare().equals("true") && userInfo.getLatitude() != null && userInfo.getLongitude() != null) {
                    Location userLocation = new Location(LocationManager.GPS_PROVIDER);
                    userLocation.setLatitude(Double.valueOf(userInfo.getLatitude()));
                    userLocation.setLongitude(Double.valueOf(userInfo.getLongitude()));

                    LatLng userLtLn = new LatLng(Double.valueOf(userInfo.getLatitude()), Double.valueOf(userInfo.getLongitude()));

                    if(!zoomState) {
                        zoomState = true;
                        centerMapOnLocation(userLocation, name, 1);
                    }
                    if(friendLocationMarker != null)
                        friendLocationMarker.remove();

                    MarkerOptions markerOptions2 = new MarkerOptions();
                    markerOptions2.position(userLtLn);
                    markerOptions2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    markerOptions2.title(name);

                    friendLocationMarker = mMap.addMarker(markerOptions2);
                    friendLocationMarker.showInfoWindow();

                    getRouteToMarker(userLtLn);
                    //mMap.addMarker(new MarkerOptions().position(myLtLn).title(name)).showInfoWindow();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    ProgressDialog progressDialog;

    private void getRouteToMarker(LatLng userLtLn) {
        //progressDialog = ProgressDialog.show(this, "Please wait.",
                //"Fetching route information.", true);
        //progressDialog.show();

        erasePolyLines();

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.WALKING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), userLtLn)
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        //progressDialog.dismiss();
        Toast.makeText(this, "Couldn't find any route between you and your friend location.", Toast.LENGTH_LONG).show();
        e.printStackTrace();
    }

    @Override
    public void onRoutingStart() {
        //Toast.makeText(this, "Start", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        //progressDialog.dismiss();

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            Map map = new HashMap();
            map.put("Distance", route.get(i).getDistanceValue());
            map.put("Duration", route.get(i).getDurationValue());

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polyline.setClickable(true);
            polyline.setTag(map);

            polylines.add(polyline);

            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {
    }

    private void erasePolyLines() {
        for(Polyline line: polylines) {
            line.remove();
        }
        polylines.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Intent intent = new Intent(MapsActivity.this, SingleUserInfo.class);
        //intent.putExtra("uid", uid);
        //startActivity(intent);
        this.finish();
        System.exit(0);
    }
}
