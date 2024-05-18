package co.il.trainwithme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomePage extends AppCompatActivity implements View.OnClickListener {
    ImageButton addWorkoutButton;
    ImageButton personalAreaButton;
    ImageButton ScoreBoardButton;
    ImageButton homeButton;
    TextView emailNotVerifiedText;
    Button verifyNowButton;
    Button filterButton;

    private FirebaseAuth fAuth;
    private FirebaseUser currentUser;

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

        addWorkoutButton.setOnClickListener(this);
        personalAreaButton.setOnClickListener(this);
        ScoreBoardButton.setOnClickListener(this);
        homeButton.setOnClickListener(this);
        verifyNowButton.setOnClickListener(this);
        filterButton.setOnClickListener(this);

        fAuth = FirebaseAuth.getInstance();
        currentUser = fAuth.getCurrentUser();

        // Check if email is verified
        if (currentUser != null && !currentUser.isEmailVerified()) {
            emailNotVerifiedText.setVisibility(View.VISIBLE);
            verifyNowButton.setVisibility(View.VISIBLE);
        } else {
            emailNotVerifiedText.setVisibility(View.GONE);
            verifyNowButton.setVisibility(View.GONE);
        }
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
            //Intent intent = new Intent(HomePage.this, HomePage.class);
            //startActivity(intent);
        } else if (v == verifyNowButton) {
            sendVerificationEmail();
        } else if (v == filterButton) {
            showFilterDialog();
        }
    }

    private void sendVerificationEmail() {
        if (currentUser != null) {
            currentUser.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(HomePage.this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(HomePage.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filters, null);
        builder.setView(dialogView);

        // Initialize the filters
        Switch privateWorkoutSwitch = dialogView.findViewById(R.id.privateWorkoutSwitch);
        Spinner genderSpinner = dialogView.findViewById(R.id.genderSpinner);
        Switch ageFilterSwitch = dialogView.findViewById(R.id.ageFilterSwitch);
        SeekBar ageRangeSeekBar = dialogView.findViewById(R.id.ageRangeSeekBar);
        TextView ageRangeTextView = dialogView.findViewById(R.id.ageRangeTextView);

        // Populate gender spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        // Initialize age range
        ageRangeSeekBar.setEnabled(false);
        ageFilterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ageRangeSeekBar.setEnabled(isChecked);
            }
        });
        ageRangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ageRangeTextView.setText("Age range: " + progress + "+");
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
                boolean isPrivate = privateWorkoutSwitch.isChecked();
                String gender = genderSpinner.getSelectedItem().toString();
                boolean ageFilterOn = ageFilterSwitch.isChecked();
                int ageRange = ageRangeSeekBar.getProgress();

                applyFilters(isPrivate, gender, ageFilterOn, ageRange);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void applyFilters(boolean isPrivate, String gender, boolean ageFilterOn, int ageRange) {
        // Logic to filter the workout list based on the selected filters
        // Update the workout list in the ScrollView accordingly
        Toast.makeText(this, "Filters applied: Private=" + isPrivate + ", Gender=" + gender + ", Age=" + (ageFilterOn ? ageRange + "+" : "Any"), Toast.LENGTH_SHORT).show();
        // Example logic (replace with actual filtering logic):
        // For demonstration, we are just showing a toast message
    }
}
