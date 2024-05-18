package co.il.trainwithme;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
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

import java.util.Calendar;

public class addWorkout extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {
    private final int FINE_PERMISSION_CODE = 1;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap myMap;
    private LatLng selectedLocation;

    private ImageButton btnRunning, btnBasketball, btnPowerWorkout, btnBicycleRide;
    private Button btnChooseLocation, btnSetWorkout, btnChooseDate, btnChooseTime;
    private Switch switchPrivateWorkout, ageFilterSwitch;
    private RadioGroup radioGroupGender;
    private SeekBar ageRangeSeekBar;
    private TextView ageRangeTextView, tvChosenWorkoutType;
    private ImageButton personal, createWorkout2, scoreboard2, homepage2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);

        // Initialize buttons
        btnRunning = findViewById(R.id.btnRunning);
        btnBasketball = findViewById(R.id.btnBasketball);
        btnPowerWorkout = findViewById(R.id.btnPowerWorkout);
        btnBicycleRide = findViewById(R.id.btnBicycleRide);
        btnChooseLocation = findViewById(R.id.btnChooseLocation);
        btnSetWorkout = findViewById(R.id.btnSetWorkout);
        btnChooseDate = findViewById(R.id.btnChooseDate);
        btnChooseTime = findViewById(R.id.btnChooseTime);

        // Set click listeners
        btnRunning.setOnClickListener(this);
        btnBasketball.setOnClickListener(this);
        btnPowerWorkout.setOnClickListener(this);
        btnBicycleRide.setOnClickListener(this);
        btnChooseLocation.setOnClickListener(this);
        btnSetWorkout.setOnClickListener(this);
        btnChooseDate.setOnClickListener(this);
        btnChooseTime.setOnClickListener(this);

        // Initialize other views
        switchPrivateWorkout = findViewById(R.id.switchPrivateWorkout);
        ageFilterSwitch = findViewById(R.id.ageFilterSwitch);
        ageRangeSeekBar = findViewById(R.id.ageRangeSeekBar);
        ageRangeTextView = findViewById(R.id.ageRangeTextView);
        tvChosenWorkoutType = findViewById(R.id.tvChosenWorkoutType);
        radioGroupGender = findViewById(R.id.radioGroupGender);

        // Initialize bottom navigation buttons
        personal = findViewById(R.id.personal);
        createWorkout2 = findViewById(R.id.createWorkout2);
        scoreboard2 = findViewById(R.id.scoreboard2);
        homepage2 = findViewById(R.id.homepage2);

        personal.setOnClickListener(this);
        createWorkout2.setOnClickListener(this);
        scoreboard2.setOnClickListener(this);
        homepage2.setOnClickListener(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup age filter functionality
        ageFilterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ageRangeTextView.setVisibility(View.VISIBLE);
                ageRangeSeekBar.setVisibility(View.VISIBLE);
            } else {
                ageRangeTextView.setVisibility(View.GONE);
                ageRangeSeekBar.setVisibility(View.GONE);
                ageRangeTextView.setText("Age range: Any");
            }
        });

        ageRangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ageRangeTextView.setText("Age range: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrame);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Request location permission if not already granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
        } else {
            // Permission already granted, get the last known location
            getLastLocationAndInitializeMap();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnRunning) {
            tvChosenWorkoutType.setText("Chosen Workout Type: Running");
        } else if (id == R.id.btnBasketball) {
            tvChosenWorkoutType.setText("Chosen Workout Type: Basketball");
        } else if (id == R.id.btnPowerWorkout) {
            tvChosenWorkoutType.setText("Chosen Workout Type: Strength Workout");
        } else if (id == R.id.btnBicycleRide) {
            tvChosenWorkoutType.setText("Chosen Workout Type: Bicycle Ride");
        } else if (id == R.id.btnChooseLocation) {
            getLastLocationAndInitializeMap();
        } else if (id == R.id.btnSetWorkout) {
            // Logic to save workout with selected options
        } else if (id == R.id.btnChooseDate) {
            showDatePicker();
        } else if (id == R.id.btnChooseTime) {
            showTimePicker();
        } else if (id == R.id.personal) {
            startActivity(new Intent(this, PersonalArea.class));
        } else if (id == R.id.createWorkout2) {
            startActivity(new Intent(this, addWorkout.class));
        } else if (id == R.id.scoreboard2) {
            startActivity(new Intent(this, ScoreBoard.class));
        } else if (id == R.id.homepage2) {
            startActivity(new Intent(this, HomePage.class));
        }
    }

    private void getLastLocationAndInitializeMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrame);
                    if (mapFragment != null) {
                        mapFragment.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(@NonNull GoogleMap googleMap) {
                                myMap = googleMap;
                                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                                myMap.addMarker(new MarkerOptions().position(currentLatLng).title("My Location"));
                                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                myMap.clear();
                myMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
                selectedLocation = latLng;
            }
        });

        // Check if currentLocation is already available
        if (currentLocation != null) {
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            myMap.addMarker(new MarkerOptions().position(currentLatLng).title("My Location"));
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
        } else {
            // If currentLocation is not yet available, get it
            getLastLocationAndInitializeMap();
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Handle date selection
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            // Handle time selection
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocationAndInitializeMap();
            } else {
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
