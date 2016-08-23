package com.evansappwriter.italianrestaurantsmap;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.evansappwriter.italianrestaurantsmap.core.BundledData;
import com.evansappwriter.italianrestaurantsmap.core.MapService;
import com.evansappwriter.italianrestaurantsmap.core.Parser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        mMap.setMyLocationEnabled(true);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);

        Bundle params = new Bundle();
        params.putString(MapService.PARAM_QUERY, "Italian+Restaurant");
        params.putString(MapService.PARAM_LOCATION, location.getLatitude()+","+location.getLongitude());
        params.putString(MapService.PARAM_RADIUS, "5000");
        params.putString(MapService.PARAM_TYPES, "restaurant");
        params.putString(MapService.PARAM_SENSOR, "true");
        MapService.getInstance().get(params,new MapService.OnUIResponseHandler() {

            @Override
            public void onSuccess(String payload) {
                if (this == null || isFinishing()) {
                    return;
                }

                List<HashMap<String, String>> places = null;
                if (payload != null) {
                    BundledData data = new BundledData(Parser.TYPE_PARSER_RESTAURANTS);
                    data.setHttpData(payload);
                    Parser.parseResponse(data);

                    if (data.getAuxData() == null) {

                    } else {
                        places = (List<HashMap<String, String>>)data.getAuxData()[0];

                        // Clears all the existing markers
                        mMap.clear();

                        for(int i=0;i<places.size();i++){

                            // Creating a marker
                            MarkerOptions markerOptions = new MarkerOptions();

                            // Getting a place from the places list
                            HashMap<String, String> hmPlace = places.get(i);

                            // Getting latitude of the place
                            double lat = Double.parseDouble(hmPlace.get("lat"));

                            // Getting longitude of the place
                            double lng = Double.parseDouble(hmPlace.get("lng"));

                            // Getting name
                            String name = hmPlace.get("place_name");

                            // Getting vicinity
                            String vicinity = hmPlace.get("vicinity");

                            LatLng latLng = new LatLng(lat, lng);

                            // Setting the position for the marker
                            markerOptions.position(latLng);

                            // Setting the title for the marker.
                            //This will be displayed on taping the marker
                            markerOptions.title(name + " : " + vicinity);

                            // Getting snippet
                            String snippet = hmPlace.get("snippet");
                            markerOptions.snippet(snippet);

                            // Placing a marker on the touched position
                            mMap.addMarker(markerOptions);
                        }
                    }
                } else {

                }
            }

            @Override
            public void onFailure(String errorTitle, String errorText, int dialogId) {
                if (this == null || isFinishing()) {
                    return;
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
