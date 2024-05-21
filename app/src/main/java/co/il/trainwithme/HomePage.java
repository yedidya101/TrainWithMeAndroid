package co.il.trainwithme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

    private boolean filterPrivate;
    private String filterGender;
    private boolean filterAgeOn;
    private int filterAgeRange;

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

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        currentUser = fAuth.getCurrentUser();

        // Initialize filters
        filterPrivate = false;
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

    private void loadWorkouts() {
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

    private boolean applyFiltersToWorkout(Map<String, Object> workout) {
        // Apply the filters to each workout and return true if the workout matches the filters

        if (filterPrivate && !(boolean) workout.get("privateWorkout")) {
            return false;
        }

        if (!filterGender.equals("Any") && !filterGender.equals(workout.get("gender"))) { // gender doesn't match
            return false;
        }

        if (filterAgeOn) { // age filter is on
            if (workout.get("minimumAge") != null) {
                int minimumAge = ((Long) workout.get("minimumAge")).intValue(); // Firestore returns numbers as Long
                if (filterAgeRange < minimumAge) { // user's age range is less than the workout's minimum age
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
        ImageView workoutImage = workoutButton.findViewById(R.id.workoutImage);

        workoutCreator.setText("Created by: " + (String) workout.get("creatorName"));
        workoutDate.setText("Scheduled on: " + (String) workout.get("date"));
        workoutTime.setText("At: " + (String) workout.get("time"));
        workoutDuration.setText(workout.get("duration") + " mins");
        participantCount.setText(workout.get("Participants") + " participants");
        String workoutType = (String) workout.get("workoutType");

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
        TextView workoutCreator = dialogView.findViewById(R.id.workoutcreator);
        TextView workoutTime = dialogView.findViewById(R.id.workoutTime);
        TextView workoutDuration = dialogView.findViewById(R.id.workoutDuration);
        TextView participantCount = dialogView.findViewById(R.id.participantCount);
        ImageView workoutImage = dialogView.findViewById(R.id.workoutImage);
        Button joinButton = dialogView.findViewById(R.id.joinButton);

        workoutCreator.setText("Created by: " + (String) workout.get("creatorName"));
        workoutDate.setText("Scheduled on: " + (String) workout.get("date"));
        workoutTime.setText("At: " + (String) workout.get("time"));
        workoutDuration.setText(workout.get("duration") + " mins");
        participantCount.setText(workout.get("Participants") + " participants");
        String workoutType = (String) workout.get("workoutType");

        if (Objects.equals(workoutType, "power")) {
            workoutImage.setImageResource(R.drawable.lifting);
        } else if (Objects.equals(workoutType, "ride")) {
            workoutImage.setImageResource(R.drawable.ride);
        } else if (Objects.equals(workoutType, "run")) {
            workoutImage.setImageResource(R.drawable.running);
        } else if (Objects.equals(workoutType, "basketball")) {
            workoutImage.setImageResource(R.drawable.player);
        }

        // Set onClick listener for join button
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Placeholder for join functionality
                Toast.makeText(HomePage.this, "Join button clicked", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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

        Switch privateWorkoutSwitch = dialogView.findViewById(R.id.privateWorkoutSwitch);
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
        privateWorkoutSwitch.setChecked(filterPrivate);
        ageFilterSwitch.setChecked(filterAgeOn);
        ageRangeSeekBar.setProgress(filterAgeRange);
        ageRangeTextView.setText("Age range: " + filterAgeRange + "+");
        genderSpinner.setSelection(adapter.getPosition(filterGender));

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
                filterPrivate = privateWorkoutSwitch.isChecked();
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
}
