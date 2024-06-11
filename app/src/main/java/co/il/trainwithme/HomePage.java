package co.il.trainwithme;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class HomePage extends AppCompatActivity implements View.OnClickListener {
    ImageButton addWorkoutButton;
    ImageButton personalAreaButton;
    ImageButton ScoreBoardButton;
    ImageButton homeButton, logoutButton;
    TextView emailNotVerifiedText, dailyStreak;
    Button verifyNowButton;
    Button filterButton;

    private FirebaseAuth fAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore fStore;
    private String filterGender, myUsername, userGender, birthdate;
    private boolean filterAgeOn;
    private int filterAgeRange, userAge;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private double latitude, longitude;
    private HashMap <Double, String> location;
    private Long participated;
    private Boolean isFriend = false;
    private long loginStreak = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private double userLatitude, userLongitude;
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
        dailyStreak = findViewById(R.id.DailyStreak);

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

        //getUserLocation();
        checkAndUpdateLoginStreak();

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
        fetchUserGenderAndBirthdate(new GenderFetchCallback() { // only after user gender and birthdate is fetched
            // load workouts because need to show only the available workouts
            @Override
            public void onGenderFetched() {
                loadWorkouts();
            }
        });
    }

    private void loadWorkouts() { // Load workouts from Firestore
        deleteOldWorkouts();
        fStore.collection("workouts").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            LinearLayout workoutListContainer = findViewById(R.id.workout_list_container);
                            workoutListContainer.removeAllViews();

                            List<Map<String, Object>> workouts = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> workout = document.getData();

                                // Check if the workout is qualified
                                IsWorkoutQualified(workout, userGender, new IsWorkoutQualifiedCallback() {
                                    @Override
                                    public void onResult(boolean isQualified) {
                                        if (isQualified) { //the user is qualified by the creator filters
                                            if (applyFiltersToWorkout(workout)) { // check if the user put filter to show the workouts he want
                                                workouts.add(workout);

                                            }
                                        }
                                    }
                                });
                            }
                            sortWorkoutsByDistance(workouts);
                            for (Map<String, Object> workout : workouts) {
                                runOnUiThread(() -> createWorkoutButton(workoutListContainer, workout));
                            }

                        } else {
                            Toast.makeText(HomePage.this, "Failed to load workouts.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private boolean applyFiltersToWorkout(Map<String, Object> workout) { // Apply filters on aviable workouts in homepage
        // Apply the filters to each workout and return true if the workout matches the filters
        if (!filterGender.equals("Any") && !filterGender.equals(workout.get("GenderFilter"))) { // gender doesn't match
            return false;
        }

        if (filterAgeOn) { // age filter is on
            if (workout.get("AgeFilter") != null) {
                int minimumAge = ((Long) workout.get("AgeFilter")).intValue(); // Firestore returns numbers as Long
                Log.d("minimumAge","minimum age is:" + minimumAge);
                if( minimumAge < filterAgeRange) { // user's age range is less than the workout's minimum age
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
        isFriend = false; //reset the Friend checking for the next workout if its private

        // Populate workout button with data
        TextView workoutDate = workoutButton.findViewById(R.id.workoutDate);
        TextView workoutCreator = workoutButton.findViewById(R.id.workoutcreator);
        TextView workoutTime = workoutButton.findViewById(R.id.workoutTime);
        TextView workoutDuration = workoutButton.findViewById(R.id.workoutDuration);
        TextView participantCount = workoutButton.findViewById(R.id.participantCount);
        TextView city = workoutButton.findViewById(R.id.cityName);
        ImageView workoutImage = workoutButton.findViewById(R.id.workoutImage);
        TextView workoutfilters = workoutButton.findViewById(R.id.workoutFilters);

        String Filters = getWorkoutFilters(workout);
        if (!Filters.equals("")) {
            workoutfilters.setText(Filters);
        }
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
        TextView filtersPopup = dialogView.findViewById(R.id.workoutFiltersPopup);
        ImageView workoutImage = dialogView.findViewById(R.id.workoutImage);
        Button joinButton = dialogView.findViewById(R.id.joinButton);
        LinearLayout participantsList = dialogView.findViewById(R.id.participantsList);

        String Filters = getWorkoutFilters(workout);
        if (!Filters.equals("")) {
            filtersPopup.setText(Filters);
        }

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

        // Use getDocument directly without listener to prevent double updates
        DocumentReference userRef = fStore.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String fullName = getFullName(documentSnapshot); // Custom method to get the full name
                if (participants.contains(fullName)) {
                    Toast.makeText(HomePage.this, "You have already joined this workout.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("HomePage", "FullName: " + fullName);

                fStore.collection("workouts").document(workoutId)
                        .update("Participants", FieldValue.arrayUnion(fullName), "ParticipantsAmount", FieldValue.increment(1))
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Update the user's document to increment WorkoutParticipated field
                                userRef.update("workoutJoined", FieldValue.increment(1)) // increment directly
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Toast.makeText(HomePage.this, "You have joined the workout.", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(HomePage.this, HomePage.class);
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(HomePage.this, "Failed to update participation count.", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                // Reload the workouts to reflect the changes
                                loadWorkouts();
                            } else {
                                Toast.makeText(HomePage.this, "Failed to join the workout.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(HomePage.this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(HomePage.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
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
        String[] genderArray = new String[0];
        if ("Male".equalsIgnoreCase(userGender)) {
            genderArray = new String[]{"Any", "Male Only"};
        } else if("Female".equalsIgnoreCase(userGender)) {
            genderArray = new String[]{"Any", "Female Only"};
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        // Load current filter settings
        ageFilterSwitch.setChecked(filterAgeOn);
        ageRangeSeekBar.setProgress(filterAgeRange);
        ageRangeTextView.setText("Above age: " + filterAgeRange + "+");
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
                ageRangeTextView.setText("Above age: " + progress + "+");
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
// from here no documentation
    public void getUsername(String id, final UsernameCallback callback) {
        DocumentReference documentReference = fStore.collection("users").document(id);
        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                String fullName = getFullName(documentSnapshot); // Use helper method
                participated = documentSnapshot.getLong("workoutJoined");
                participated = (long) + 1;
                Log.d("participated:", "participated: " + participated);
                callback.onCallback(fullName);
            } else {
                callback.onCallback(""); // Handle the case where the document does not exist
            }
        }).addOnFailureListener(e -> {
            callback.onCallback(""); // Handle the error
        });
    }

    private String getFullName(DocumentSnapshot documentSnapshot) {
        String username = documentSnapshot.getString("username");
        String firstName = documentSnapshot.getString("firstName");
        String lastName = documentSnapshot.getString("lastName");
        return firstName + " " + lastName + " (" + username + ")";
    }

    private void fetchUserGenderAndBirthdate(final GenderFetchCallback callback) {
        String userId = currentUser.getUid();
        DocumentReference userRef = fStore.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                userGender = documentSnapshot.getString("gender");
                Log.d("userGender:", "userGender: " + userGender);
                birthdate = documentSnapshot.getString("birthdate");

                String[] dateArray = birthdate.split("/"); // calculate user age from his birthdate
                int year = Integer.parseInt(dateArray[2]);
                int month = Integer.parseInt(dateArray[1]) - 1;
                int day = Integer.parseInt(dateArray[0]);

                userAge = calculateAge(year, month, day);

                // Call the callback after fetching the gender and age
                callback.onGenderFetched();
            }
        }).addOnFailureListener(e -> {
            Log.e("HomePage", "Failed to fetch user gender", e);
        });
    }

    public interface GenderFetchCallback {
        void onGenderFetched();
    }



    private void IsWorkoutQualified(Map<String, Object> workout, String userGender, IsWorkoutQualifiedCallback callback) {
        int index;
        String WorkoutGender = "";
        String creatorId = (String) workout.get("CreatorID");
        boolean workoutPrivate = (boolean) workout.get("Private");
        String myId = currentUser.getUid();

        // Check gender filter
        if (workout.get("GenderFilter") != null) {
            String WorkoutGenderfullParse = (String) workout.get("GenderFilter");
            if (!Objects.equals(WorkoutGenderfullParse, "All")) {
                index = WorkoutGenderfullParse.indexOf(' ');
                WorkoutGender = WorkoutGenderfullParse.substring(0, index); // remove the word only after the gender.
            }
            if (!userGender.equals(WorkoutGender)) { // gender doesn't match
                if (!workout.get("GenderFilter").equals("All")) {
                    callback.onResult(false);
                    return;
                }
            }
        }

        // Check age filter
        if (workout.get("AgeFilter") != null) {
            int minimumAge = ((Long) workout.get("AgeFilter")).intValue(); // Firestore returns numbers as Long
            if (userAge < minimumAge) { // user's age range is less than the workout's minimum age
                callback.onResult(false);
                return;
            }
        }

        // Check if the workout is private and if the user is a friend
        if (workoutPrivate && !creatorId.equals(myId)) {
            DocumentReference userDocRef = fStore.collection("users").document(creatorId);
            userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> friendsList = (List<String>) documentSnapshot.get("friendsList");
                    boolean isFriend = false;
                    if (friendsList != null && !friendsList.isEmpty()) {
                        for (String friendId : friendsList) {
                            if (friendId.equals(myId)) {
                                isFriend = true;
                                break;
                            }
                        }
                    }
                    callback.onResult(isFriend);
                } else {
                    callback.onResult(false);
                }
            }).addOnFailureListener(e -> {
                callback.onResult(false);
            });
        } else {
            callback.onResult(true);
        }
    }

    public interface IsWorkoutQualifiedCallback {
        void onResult(boolean isQualified);
    }


    private int calculateAge(int year, int month, int day) {
        LocalDate birthDate = LocalDate.of(year, month, day);
        LocalDate currentDate = LocalDate.now();
        return Period.between(birthDate, currentDate).getYears();
    }
    private String getWorkoutFilters(Map<String, Object> workout) {
        String Filters = "";
        boolean Privateworkout = (boolean) workout.get("Private");
        int minimumage = ((Long) workout.get("AgeFilter")).intValue();
        if(Privateworkout) {
            Filters = Filters + "Private Workout";
        }
        if (workout.get("GenderFilter") != null && !workout.get("GenderFilter").equals("All")) {
            Filters = Filters + " " +"(" + workout.get("GenderFilter") + ")";
        }
        if (workout.get("AgeFilter") != null) {
            if(!(minimumage == 0)) {
                Filters = Filters + " " + "Above age:" + " " + workout.get("AgeFilter");
            }
        }
        return Filters;
    }

    private void checkAndUpdateLoginStreak() {
        String userId = currentUser.getUid();
        DocumentReference userDocRef = fStore.collection("users").document(userId);

        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDateStr = sdf.format(new Date());

            if (documentSnapshot.exists()) {
                String lastLoginDateStr = documentSnapshot.getString("lastLoginDate");

                if (lastLoginDateStr != null) {
                    try {
                        Date lastLoginDate = sdf.parse(lastLoginDateStr);
                        Date currentDate = sdf.parse(currentDateStr);

                        long diffInMillies = Math.abs(currentDate.getTime() - lastLoginDate.getTime());
                        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                        if (diffInDays == 1) {
                            // User logged in consecutively, increment the streak
                            loginStreak = documentSnapshot.getLong("loginStreak") + 1;
                        } else if (diffInDays > 1) {
                            // User missed a day, reset the streak
                            loginStreak = 1;
                        } else {
                            // User logged in again on the same day, keep the streak unchanged
                            loginStreak = documentSnapshot.getLong("loginStreak");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Update the last login date and login streak in the database
                userDocRef.update("lastLoginDate", currentDateStr, "loginStreak", loginStreak)
                        .addOnSuccessListener(aVoid -> Log.d("HomePage", "Login streak updated successfully"))
                        .addOnFailureListener(e -> Log.e("HomePage", "Failed to update login streak", e));

            } else {
                // If the document does not exist, create the fields
                Map<String, Object> data = new HashMap<>();
                data.put("lastLoginDate", currentDateStr);
                data.put("loginStreak", loginStreak);

                userDocRef.set(data)
                        .addOnSuccessListener(aVoid -> Log.d("HomePage", "Login streak initialized successfully"))
                        .addOnFailureListener(e -> Log.e("HomePage", "Failed to initialize login streak", e));
            }
            if(loginStreak == 1) {
                dailyStreak.setText("Daily Login Streak: " + loginStreak);
            }
            else
                dailyStreak.setText("Daily Login Streak: " + loginStreak + "\uD83D\uDD25 "  );
        }).addOnFailureListener(e -> Log.e("HomePage", "Failed to fetch user data", e));
    }


    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            userLatitude = (double) location.getLatitude();
                            userLongitude = (double) location.getLongitude();
                            loadWorkouts();
                        }
                    }
                });
    }

    private double calculateDistance( double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }

    private void sortWorkoutsByDistance(List<Map<String, Object>> workouts) {
        for (Map<String, Object> workout : workouts) {
            Map<String, Object> workoutLocation = (Map<String, Object>) workout.get("Location");
            double workoutLatitude = (double) workoutLocation.get("latitude");
            double workoutLongitude = (double) workoutLocation.get("longitude");
            double distance = calculateDistance(userLatitude, userLongitude, workoutLatitude, workoutLongitude);
            workout.put("distance", distance);
        }

        Collections.sort(workouts, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> w1, Map<String, Object> w2) {
                double distance1 = (double) w1.get("distance");
                double distance2 = (double) w2.get("distance");
                return Double.compare(distance1, distance2);
            }
        });
        Collections.reverse(workouts);
    }

    private void deleteOldWorkouts() {
        // Fetch all workouts from the database
        CollectionReference workoutsRef = fStore.collection("workouts");
        workoutsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date currentDate = new Date();
            String currentDateStr = sdf.format(currentDate);

            for (DocumentSnapshot document : queryDocumentSnapshots) {
                Map<String, Object> workout = document.getData();
                String workoutDateStr = (String) workout.get("Date");

                if (workoutDateStr != null) {
                    try {
                        Date workoutDate = sdf.parse(workoutDateStr);
                        long diffInMillies = currentDate.getTime() - workoutDate.getTime();
                        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                        if (diffInDays > 0) { // If the workout is older than today
                            workoutsRef.document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> Log.d("DeleteOldWorkouts", "Workout deleted successfully"))
                                    .addOnFailureListener(e -> Log.e("DeleteOldWorkouts", "Failed to delete workout", e));
                        }
                    } catch (ParseException e) {
                        Log.e("DeleteOldWorkouts", "Failed to parse workout date", e);
                    }
                }
            }
        }).addOnFailureListener(e -> Log.e("DeleteOldWorkouts", "Failed to fetch workouts", e));
    }

}
