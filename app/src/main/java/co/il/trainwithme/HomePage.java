package co.il.trainwithme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomePage extends AppCompatActivity implements View.OnClickListener {
    ImageButton addWorkoutButton;
    ImageButton personalAreaButton;
    ImageButton ScoreBoardButton;
    ImageButton homeButton, logoutButton;
    TextView emailNotVerifiedText;
    Button verifyNowButton;
    Button filterButton;

    private FirebaseAuth fAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore fStore;
    private String filterGender, myUsername;
    private boolean filterAgeOn;
    private int filterAgeRange;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private double latitude, longitude;
    private HashMap <Double, String> location;
    private Long participated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        addWorkoutButton = findViewById(R.id.createWorkout);
        personalAreaButton = findViewById(R.id.personal);
        ScoreBoardButton = findViewById(R.id.scoreboard);
        homeButton = findViewById(R.id.homepage);
        emailNotVerifiedText = findViewById(R.id.emailNotVerifiedText);
        verifyNowButton = findViewById(R.id.verifyNowButton);
        filterButton = findViewById(R.id.filterButton);
        logoutButton = findViewById(R.id.logoutbutton);

        addWorkoutButton.setOnClickListener(this);
        personalAreaButton.setOnClickListener(this);
        ScoreBoardButton.setOnClickListener(this);
        homeButton.setOnClickListener(this);
        verifyNowButton.setOnClickListener(this);
        filterButton.setOnClickListener(this);
        logoutButton.setOnClickListener(this);

        homeButton.setBackgroundResource(R.drawable.homepagechosen);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        currentUser = fAuth.getCurrentUser();

        // Initialize filters
        filterGender = "Any";
        filterAgeOn = false;
        filterAgeRange = 0;

        // Check if email is verified
        if (currentUser != null && !currentUser.isEmailVerified()) {
            emailNotVerifiedText.setVisibility(View.VISIBLE);
            verifyNowButton.setVisibility(View.VISIBLE);
        } else {
            emailNotVerifiedText.setVisibility(View.GONE);
            verifyNowButton.setVisibility(View.GONE);
        }

        // Load workouts from Firestore
        loadWorkouts();
    }

    private void loadWorkouts() { // Load workouts from Firestore
        fStore.collection("workouts").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            LinearLayout workoutListContainer = findViewById(R.id.workout_list_container);
                            workoutListContainer.removeAllViews();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> workout = document.getData();

                                if (applyFiltersToWorkout(workout)) {
                                    createWorkoutButton(workoutListContainer, workout);
                                }
                            }
                        } else {
                            Toast.makeText(HomePage.this, "Failed to load workouts.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean applyFiltersToWorkout(Map<String, Object> workout) { // Apply filters on aviable workouts in homepage
        // Apply the filters to each workout and return true if the workout matches the filters
        if (!filterGender.equals("Any") && !filterGender.equals(workout.get("gender"))) { // gender doesn't match
            return false;
        }

        if (filterAgeOn) { // age filter is on
            if (workout.get("AgeFilter") != null) {
                int minimumAge = ((Long) workout.get("AgeFilter")).intValue(); // Firestore returns numbers as Long
                if(filterAgeRange < minimumAge) { // user's age range is less than the workout's minimum age
                    return false;
                }
            } else {
                // If the workout does not have a minimumAge field, we consider it not matching the filter
                return false;
            }
        }

        return true;
    }

    private void createWorkoutButton(LinearLayout container, Map<String, Object> workout) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View workoutButton = inflater.inflate(R.layout.workout_button, container, false);

        // Populate workout button with data
        TextView workoutDate = workoutButton.findViewById(R.id.workoutDate);
        TextView workoutCreator = workoutButton.findViewById(R.id.workoutcreator);
        TextView workoutTime = workoutButton.findViewById(R.id.workoutTime);
        TextView workoutDuration = workoutButton.findViewById(R.id.workoutDuration);
        TextView participantCount = workoutButton.findViewById(R.id.participantCount);
        TextView city = workoutButton.findViewById(R.id.cityName);
        ImageView workoutImage = workoutButton.findViewById(R.id.workoutImage);

        city.setText((String) workout.get("City"));
        workoutCreator.setText("Created by: " + (String) workout.get("FullName"));
        workoutDate.setText("Scheduled on: " + (String) workout.get("Date"));
        workoutTime.setText("At: " + (String) workout.get("Time"));
        workoutDuration.setText(workout.get("Duration") + " mins");
        participantCount.setText(workout.get("ParticipantsAmount") + " participants");
        String workoutType = (String) workout.get("Type");

        if (Objects.equals(workoutType, "power")) {
            workoutImage.setImageResource(R.drawable.lifting);
        } else if (Objects.equals(workoutType, "ride")) {
            workoutImage.setImageResource(R.drawable.ride);
        } else if (Objects.equals(workoutType, "run")) {
            workoutImage.setImageResource(R.drawable.running);
        } else if (Objects.equals(workoutType, "basketball")) {
            workoutImage.setImageResource(R.drawable.player);
        }

        // Set onClick listener to show popup
        workoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWorkoutDetailsPopup(workout);
            }
        });

        container.addView(workoutButton);
    }

    private void showWorkoutDetailsPopup(Map<String, Object> workout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.workout_details_popup, null);
        builder.setView(dialogView);

        // Populate the popup with workout details

        TextView workoutDate = dialogView.findViewById(R.id.workoutDate);
        TextView cityPopup = dialogView.findViewById(R.id.cityName);
        TextView workoutCreator = dialogView.findViewById(R.id.workoutcreator);
        TextView workoutTime = dialogView.findViewById(R.id.workoutTime);
        TextView workoutDuration = dialogView.findViewById(R.id.workoutDuration);
        TextView participantCount = dialogView.findViewById(R.id.participantCount);
        ImageView workoutImage = dialogView.findViewById(R.id.workoutImage);
        Button joinButton = dialogView.findViewById(R.id.joinButton);
        LinearLayout participantsList = dialogView.findViewById(R.id.participantsList);

        // Set workout details
        cityPopup.setText((String) workout.get("City"));
        workoutCreator.setText("Created by: " + (String) workout.get("FullName"));
        workoutDate.setText("Scheduled on: " + (String) workout.get("Date"));
        workoutTime.setText("At: " + (String) workout.get("Time"));
        workoutDuration.setText(workout.get("Duration") + " mins");
        List<String> participants = (List<String>) workout.get("Participants");
        participantCount.setText(participants.size() + " participants");
        Map<String, Object> location = (Map<String, Object>) workout.get("Location");

        if (location != null) {
            // Retrieve values from the "Location" map
             latitude = (double) location.get("latitude");
             longitude = (double) location.get("longitude");}

        String workoutType = (String) workout.get("Type");
        if (Objects.equals(workoutType, "power")) {
            workoutImage.setImageResource(R.drawable.lifting);
        } else if (Objects.equals(workoutType, "ride")) {
            workoutImage.setImageResource(R.drawable.ride);
        } else if (Objects.equals(workoutType, "run")) {
            workoutImage.setImageResource(R.drawable.running);
        } else if (Objects.equals(workoutType, "basketball")) {
            workoutImage.setImageResource(R.drawable.player);
        }

        String myuserId = currentUser.getUid();
        getUsername(myuserId, new UsernameCallback() {
                    @Override
                    public void onCallback(String fullName) {
                        myUsername = fullName;
                        int istart1 = myUsername.indexOf("(");
                        int iend1 = myUsername.indexOf(")");
                        myUsername = myUsername.substring(istart1 + 1, iend1);
                        Log.d("myUsername", myUsername);
                    }
                });



        // Add participants to the list
        for (String participant : participants) {
            TextView participantView = new TextView(this);
            participantView.setText(participant);
            participantView.setPadding(10, 10, 10, 10);
            participantView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSendFriendRequestPopup(participant);
                }
            });
            participantsList.addView(participantView);
        }

        // Map view setup
        if (mapFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(mapFragment).commit();
            mapFragment = null;
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.mapView, mapFragment).commit();
        }

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                if (mMap != null && location != null) {
                    LatLng workoutLocation = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(workoutLocation));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(workoutLocation, 13));
                }
            }
        });

        // Set onClick listener for join button
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinWorkout(workout);
            }
        });

        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (mapFragment != null) {
                    getSupportFragmentManager().beginTransaction().remove(mapFragment).commit();
                    mapFragment = null;
                }
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void joinWorkout(Map<String, Object> workout) {
        String workoutId = (String) workout.get("workoutID");
        String userId = currentUser.getUid();
        List<String> participants = (List<String>) workout.get("Participants");
        String creatorId = (String) workout.get("creatorId");


        if (userId.equals(creatorId)) {
            Toast.makeText(this, "You cannot join your own workout.", Toast.LENGTH_SHORT).show();
            return;
        }

        getUsername(userId, new UsernameCallback() {
            @Override
            public void onCallback(String fullName) {
                if (participants.contains(fullName)) {
                    Toast.makeText(HomePage.this, "You have already joined this workout.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("HomePage", "FullName: " + fullName);

                fStore.collection("workouts").document(workoutId)
                        .update("Participants", FieldValue.arrayUnion(fullName), "ParticipantsAmount", FieldValue.increment(1))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(HomePage.this, "You have joined the workout.", Toast.LENGTH_SHORT).show();
                                    loadWorkouts();
                                    Intent intent = new Intent(HomePage.this, HomePage.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(HomePage.this, "Failed to join the workout.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }



    private void showSendFriendRequestPopup(String participant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Send Friend Request");
        builder.setMessage("Send a friend request to " + participant + "?");

        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String friendUsername = participant;
                int istart2 = friendUsername.indexOf("(");
                int iend2= friendUsername.indexOf(")");
                friendUsername = friendUsername.substring(istart2 + 1, iend2); // get only the username for sending friend request
                Log.d("HomePage", "friendUsername: " + friendUsername);

                sendFriendRequest(friendUsername);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendFriendRequest(String friendUsername) {
        Log.d("HomePage", "friendUsername: " + friendUsername);
        Log.d("HomePage", "myUsername: " + myUsername);
        if (friendUsername.equals(myUsername)) {
            Toast.makeText(HomePage.this, "You cannot send a friend request to yourself.", Toast.LENGTH_SHORT).show();
            return;
        }

        String myId = currentUser.getUid();

        CollectionReference usersCollectionRef = fStore.collection("users");

        usersCollectionRef.whereEqualTo("username", friendUsername).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Check if the user exists
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot friendSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String friendId = friendSnapshot.getId();
                        DocumentReference friendDocRef = usersCollectionRef.document(friendId);

                        // Check if the friend request already exists or they are already friends
                        friendDocRef.get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                List<String> friendRequests = (List<String>) documentSnapshot.get("friendRequests");
                                List<String> friendsList = (List<String>) documentSnapshot.get("friendsList");

                                if (friendRequests != null && friendRequests.contains(myId)) {
                                    Toast.makeText(HomePage.this, "Friend request already sent.", Toast.LENGTH_SHORT).show();
                                } else if (friendsList != null && friendsList.contains(myId)) {
                                    Toast.makeText(HomePage.this, "You are already friends.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Send the friend request
                                    friendDocRef.update("friendRequests", FieldValue.arrayUnion(myId))
                                            .addOnSuccessListener(aVoid -> Toast.makeText(HomePage.this, "Friend request sent to " + friendUsername, Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e -> Toast.makeText(HomePage.this, "Failed to send friend request", Toast.LENGTH_SHORT).show());
                                }
                            }
                        }).addOnFailureListener(e -> Toast.makeText(HomePage.this, "Error checking friend requests", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(HomePage.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(HomePage.this, "Error searching for user", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onClick(View v) {
        if (v == addWorkoutButton) {
            Intent intent = new Intent(HomePage.this, addWorkout.class);
            startActivity(intent);
        } else if (v == personalAreaButton) {
            Intent intent = new Intent(HomePage.this, PersonalArea.class);
            startActivity(intent);
        } else if (v == ScoreBoardButton) {
            Intent intent = new Intent(HomePage.this, ScoreBoard.class);
            startActivity(intent);
        } else if (v == homeButton) {
            Intent intent = new Intent(HomePage.this, HomePage.class);
            startActivity(intent);
            finish();
        } else if (v == logoutButton) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        } else if (v == verifyNowButton) {
            currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(HomePage.this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HomePage.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else if (v == filterButton) {
            showFilterDialog();
        }
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filters, null);
        builder.setView(dialogView);

        Switch ageFilterSwitch = dialogView.findViewById(R.id.ageFilterSwitch);
        SeekBar ageRangeSeekBar = dialogView.findViewById(R.id.ageRangeSeekBar);
        TextView ageRangeTextView = dialogView.findViewById(R.id.ageRangeTextView);
        Spinner genderSpinner = dialogView.findViewById(R.id.genderSpinner);

        // Set up gender spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        // Load current filter settings
        ageFilterSwitch.setChecked(filterAgeOn);
        ageRangeSeekBar.setProgress(filterAgeRange);
        ageRangeTextView.setText("Age range: " + filterAgeRange + "+");
        genderSpinner.setSelection(adapter.getPosition(filterGender));

        // Set the initial enabled state of the SeekBar based on the Switch
        ageRangeSeekBar.setEnabled(filterAgeOn);

        // Listen for changes to the Switch to enable/disable the SeekBar
        ageFilterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ageRangeSeekBar.setEnabled(isChecked);
        });

        ageRangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ageRangeTextView.setText("Age range: " + progress + "+");
                filterAgeRange = progress; // Correctly set filterAgeRange here
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Save filter settings and apply them
                filterGender = genderSpinner.getSelectedItem().toString();
                filterAgeOn = ageFilterSwitch.isChecked();
                // filterAgeRange is already set correctly by the SeekBar listener

                loadWorkouts();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public interface UsernameCallback {
        void onCallback(String username);
    }

    public void getUsername(String id, final UsernameCallback callback) {
        DocumentReference documentReference = fStore.collection("users").document(id);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    String firstName = documentSnapshot.getString("firstName");
                    String lastName = documentSnapshot.getString("lastName");
                    String fullName = firstName + " " + lastName + " (" + username + ")";
                    participated = documentSnapshot.getLong("WorkoutParticipated");
                    callback.onCallback(fullName);
                } else {
                    callback.onCallback(""); // Handle the case where the document does not exist or an error occurred
                }
            }
        });
    }

}
