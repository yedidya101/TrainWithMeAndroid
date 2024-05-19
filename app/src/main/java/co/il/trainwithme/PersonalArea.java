package co.il.trainwithme;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalArea extends AppCompatActivity implements View.OnClickListener {
    private Calendar selectedDate;
    private int year, month, day, age, workoutcreated, workoutjoined;
    private TextView firstName_personal, lastName_personal, email_personal, age_personal, gender_personal, username;
    private TextView workoutsCreated, workoutsJoined, chosenDate;
    private String firstName, lastName, email, userId, gender, birthdate, pUsername;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fstore;
    private Button changeInfoButton, sendFriendRequestButton, pendingRequestsButton;
    private ImageButton HomePageButton, ScoreBoardButton, addWorkoutButton;
    private LinearLayout friendsListContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_area);

        firstName_personal = findViewById(R.id.firstName_personal);
        lastName_personal = findViewById(R.id.lastName_personal);
        email_personal = findViewById(R.id.email_personal);
        age_personal = findViewById(R.id.age_personal);
        gender_personal = findViewById(R.id.gender_personal);
        username = findViewById(R.id.username);

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
                        workoutcreated = documentSnapshot.getLong("workoutCreated").intValue();
                        workoutjoined = documentSnapshot.getLong("workoutJoined").intValue();
                        pUsername = documentSnapshot.getString("username");

                        String[] dateArray = birthdate.split("/");
                        year = Integer.parseInt(dateArray[2]);
                        month = Integer.parseInt(dateArray[1]) - 1; // month is 0-based in Calendar
                        day = Integer.parseInt(dateArray[0]);

                        age = calculateAge(year, month, day);

                        gender = documentSnapshot.getString("gender");
                        firstName_personal.setText(firstName);
                        lastName_personal.setText(lastName);
                        email_personal.setText(email);
                        username.setText(pUsername);
                        age_personal.setText(String.valueOf(age));
                        gender_personal.setText(gender);
                        workoutsCreated.setText(String.valueOf(workoutcreated));
                        workoutsJoined.setText(String.valueOf(workoutjoined));
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
            finish();
        } else if (v == ScoreBoardButton) {
            Intent intent = new Intent(PersonalArea.this, ScoreBoard.class);
            finish();
            startActivity(intent);
        } else if (v == addWorkoutButton) {
            Intent intent = new Intent(PersonalArea.this, addWorkout.class);
            startActivity(intent);
            finish();
        }
    }

    private void showChangeInfoPopup() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View changeInfoView = inflater.inflate(R.layout.popup_change_info, null);

        final Spinner editGender = changeInfoView.findViewById(R.id.editGender);
        final EditText editFirstName = changeInfoView.findViewById(R.id.editFirstName);
        final EditText editLastName = changeInfoView.findViewById(R.id.editLastName);
        final Button setBirthdatebtn = changeInfoView.findViewById(R.id.setBirthdatebtn);

        editFirstName.setText(firstName);
        editLastName.setText(lastName);

        // Set up the Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editGender.setAdapter(adapter);

        // Set the current gender
        if (gender != null) {
            int spinnerPosition = adapter.getPosition(gender);
            editGender.setSelection(spinnerPosition);
        }

        setBirthdatebtn.setText(birthdate);
        setBirthdatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(setBirthdatebtn);
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(changeInfoView)
                .setTitle("Change Information")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (editGender.getSelectedItem().toString().equals("Any")) {
                            Toast.makeText(PersonalArea.this, "Please choose your correct gender.", Toast.LENGTH_SHORT).show();
                        } else {
                            String newFirstName = editFirstName.getText().toString();
                            String newLastName = editLastName.getText().toString();
                            String newGender = editGender.getSelectedItem().toString();
                            String newBirthdate = setBirthdatebtn.getText().toString();

                            // Update the user's information
                            firstName_personal.setText(newFirstName);
                            lastName_personal.setText(newLastName);
                            gender_personal.setText(newGender);
                            age_personal.setText(String.valueOf(calculateAge(year, month, day)));

                            // Update the user's information in Firestore
                            DocumentReference documentReference = fstore.collection("users").document(userId);
                            Map<String, Object> user = new HashMap<>();
                            user.put("firstName", newFirstName);
                            user.put("lastName", newLastName);
                            user.put("birthdate", newBirthdate);
                            user.put("gender", newGender);
                            documentReference.update(user);

                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        dialog.show();
    }

    private void showSendFriendRequestPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_send_friend_request, null);
        builder.setView(dialogView);

        EditText editFriendName = dialogView.findViewById(R.id.editFriendName);

        builder.setTitle("Send Friend Request")
                .setPositiveButton("Send", null)
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button sendButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            sendButton.setOnClickListener(view -> {
                String friendName = editFriendName.getText().toString().trim();
                if (!friendName.isEmpty()) {
                    sendFriendRequest(friendName, dialog);
                } else {
                    Toast.makeText(PersonalArea.this, "Please enter a username.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void sendFriendRequest(String friendUsername, AlertDialog dialog) {
        CollectionReference usersRef = fstore.collection("users");
        usersRef.whereEqualTo("username", friendUsername).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String friendId = document.getId();
                        String friendName = document.getString("firstName") + " " + document.getString("lastName");

                        // Check if a friend request already exists
                        DocumentReference friendRequestRef = fstore.collection("friend_requests").document(friendId).collection("requests").document(userId);
                        friendRequestRef.get().addOnCompleteListener(friendRequestTask -> {
                            if (friendRequestTask.isSuccessful() && !friendRequestTask.getResult().exists()) {
                                // Create a new friend request
                                Map<String, Object> request = new HashMap<>();
                                request.put("from", userId);
                                request.put("fromUsername", pUsername);
                                request.put("status", "pending");
                                request.put("timestamp", FieldValue.serverTimestamp());

                                friendRequestRef.set(request).addOnCompleteListener(sendRequestTask -> {
                                    if (sendRequestTask.isSuccessful()) {
                                        Toast.makeText(PersonalArea.this, "Friend request sent to " + friendName, Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(PersonalArea.this, "Failed to send friend request. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(PersonalArea.this, "Friend request already sent to " + friendName, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(PersonalArea.this, "User not found.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPendingRequestsPopup() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View pendingRequestsView = inflater.inflate(R.layout.popup_pending_requests, null);

        final LinearLayout pendingRequestsList = pendingRequestsView.findViewById(R.id.pendingRequestsList);

        // Retrieve pending requests
        fstore.collection("friend_requests").document(userId).collection("requests")
                .whereEqualTo("status", "pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> requests = task.getResult().getDocuments();
                        for (DocumentSnapshot request : requests) {
                            String fromUsername = request.getString("fromUsername");
                            String requestId = request.getId();
                            View requestItemView = inflater.inflate(R.layout.pending_request_item, null);
                            TextView requestTextView = requestItemView.findViewById(R.id.requestTextView);
                            Button acceptButton = requestItemView.findViewById(R.id.acceptButton);
                            Button rejectButton = requestItemView.findViewById(R.id.rejectButton);

                            requestTextView.setText("Pending Request: " + fromUsername);

                            acceptButton.setOnClickListener(v -> handleFriendRequest(requestId, true));
                            rejectButton.setOnClickListener(v -> handleFriendRequest(requestId, false));

                            pendingRequestsList.addView(requestItemView);
                        }
                    } else {
                        Toast.makeText(PersonalArea.this, "Failed to load pending requests.", Toast.LENGTH_SHORT).show();
                    }
                });

        new AlertDialog.Builder(this)
                .setView(pendingRequestsView)
                .setTitle("Pending Requests")
                .setNegativeButton("Close", null)
                .create()
                .show();
    }

    private void handleFriendRequest(String requestId, boolean isAccepted) {
        DocumentReference requestRef = fstore.collection("friend_requests").document(userId).collection("requests").document(requestId);
        requestRef.update("status", isAccepted ? "accepted" : "rejected")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (isAccepted) {
                            // Add each other as friends
                            DocumentReference friendRef = fstore.collection("friends").document(userId).collection("userFriends").document(requestId);
                            friendRef.set(new HashMap<>());

                            DocumentReference myRef = fstore.collection("friends").document(requestId).collection("userFriends").document(userId);
                            myRef.set(new HashMap<>());
                        }
                        Toast.makeText(PersonalArea.this, "Request " + (isAccepted ? "accepted" : "rejected"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PersonalArea.this, "Failed to update request status. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
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

    private void loadFriends() {
        fstore.collection("friends").document(userId).collection("userFriends").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> friends = task.getResult().getDocuments();
                        for (DocumentSnapshot friendDoc : friends) {
                            String friendId = friendDoc.getId();
                            fstore.collection("users").document(friendId).get()
                                    .addOnCompleteListener(friendTask -> {
                                        if (friendTask.isSuccessful() && friendTask.getResult() != null) {
                                            DocumentSnapshot friendData = friendTask.getResult();
                                            String friendName = friendData.getString("firstName") + " " + friendData.getString("lastName");
                                            String gender = friendData.getString("gender");

                                            View friendItemView = getLayoutInflater().inflate(R.layout.friend_item, null);
                                            ImageView friendImageView = friendItemView.findViewById(R.id.friendImage);
                                            TextView friendNameTextView = friendItemView.findViewById(R.id.friendName);

                                            friendNameTextView.setText(friendName);
                                            if ("Male".equalsIgnoreCase(gender)) {
                                                friendImageView.setImageResource(R.drawable.person_image); // Replace with your male image resource
                                            } else if ("Female".equalsIgnoreCase(gender)) {
                                                friendImageView.setImageResource(R.drawable.women); // Replace with your female image resource
                                            }

                                            friendsListContainer.addView(friendItemView);
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(PersonalArea.this, "Failed to load friends.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

