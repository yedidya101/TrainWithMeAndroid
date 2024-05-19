package co.il.trainwithme;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PersonalArea extends AppCompatActivity implements View.OnClickListener {
    private Calendar selectedDate;
    private int year, month, day, age;
    private TextView firstName_personal, lastName_personal, email_personal, age_personal, gender_personal;
    private TextView workoutsCreated, workoutsJoined, chosenDate;
    private String firstName, lastName, email, userId, gender, birthdate;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fstore;
    private Button changeInfoButton, sendFriendRequestButton, pendingRequestsButton;
    private ImageButton HomePageButton, ScoreBoardButton, addWorkoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_area);

        firstName_personal = findViewById(R.id.firstName_personal);
        lastName_personal = findViewById(R.id.lastName_personal);
        email_personal = findViewById(R.id.email_personal);
        age_personal = findViewById(R.id.age_personal);
        gender_personal = findViewById(R.id.gender_personal);

        workoutsCreated = findViewById(R.id.workoutsCreated); // need to add from database
        workoutsJoined = findViewById(R.id.workoutsParticipated); // need to add from database

        sendFriendRequestButton = findViewById(R.id.sendFriendRequestButton);
        pendingRequestsButton = findViewById(R.id.pendingRequestsButton);
        changeInfoButton = findViewById(R.id.changeInfoButton);

        changeInfoButton.setOnClickListener(this);
        pendingRequestsButton.setOnClickListener(this);
        sendFriendRequestButton.setOnClickListener(this);

        HomePageButton = findViewById(R.id.homepage2);
        ScoreBoardButton = findViewById(R.id.scoreboard2);
        addWorkoutButton = findViewById(R.id.createWorkout2);

        addWorkoutButton.setOnClickListener(this);
        HomePageButton.setOnClickListener(this);
        ScoreBoardButton.setOnClickListener(this);

        // Retrieve user data
        // Initialize FirebaseAuth and FirebaseFirestore
        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        // Ensure fAuth is not null and there's a current user
        if (fAuth.getCurrentUser() != null) {
            userId = fAuth.getCurrentUser().getUid();
            DocumentReference documentReference = fstore.collection("users").document(userId);
            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        firstName = documentSnapshot.getString("firstName");
                        lastName = documentSnapshot.getString("lastName");
                        email = documentSnapshot.getString("email");
                        birthdate = documentSnapshot.getString("birthdate");

                        String[] dateArray = birthdate.split("/");
                        year = Integer.parseInt(dateArray[2]);
                        month = Integer.parseInt(dateArray[1]) - 1; // month is 0-based in Calendar
                        day = Integer.parseInt(dateArray[0]);

                        age = calculateAge(year, month, day);

                        gender = documentSnapshot.getString("gender");
                        firstName_personal.setText(firstName);
                        lastName_personal.setText(lastName);
                        email_personal.setText(email);
                        age_personal.setText(String.valueOf(age));
                        gender_personal.setText(gender);
                    }
                }
            });
        } else {
            Intent loginintent = new Intent(PersonalArea.this, MainActivity.class);
            startActivity(loginintent);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == changeInfoButton) {
            showChangeInfoPopup();
        } else if (v == sendFriendRequestButton) {
            showSendFriendRequestPopup();
        } else if (v == pendingRequestsButton) {
            showPendingRequestsPopup();
        } else if (v == HomePageButton) {
            Intent intent = new Intent(PersonalArea.this, HomePage.class);
            startActivity(intent);
        } else if (v == ScoreBoardButton) {
            Intent intent = new Intent(PersonalArea.this, ScoreBoard.class);
            startActivity(intent);
        } else if (v == addWorkoutButton) {
            Intent intent = new Intent(PersonalArea.this, addWorkout.class);
            startActivity(intent);
        }
    }

    private void showChangeInfoPopup() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View changeInfoView = inflater.inflate(R.layout.popup_change_info, null);

        final EditText editFirstName = changeInfoView.findViewById(R.id.editFirstName);
        final EditText editLastName = changeInfoView.findViewById(R.id.editLastName);
        //final EditText editEmail = changeInfoView.findViewById(R.id.editEmail);
        final EditText editGender = changeInfoView.findViewById(R.id.editGender);
        final Button setBirthdatebtn = changeInfoView.findViewById(R.id.setBirthdatebtn);

        editFirstName.setText(firstName);
        editLastName.setText(lastName);
        //editEmail.setText(email);
        editGender.setText(gender);
        setBirthdatebtn.setText(birthdate);

        setBirthdatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(setBirthdatebtn);
            }
        });

        new AlertDialog.Builder(this)
                .setView(changeInfoView)
                .setTitle("Change Information")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newFirstName = editFirstName.getText().toString();
                        String newLastName = editLastName.getText().toString();
                        //String newEmail = editEmail.getText().toString();
                        String newGender = editGender.getText().toString();
                        String newBirthdate = setBirthdatebtn.getText().toString();

                        // Update the user's information
                        firstName_personal.setText(newFirstName);
                        lastName_personal.setText(newLastName);
                        //email_personal.setText(newEmail);
                        gender_personal.setText(newGender);
                        age_personal.setText(String.valueOf(calculateAge(year, month, day)));

                        // Update the user's information in Firestore
                        DocumentReference documentReference = fstore.collection("users").document(userId);
                        Map<String, Object> user = new HashMap<>();
                        user.put("firstName", newFirstName);
                        user.put("lastName", newLastName);
                        //yuser.put("email", newEmail);
                        user.put("birthdate", newBirthdate);
                        user.put("gender", newGender);
                        documentReference.update(user);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showSendFriendRequestPopup() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View sendFriendRequestView = inflater.inflate(R.layout.popup_send_friend_request, null);

        final EditText editFriendName = sendFriendRequestView.findViewById(R.id.editFriendName);

        new AlertDialog.Builder(this)
                .setView(sendFriendRequestView)
                .setTitle("Send Friend Request")
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String friendName = editFriendName.getText().toString();
                        // Send the friend request
                        // ...
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showPendingRequestsPopup() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View pendingRequestsView = inflater.inflate(R.layout.popup_pending_requests, null);

        final LinearLayout pendingRequestsList = pendingRequestsView.findViewById(R.id.pendingRequestsList);

        // Populate the pending requests list dynamically
        // ...

        new AlertDialog.Builder(this)
                .setView(pendingRequestsView)
                .setTitle("Pending Requests")
                .setNegativeButton("Close", null)
                .create()
                .show();
    }

    private int calculateAge(int year, int month, int day) {
        LocalDate birthDate = LocalDate.of(year, month + 1, day); // month is 0-based
        LocalDate currentDate = LocalDate.now();
        return Period.between(birthDate, currentDate).getYears();
    }

    private void showDatePickerDialog(final Button setBirthdatebtn) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int chosenYear, int chosenMonth, int dayOfMonth) {
                        selectedDate = Calendar.getInstance();
                        selectedDate.set(chosenYear, chosenMonth, dayOfMonth);
                        birthdate = dayOfMonth + "/" + (chosenMonth + 1) + "/" + chosenYear;
                        setBirthdatebtn.setText(birthdate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }
}
