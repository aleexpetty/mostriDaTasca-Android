package com.example.progetto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Style.OnStyleLoaded, PermissionsListener {

    public final String URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";
    TextView tv_lifePoints, tv_experiencePoints;
    SharedPreferences firstRun, firstRunMap, session_id, sharedLP, sharedXP;
    private MapView mapView;
    public MapboxMap mapboxMap;
    PermissionsManager permissionsManager;
    LocationManager locationManager;
    private LocationListeningCallback callback = new LocationListeningCallback(this);
    LocationEngine locationEngine;
    LocationComponent locationComponent;
    private LocationComponentActivationOptions locationComponentActivationOptions;
    private LocationComponentOptions customLocationComponentOptions;
    final JSONObject jsonBody = new JSONObject();
    SymbolManager symbolManager;
    Symbol symbol;
    public static final String SYMBOL_CANDY = "candy";
    public static final String SYMBOL_MONSTER = "monster";
    private boolean stopMapAsyncTask = false;
    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(!stopMapAsyncTask) {
                new getMapAsyncTask().execute();
                handler.postDelayed(this, 60000);
                Log.d("stopMapAsyncTask", "Eseguo Thread");
            }
            else
                Log.d("stopMapAsyncTask", "Non eseguo Thread");
        }
    };

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.ranking_btn:
                    Intent intentRanking = new Intent(getApplicationContext(), RankingActivity.class);
                    intentRanking.putExtra("session_id", session_id.getString("sid",null));
                    startActivity(intentRanking);
                    break;

                case R.id.profile_btn:
                    Intent intentProfile = new Intent(getApplicationContext(), ProfileActivity.class);
                    intentProfile.putExtra("session_id", session_id.getString("sid",null));
                    startActivity(intentProfile);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));

        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        locationManager =  (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        firstRun = getPreferences(Context.MODE_PRIVATE);
        firstRunMap = getPreferences(Context.MODE_PRIVATE);
        sharedLP = getPreferences(Context.MODE_PRIVATE);
        sharedXP = getPreferences(Context.MODE_PRIVATE);
        session_id = getPreferences(Context.MODE_PRIVATE);

        ImageButton ranking_btn = findViewById(R.id.ranking_btn);
        ImageButton profile_btn = findViewById(R.id.profile_btn);
        tv_lifePoints = findViewById(R.id.textView4);
        tv_experiencePoints = findViewById(R.id.textView5);

        ranking_btn.setOnClickListener(buttonListener);
        profile_btn.setOnClickListener(buttonListener);

        stopMapAsyncTask = false;
        //handler.postDelayed(runnable, 60000);
    }

    private boolean checkFirstRun(SharedPreferences sharedPref) {
        if (sharedPref.getBoolean("my_first_time", true)) {
            Log.d("FirstTime", "First time");
            sharedPref.edit().putBoolean("my_first_time", false).commit();
            return true;
        }
        else
            Log.d("FirstTime", "Not First time");
        return false;
    }


    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        //mapboxMap.setStyle(Style.MAPBOX_STREETS, this);
        //mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/alexpetty/ck4ibnasz02pb1co0a58rfex9"),this);
        mapboxMap.setStyle(new Style.Builder().fromUri("asset://style.json"),this);
    }

    @Override
    public void onStyleLoaded(Style style) {

        enableLocationComponent(style);

        new getMapAsyncTask().execute();

        style.addImage(SYMBOL_CANDY, BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.candy1));
        style.addImage(SYMBOL_MONSTER, BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.monster));

        symbolManager = new SymbolManager(mapView, mapboxMap, style);
        symbolManager.setIconAllowOverlap(true);
        symbolManager.setTextAllowOverlap(true);

        markerClickListener();
    }


    @Override
    protected void onStart() {
        super.onStart();
        stopMapAsyncTask = false;

        if(checkFirstRun(firstRun))
        {
            sharedLP.edit().putString("sharedLP", "100").commit();
            sharedXP.edit().putString("sharedXP", "0").commit();
            Log.d("FirstRun","Vado al profilo");
            getSessionId();
        }
        else
        {
            Log.d("NotFirstRun","Visualizzo la mappa");

            mapView.onStart();
            handler.postDelayed(runnable, 60000);

            getProfileInfo();

            permissionsManager = new PermissionsManager(this);

            /*
            if (PermissionsManager.areLocationPermissionsGranted(this)) {
                // Permission sensitive logic called here, such as activating the Maps SDK's LocationComponent to show the device's location

            } else {
                permissionsManager = new PermissionsManager(this);
                permissionsManager.requestLocationPermissions(this);
            }*/
        }
    }

    private void enableLocationComponent(Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
             if (PermissionsManager.areLocationPermissionsGranted(this)) {
                customLocationComponentOptions = LocationComponentOptions.builder(this)
                        .elevation(5)
                        .accuracyAlpha(.6f)
                        .accuracyColor(Color.GREEN)
                        .build();

                // Get an instance of the component
                locationComponent = mapboxMap.getLocationComponent();

                locationComponentActivationOptions =
                        LocationComponentActivationOptions.builder(this, loadedMapStyle)
                                .locationComponentOptions(customLocationComponentOptions)
                                .build();

                // Activate with a built LocationComponentActivationOptions object
                locationComponent.activateLocationComponent(locationComponentActivationOptions);
                // Enable to make component visible
                locationComponent.setLocationComponentEnabled(true);
                // Set the component's camera mode
                locationComponent.setCameraMode(CameraMode.TRACKING);
                // Set the component's render mode
                locationComponent.setRenderMode(RenderMode.COMPASS);
            }
            else {
                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(45.4642200, 9.1905600))
                        .zoom(12)
                        .tilt(20)
                        .build();

                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000);
            }
        }
        else {
            //Toast.makeText(MainActivity.this, "Segnale GPS assente", Toast.LENGTH_LONG).show();

            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(45.4642200, 9.1905600))
                    .zoom(12)
                    .tilt(20)
                    .build();

            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000);

            /*
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);*/
        }
    }

    public void GetUserPosition(View v) {
        Log.d("Click","click");
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            if (PermissionsManager.areLocationPermissionsGranted(this)) {
                enableLocationComponent(mapboxMap.getStyle());
                Log.d("Location", mapboxMap.getCameraPosition().toString());
            }
            else {
                permissionsManager = new PermissionsManager(this);
                permissionsManager.requestLocationPermissions(this);
            }
        }
        else
            Toast.makeText(MainActivity.this, "Segnale GPS disattivato", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
            Log.d("Permission","Permission granted");
            long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
            long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

            locationEngine = LocationEngineProvider.getBestLocationEngine(this);

            LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                    .build();

            locationEngine.requestLocationUpdates(request, callback, getMainLooper());
            locationEngine.getLastLocation(callback);

            Log.d("Location", mapboxMap.getCameraPosition().toString());
        } else {
            // User denied the permission
            Log.d("Permission", "Permission denied");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        stopMapAsyncTask = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        stopMapAsyncTask = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        stopMapAsyncTask = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void addMarkers(MapObject x) {
        Log.d("Marker", "Adding marker : " + x.getId());
        String id = x.getId();
        String nome = x.getName();
        String tipo = x.getType();
        Double lat = Double.parseDouble(x.getLatitude());
        Double lon = Double.parseDouble(x.getLongitude());
        Log.d("LatLon", lat+" "+lon);

        if(tipo.equals("CA")){
            symbol = symbolManager.create(new SymbolOptions()
                    .withLatLng(new LatLng(lat, lon))
                    .withIconImage(SYMBOL_CANDY)
                    .withTextAnchor(id)
            );
        }
        else if(tipo.equals("MO")) {
            symbol = symbolManager.create(new SymbolOptions()
                    .withLatLng(new LatLng(lat, lon))
                    .withIconImage(SYMBOL_MONSTER)
                    .withTextAnchor(id)
            );
        }
    }

    public void markerClickListener() {
        Log.d("Marker", "Adding listener");
        symbolManager.addClickListener(new OnSymbolClickListener() {
            @Override
            public void onAnnotationClick(Symbol symbol) {
                Log.d("Alert","Click on marker");

                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    if (PermissionsManager.areLocationPermissionsGranted(getApplicationContext())) {
                        String symbolId = symbol.getTextAnchor();
                        MapObject mapObject = MapObjectsModel.getInstance().getMapObjectById(symbolId);
                        Log.d("MARKER", "Marker "+ symbolId);

                        Double myLat = mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude();
                        Double myLong = mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude();
                        LatLng myPos = new LatLng(myLat, myLong);
                        LatLng markerPos = symbol.getLatLng();

                        Bundle bundle = new Bundle();
                        bundle.putString("id", mapObject.getId());
                        bundle.putString("sID", session_id.getString("sid", null));
                        bundle.putString("size", mapObject.getSize());
                        bundle.putString("name", mapObject.getName());
                        bundle.putParcelable("myPos", myPos);
                        bundle.putParcelable("markerPos", markerPos);

                        if (mapObject.getType().equals("CA")) {
                            addFragment(new FragmentCandy(), false, "fragmentCandy", bundle);
                            Log.d("Alert","AddFragment Candy");
                        } else if (mapObject.getType().equals("MO")) {
                            addFragment(new FragmentMonster(), false, "fragmentMonster", bundle);
                            Log.d("Alert","AddFragment Monster");
                        }
                    }
                    else
                    {
                        Log.d("NoPermission","NoPermission");
                        noPermissionDialog();
                    }
                }
                else
                    Toast.makeText(MainActivity.this, "Segnale GPS disattivato", Toast.LENGTH_LONG).show();
            }
        });
    }

    private class LocationListeningCallback implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<MainActivity> activityWeakReference;

        LocationListeningCallback(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            /*
            Location lastLocation = result.getLastLocation();

            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();

            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude))
                    .zoom(10)
                    .tilt(20)
                    .build();

            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);

            Log.d("LastLocation", lastLocation.toString());*/
        }

        @Override
        public void onFailure(@NonNull Exception exception) {

        }
    }

    public void addFragment(Fragment fragment, boolean addToBackStack, String tag, Bundle bundle) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void getSessionId() {
        final JsonObjectRequest request = new JsonObjectRequest(
                URL + "register.php",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String sID = response.getString("session_id");
                            session_id.edit().putString("sid", sID).commit();
                            Log.d("sID", "Session ID: "+sID);

                            Intent intentProfile = new Intent(getApplicationContext(), ProfileActivity.class);
                            intentProfile.putExtra("session_id", sID);
                            startActivity(intentProfile);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", "Error: " + error.toString());
                //Toast.makeText(MainActivity.this, "Richiesta fallita REGISTER", Toast.LENGTH_SHORT).show();
                noConnection();
            }
        });

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        queue.add(request);
    }

    private class getMapAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            final JSONObject jsonBody = new JSONObject();

            try {
                jsonBody.put("session_id", session_id.getString("sid",null));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            final JsonObjectRequest mapRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    URL + "getmap.php",
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray array = response.getJSONArray("mapobjects");

                                if(symbolManager != null){
                                    MapObjectsModel.getInstance().removeAll();
                                    symbolManager.deleteAll();
                                }

                                for (int i = 0; i < array.length(); i++) {
                                    String id = array.getJSONObject(i).getString("id");
                                    String lat = array.getJSONObject(i).getString("lat");
                                    String lon = array.getJSONObject(i).getString("lon");
                                    String type = array.getJSONObject(i).getString("type");
                                    String size = array.getJSONObject(i).getString("size");
                                    String name = array.getJSONObject(i).getString("name");

                                    MapObject x = new MapObject(id, lat, lon, type, size, name);
                                    MapObjectsModel.getInstance().addMapObject(x);
                                    addMarkers(x);

                                    Log.d("VolleyMapObjectsModel-Add", x.toString());
                                }

                            }   catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d("Volley","Correct: "+ response.toString());
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Volley","Error: "+error.toString());
                            //Toast.makeText(MainActivity.this, "Richiesta fallita", Toast.LENGTH_SHORT).show();
                            noConnection();
                        }
                    });

            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            queue.add(mapRequest);

            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            super.onPostExecute(voids);
        }
    }

    public void noPermissionDialog() {
        permissionsManager = new PermissionsManager(permissionsManager.getListener());
        permissionsManager.requestLocationPermissions(MainActivity.this);
    }

    public void noConnection() {
        Toast.makeText(MainActivity.this, R.string.errorNoConnection, Toast.LENGTH_LONG).show();
    }

    public void getMapObjects()
    {
        Log.d("getMapObjects", "getMapObjects");
        new getMapAsyncTask().execute();
    }

    public void getProfileInfo() {
        final JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("session_id", session_id.getString("sid",null));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest request = new JsonObjectRequest(
                URL + "getprofile.php",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response",response.toString());
                        try {
                            String lp = response.getString("lp");
                            String xp = response.getString("xp");

                            sharedLP.edit().putString("sharedLP", lp).commit();
                            sharedXP.edit().putString("sharedXP", xp).commit();

                            tv_lifePoints.setText(lp);
                            tv_experiencePoints.setText(xp);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", "Error: " + error.toString());
                noConnection();
            }
        });
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        queue.add(request);
    }
}