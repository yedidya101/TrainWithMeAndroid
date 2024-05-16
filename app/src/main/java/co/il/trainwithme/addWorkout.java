package co.il.trainwithme;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class addWorkout extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {
    private final int FINE_PERMISSION_CODE = 1;
    Location CurrentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    GoogleMap mymap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {

            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    CurrentLocation = location;

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(addWorkout.this);
                }
            }
        });
      }

      @Override
    public void onClick(View v) {

    }


      @Override
      public void onMapReady(@NonNull GoogleMap googleMap) {
            mymap = googleMap;

            mymap.addMarker(new MarkerOptions().position(new LatLng(CurrentLocation.getLatitude(), CurrentLocation.getLongitude())).title("My Location"));
            mymap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(CurrentLocation.getLatitude(), CurrentLocation.getLongitude()), 15f));
      }
       public void OnRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
       {
           super.onRequestPermissionsResult(requestCode, permissions, grantResults);
           if(requestCode == FINE_PERMISSION_CODE)
           {
               if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
               {
                   getLastLocation();
               }
               else
               {
                   Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
               }
           }
       }
  }