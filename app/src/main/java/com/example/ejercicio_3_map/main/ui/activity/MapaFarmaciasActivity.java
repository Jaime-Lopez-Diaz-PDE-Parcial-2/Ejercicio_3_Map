package com.example.ejercicio_3_map.main.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ejercicio_3_map.R;
import com.example.ejercicio_3_map.main.model.Farmacia;
import com.example.ejercicio_3_map.main.repos.firebase.FarmaciaHandler;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapaFarmaciasActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private FarmaciaHandler farmaciaHandler;
    private List<Farmacia> farmacias;
    private List<Marker> markers;
    private EditText searchBar;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_farmacias);

        farmaciaHandler = new FarmaciaHandler();
        farmacias = new ArrayList<>();
        markers = new ArrayList<>();
        searchBar = findViewById(R.id.searchBar);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        verificarServiciosDeUbicacion();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        configurarBuscador();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            centrarMapaEnUbicacion();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }

        cargarFarmaciasEnMapa();
        map.setOnMarkerClickListener(marker -> {
            mostrarInformacionFarmacia(marker);
            return false;
        });
    }

    @SuppressLint("MissingPermission")
    private void centrarMapaEnUbicacion() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000)
                .setFastestInterval(1000);

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Si se obtiene la última ubicación conocida
                        LatLng ubicacionUsuario = new LatLng(location.getLatitude(), location.getLongitude());
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionUsuario, 15));
                    } else {
                        // Solicitar actualizaciones si no se puede obtener la última ubicación conocida
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                if (!locationResult.getLocations().isEmpty()) {
                                    Location location = locationResult.getLastLocation();
                                    LatLng ubicacionUsuario = new LatLng(location.getLatitude(), location.getLongitude());
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionUsuario, 15));
                                    fusedLocationProviderClient.removeLocationUpdates(this);
                                }
                            }
                        }, null);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener la ubicación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarFarmaciasEnMapa() {
        farmaciaHandler.obtenerFarmacias(new FarmaciaHandler.FarmaciaCallback() {
            @Override
            public void onSuccess(List<Farmacia> farmaciasCargadas) {
                runOnUiThread(() -> {
                    farmacias.clear();
                    markers.clear();
                    farmacias.addAll(farmaciasCargadas);

                    for (Farmacia farmacia : farmacias) {
                        LatLng ubicacion = new LatLng(farmacia.getLatitud(), farmacia.getLongitud());
                        Marker marker = map.addMarker(new MarkerOptions()
                                .position(ubicacion)
                                .title(farmacia.getNombre())
                                .snippet(farmacia.getDireccion())
                                .icon(bitmapDescriptorFromVector(R.drawable.ic_map)));
                        markers.add(marker);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(MapaFarmaciasActivity.this, "Error al cargar farmacias: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void configurarBuscador() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filtrarFarmacias(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void filtrarFarmacias(String query) {
        for (Marker marker : markers) {
            marker.setVisible(false);
        }

        for (int i = 0; i < farmacias.size(); i++) {
            Farmacia farmacia = farmacias.get(i);
            if (farmacia.getNombre().toLowerCase().contains(query.toLowerCase()) ||
                    farmacia.getDireccion().toLowerCase().contains(query.toLowerCase())) {
                markers.get(i).setVisible(true);
            }
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void mostrarInformacionFarmacia(Marker marker) {
        Toast.makeText(this, "Farmacia: " + marker.getTitle() + "\nDirección: " + marker.getSnippet(), Toast.LENGTH_LONG).show();
    }

    private void verificarServiciosDeUbicacion() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(builder.build())
                .addOnFailureListener(exception -> {
                    if (exception instanceof ResolvableApiException) {
                        try {
                            ((ResolvableApiException) exception).startResolutionForResult(this, 101);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(this, "Por favor, activa los servicios de ubicación", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                map.setMyLocationEnabled(true);
                centrarMapaEnUbicacion();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
