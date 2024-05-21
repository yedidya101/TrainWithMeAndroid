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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class addWorkout extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {
    private boolean isAgeFiltered = false;
    private String workoutDate, workoutTime, workoutDuration, FullName, gender;
    private String userID, firstName, lastName, WorkoutType;
    private int duration = 0;
    private int workoutParticipated = 0, workoutCreated = 0;
    private final int FINE_PERMISSION_CODE = 1;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap myMap;
    private LatLng selectedLocation;
    private PopupWindow popupWindow;
    private GoogleMap map;
    private String typeChosen;

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
    private View lastSelectedButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);
        Log.d("addWorkout", "onCreate started");
        try{
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
        //getLastLocationAndInitializeMap();
    } catch (Exception e){
            Log.e("addWorkout", "Error in onCreate: " + e.getMessage());

        }

    }

    // Set workout typeChosen
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnRunning || id == R.id.btnBasketball || id == R.id.btnPowerWorkout || id == R.id.btnBicycleRide) {
            handleWorkoutTypeSelection(v, id);
        }
        else if (id == R.id.btnChooseLocation) {
            showMapPopup();
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

    private void handleWorkoutTypeSelection(View v, int id) {
        // If there was a previously selected button, revert its image to the original
        if (lastSelectedButton != null) {
            // Revert the background to the original image of the last selected button
            if (lastSelectedButton.getId() == R.id.btnRunning) {
                lastSelectedButton.setBackgroundResource(R.drawable.running);
            } else if (lastSelectedButton.getId() == R.id.btnBasketball) {
                lastSelectedButton.setBackgroundResource(R.drawable.player);
            } else if (lastSelectedButton.getId() == R.id.btnPowerWorkout) {
                lastSelectedButton.setBackgroundResource(R.drawable.lifting);
            } else if (lastSelectedButton.getId() == R.id.btnBicycleRide) {
                lastSelectedButton.setBackgroundResource(R.drawable.ride);
            }
        }

        // Apply the semi-transparent overlay to the current button
        if (id == R.id.btnRunning) {
            v.setBackgroundResource(R.drawable.semi_transparent_running);
            tvChosenWorkoutType.setText("Chosen Workout Type: Running");
            WorkoutType = "run";
        } else if (id == R.id.btnBasketball) {
            v.setBackgroundResource(R.drawable.semi_transparent_basketball);
            tvChosenWorkoutType.setText("Chosen Workout Type: Basketball");
            WorkoutType = "basketball";
        } else if (id == R.id.btnPowerWorkout) {
            v.setBackgroundResource(R.drawable.semi_transparent_powerworkout);
            tvChosenWorkoutType.setText("Chosen Workout Type: Strength Workout");
            WorkoutType = "power";
        } else if (id == R.id.btnBicycleRide) {
            v.setBackgroundResource(R.drawable.semi_transparent_bicycleride);
            tvChosenWorkoutType.setText("Chosen Workout Type: Bicycle Ride");
            WorkoutType = "ride";
        }

        // Update the last selected button
        lastSelectedButton = v;
    }

    private void showMapPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_map, null);

        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.showAtLocation(findViewById(R.id.addWorkoutLayout), Gravity.CENTER, 0, 0);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button btnSaveLocation = popupView.findViewById(R.id.btnSaveLocation);
        btnSaveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedLocation != null) {
                    // Handle the selected location (e.g., save it to the workout details)
                    Toast.makeText(addWorkout.this, "Location saved ", Toast.LENGTH_SHORT).show();
                    // Dismiss the popup
                    popupWindow.dismiss();
                } else {
                    Toast.makeText(addWorkout.this, "Please select a location", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                        gender = documentSnapshot.getString("Gender");
                        Long participated = documentSnapshot.getLong("WorkoutParticipated");
                        Long created = documentSnapshot.getLong("WorkoutCreated");

                        if (participated != null) {
                            workoutParticipated = participated.intValue();
                        } else {
                            workoutParticipated = 0; // Default value if not available
                        }

                        if (created != null) {
                            workoutCreated = created.intValue();
                        } else {
                            workoutCreated = 0; // Default value if not available
                        }

                        FullName = firstName + " " + lastName;
                    } else {
                        Log.d("addWorkout", "No such document");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Document", "get failed with ", e);
                }
            });
        }
    }

    private void saveWorkoutToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("workouts").document();
        Map<String, Object> workout = new HashMap<>();
        workout.put("FullName", FullName);
        workout.put("Type", WorkoutType);
        workout.put("Date", workoutDate);
        workout.put("Time", workoutTime);
        workout.put("Location", selectedLocation);
        workout.put("CreatorID", userID);
        workout.put("Private", switchPrivateWorkout.isChecked());
        workout.put("GenderFilter", getSelectedGender());
        workout.put("AgeFilter", isAgeFiltered ? ageRangeSeekBar.getProgress() : 0);
        workout.put("Duration", duration);
        workout.put("Participants", new ArrayList<String>());

        docRef.set(workout).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(addWorkout.this, "Workout saved successfully", Toast.LENGTH_SHORT).show();
                updateWorkoutCreatedCount();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(addWorkout.this, "Error saving workout", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSelectedGender() {
        int selectedId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedId != -1) {
            radioButton = findViewById(selectedId);
            return radioButton.getText().toString();
        }
        return "All";
    }

    private void updateWorkoutCreatedCount() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userID);
        workoutCreated += 1;
        docRef.update("WorkoutCreated", workoutCreated).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Document", "WorkoutCreated updated successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Document", "Error updating WorkoutCreated", e);
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        workoutDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        timePickerDialog = new TimePickerDialog(addWorkout.this,
                (view, hourOfDay, minuteOfHour) -> workoutTime = hourOfDay + ":" + minuteOfHour, hour, minute, true);
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
                btnChooseDuration.setText(duration + " minutes");

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


    /*private void getLastLocationAndInitializeMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
                    if (mapFragment != null) {
                        mapFragment.getMapAsync(addWorkout.this);
                    }
                    else {
                    Log.e("addWorkout", "Map fragment is null");
                    Toast.makeText(addWorkout.this, "Map fragment is null", Toast.LENGTH_SHORT).show();}
                    }
            }
        });
    } */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        map.setMyLocationEnabled(true);

        if (currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                map.clear();
                map.addMarker(new MarkerOptions().position(latLng));
                selectedLocation = latLng;
            }
        });
    }

}
