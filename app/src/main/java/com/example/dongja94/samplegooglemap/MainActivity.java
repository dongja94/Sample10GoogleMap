package com.example.dongja94.samplegooglemap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback ,
        GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    GoogleMap map;
    LocationManager mLM;
    String mProvider = LocationManager.GPS_PROVIDER;
    EditText keywordView;
    ListView listView;
    ArrayAdapter<POIItem> mAdapter;
    RadioGroup typeView;

    POIItem start, end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        typeView = (RadioGroup)findViewById(R.id.group_type);
        keywordView = (EditText)findViewById(R.id.edit_keyword);
        listView = (ListView)findViewById(R.id.listView);
        mAdapter = new ArrayAdapter<POIItem>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                POIItem item = (POIItem) listView.getItemAtPosition(position);
                final Marker marker = markerResolver.get(item);
                CameraUpdate update = CameraUpdateFactory.newLatLng(marker.getPosition());
                map.animateCamera(update, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                       marker.showInfoWindow();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        });

        Button btn = (Button)findViewById(R.id.btn_marker);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map == null) return;
                CameraPosition postion = map.getCameraPosition();
                POIItem item = new POIItem();
                item.title = "MyMarker";
                item.subtitle = "marker test";
                item.description = "marker description";
                addMarker(postion.target, item);
            }
        });

        btn = (Button)findViewById(R.id.btn_search);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = keywordView.getText().toString();
                if (!TextUtils.isEmpty(keyword)) {
                    NetworkManager.getInstance().getTmapPOI(MainActivity.this, keyword, new NetworkManager.OnResultListener<TMapPOIList>() {
                        @Override
                        public void onSuccess(Request request, TMapPOIList result) {
                            clearAllMarker();
                            for (POIItem item : result.pois.poi) {
                                LatLng latLng = new LatLng(item.latitude, item.longitude);
                                addMarker(latLng, item);
                            }
                        }

                        @Override
                        public void onFailure(Request request, int code, Throwable cause) {

                        }
                    });
                }
            }
        });

        btn = (Button)findViewById(R.id.btn_route);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start != null && end != null) {
                    NetworkManager.getInstance().getCarRouteInfo(MainActivity.this, start.latitude, start.longitude, end.latitude, end.longitude,
                            new NetworkManager.OnResultListener<CarRouteInfo>(){
                                @Override
                                public void onSuccess(Request request, CarRouteInfo result) {
                                    displayRoute(result);
                                }

                                @Override
                                public void onFailure(Request request, int code, Throwable cause) {

                                }
                            });
                } else {
                    Toast.makeText(MainActivity.this, "start or end is null", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    private void displayRoute(CarRouteInfo info) {
        PolylineOptions options = new PolylineOptions();
        for (CarFeature feature : info.features) {
            if (feature.geometry.type.equals("LineString")) {
                for (int i = 0; i < feature.geometry.coordinates.length; i+=2) {
                    LatLng latLng = new LatLng(feature.geometry.coordinates[i+1], feature.geometry.coordinates[i]);
                    options.add(latLng);
                }
            }
        }
        int totalTime = info.features.get(0).properties.totalTime;
        int totalDistance = info.features.get(0).properties.totalDistance;
        options.color(Color.RED);
        options.width(10);
        map.addPolyline(options);
        Toast.makeText(this, "total : " + totalTime + "," + totalDistance,Toast.LENGTH_SHORT).show();
    }

    private void clearAllMarker() {
        for (int i = 0; i < mAdapter.getCount(); i++) {
            POIItem item = mAdapter.getItem(i);
            Marker marker = markerResolver.get(item);
            marker.remove();
        }
        mAdapter.clear();
    }

    Map<POIItem, Marker> markerResolver = new HashMap<>();
    Map<Marker, POIItem> poiResolver = new HashMap<>();

    private void addMarker(LatLng position, POIItem data) {
        MarkerOptions options = new MarkerOptions();
        options.position(position);
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
        options.icon(icon);
        options.anchor(0.5f, 1);
        options.title(data.title);
        options.snippet(data.subtitle);
        options.draggable(true);

        Marker marker = map.addMarker(options);
        markerResolver.put(data, marker);
        poiResolver.put(marker, data);
        mAdapter.add(data);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = mLM.getLastKnownLocation(mProvider);
        if (location != null) {
            mListener.onLocationChanged(location);
        }

        mLM.requestSingleUpdate(mProvider, mListener, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLM.removeUpdates(mListener);
    }

    LocationListener mListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            moveMap(location.getLatitude(), location.getLongitude());
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
    };


    private void moveMap(double lat, double lng) {
        if (map == null) {
            return;
        }

//        CameraPosition position = new CameraPosition.Builder()
//                .target(new LatLng(lat, lng))
//                .zoom(17)
//                .bearing(15)
//                .tilt(30)
//                .build();
//        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 17);
        map.moveCamera(update);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
//        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnCameraChangeListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setInfoWindowAdapter(new MyInfoWindowAdapter(this, poiResolver));
//        map.getUiSettings().setScrollGesturesEnabled(false);
    }

    private static final String TAG = "MainActivity";
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.i(TAG, "camera change");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        POIItem item = poiResolver.get(marker);
        Toast.makeText(this, "description : " + item.description, Toast.LENGTH_SHORT).show();
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "info window click", Toast.LENGTH_SHORT).show();
        POIItem item = poiResolver.get(marker);
        switch (typeView.getCheckedRadioButtonId()) {
            case R.id.radio_start :
                start = item;
                break;
            case R.id.radio_end :
                end = item;
                break;
        }
        marker.hideInfoWindow();
    }
}
