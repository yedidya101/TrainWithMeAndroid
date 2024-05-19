package co.il.trainwithme;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class addWorkout extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {
    private boolean isAgeFiltered = false;
    private String workoutDate, workoutTime, workoutDuration, FullName, gender;
    private String userID, firstName, lastName, WorkoutType;
    private int duration = 0;
    private int workoutParticipated = 0;
    private final int FINE_PERMISSION_CODE = 1;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap myMap;
    private LatLng selectedLocation;

    private ImageButton btnRunning, btnBasketball, btnPowerWorkout, btnBicycleRide;
    private Button btnChooseLocation, btnSetWorkout, btnChooseDate, btnChooseTime, btnChooseDuration;
    private Switch switchPrivateWorkout, ageFilterSwitch;
    private RadioGroup radioGroupGender;
    private RadioButton radioButton;
    private SeekBar ageRangeSeekBar;
    private TextView ageRangeTextView, tvChosenWorkoutType;
    private ImageButton personal, createWorkout2, scoreboard2, homepage2;
    private Calendar selectedDate;
    private TimePickerDialog timePickerDialog;

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
        btnChooseDuration = findViewById(R.id.btnChooseDuration);

        // Set click listeners
        btnRunning.setOnClickListener(this);
        btnBasketball.setOnClickListener(this);
        btnPowerWorkout.setOnClickListener(this);
        btnBicycleRide.setOnClickListener(this);
        btnChooseLocation.setOnClickListener(this);
        btnSetWorkout.setOnClickListener(this);
        btnChooseDate.setOnClickListener(this);
        btnChooseTime.setOnClickListener(this);
        btnChooseDuration.setOnClickListener(this);

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
                isAgeFiltered = true;
                ageRangeTextView.setVisibility(View.VISIBLE);
                ageRangeSeekBar.setVisibility(View.VISIBLE);
            } else {
                isAgeFiltered = false;
                ageRangeTextView.setVisibility(View.GONE);
                ageRangeSeekBar.setVisibility(View.GONE);
            }
        });

        ageRangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ageRangeTextView.setText("Age range: " + progress + "+");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fetchUserDetails();
        getLastLocationAndInitializeMap();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnRunning) {
            tvChosenWorkoutType.setText("Chosen Workout Type: Running");
            WorkoutType = "run";
        } else if (id == R.id.btnBasketball) {
            tvChosenWorkoutType.setText("Chosen Workout Type: Basketball");
            WorkoutType = "basketball";
        } else if (id == R.id.btnPowerWorkout) {
            tvChosenWorkoutType.setText("Chosen Workout Type: Strength Workout");
            WorkoutType = "power";
        } else if (id == R.id.btnBicycleRide) {
            tvChosenWorkoutType.setText("Chosen Workout Type: Bicycle Ride");
            WorkoutType = "ride";
        } else if (id == R.id.btnChooseLocation) {
            getLastLocationAndInitializeMap();
        } else if (id == R.id.btnSetWorkout) {
            saveWorkoutToFirestore();
        } else if (id == R.id.btnChooseDate) {
            showDatePickerDialog();
        } else if (id == R.id.btnChooseTime) {
            showTimePicker();
        } else if (id == R.id.btnChooseDuration) {
            showDurationPicker();
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

    private void fetchUserDetails() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userID);
        if (docRef != null) {
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        firstName = documentSnapshot.getString("firstName");
                        lastName = documentSnapshot.getString("lastName");
                        workoutParticipated = documentSnapshot.getLong("workoutJoined").intValue();
                        FullName = firstName + " " + lastName;
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(addWorkout.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error fetching user data", e);
                }
            });
        }
    }

    private void saveWorkoutToFirestore() {
        gender = null;
        int selectedId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            gender = selectedRadioButton.getText().toString();
        }

        if (FullName == null) {
            Toast.makeText(this, "User data not loaded yet, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (WorkoutType == null) {
            Toast.makeText(this, "Please choose a workout type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gender == null) {
            Toast.makeText(this, "Please choose your gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (duration == 0) {
            Toast.makeText(this, "Please choose a workout duration", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isAgeFiltered && ageRangeTextView.getText().toString().equals("Age range: ")) {
            Toast.makeText(this, "Please choose minimal age", Toast.LENGTH_SHORT).show();
            return;
        }

        // Increment workoutParticipated before saving workout data
        workoutParticipated += 1;

        Map<String, Object> workoutData = new HashMap<>();
        workoutData.put("workoutType", WorkoutType);
        workoutData.put("privateWorkout", switchPrivateWorkout.isChecked());
        workoutData.put("date", workoutDate);
        workoutData.put("time", workoutTime);
        workoutData.put("duration", duration);
        workoutData.put("ageFiltered", isAgeFiltered);
        workoutData.put("creatorId", userID);
        workoutData.put("Participants", 1);
        workoutData.put("creatorName", FullName);
        workoutData.put("gender", gender);
        if (isAgeFiltered) {
            workoutData.put("minimumAge", ageRangeTextView.getText().toString().replace("Age range: ", ""));
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Update the user's workoutParticipated count first
        DocumentReference userDocRef = db.collection("users").document(userID);
        userDocRef.update("workoutJoined", workoutParticipated)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Now add the workout data
                        db.collection("workouts")
                                .add(workoutData)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(addWorkout.this, "Workout added successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(addWorkout.this, HomePage.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(addWorkout.this, "Error adding workout", Toast.LENGTH_SHORT).show();
                                        Log.e("Firestore", "Error adding workout", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(addWorkout.this, "Error updating workout count", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error updating workout count", e);
                    }
                });
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

        if (currentLocation != null) {
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            myMap.addMarker(new MarkerOptions().position(currentLatLng).title("My Location"));
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
        } else {
            getLastLocationAndInitializeMap();
        }
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int chosenyear, int chosenmonth, int dayOfMonth) {
                        selectedDate = Calendar.getInstance();
                        selectedDate.set(chosenyear, chosenmonth, dayOfMonth);
                        workoutDate = dayOfMonth + "/" + (chosenmonth + 1) + "/" + chosenyear;
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
                        workoutTime = hourOfDay + ":" + (minute < 10 ? "0" + minute : minute);
                        Toast.makeText(addWorkout.this, "Chosen Time: " + workoutTime, Toast.LENGTH_SHORT).show();
                    }
                }, currentHour, currentMinute, true);
        timePickerDialog.show();
    }

    private void showDurationPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_duration_picker, null);
        builder.setView(dialogView);

        NumberPicker npHours = dialogView.findViewById(R.id.np_hours);
        NumberPicker npMinutes = dialogView.findViewById(R.id.np_minutes);
        Button btnSetDuration = dialogView.findViewById(R.id.btn_set_duration);

        npHours.setMinValue(0);
        npHours.setMaxValue(23);
        npMinutes.setMinValue(0);
        npMinutes.setMaxValue(59);

        final AlertDialog dialog = builder.create();

        btnSetDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hours = npHours.getValue();
                int minutes = npMinutes.getValue();
                duration = hours * 60 + minutes;

                if(hours == 0 && minutes == 0) {
                    Toast.makeText(getApplicationContext(), "Please select duration", Toast.LENGTH_SHORT).show();
                    return;
                }
                String workoutDuration = hours + " hours " + minutes + " minutes";
                Toast.makeText(getApplicationContext(), "Chosen Duration: " + workoutDuration, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
