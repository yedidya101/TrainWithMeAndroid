package co.il.trainwithme;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import java.util.Objects;

public class PersonalArea extends AppCompatActivity implements View.OnClickListener {
    private Calendar selectedDate;
    private int year, month, day, age, workoutcreated, workoutjoined;
    private TextView firstName_personal, lastName_personal, email_personal, age_personal, gender_personal, username;
    private TextView workoutsCreated, workoutsJoined, chosenDate;
    private String firstName, lastName, email, userId, gender, birthdate, pUsername;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fstore;
    private Button changeInfoButton, sendFriendRequestButton, pendingRequestsButton;
    private ImageButton HomePageButton, ScoreBoardButton, addWorkoutButton, personalAreaButton;
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
        personalAreaButton = findViewById(R.id.personal);

        workoutsCreated = findViewById(R.id.workoutsCreated);
        workoutsJoined = findViewById(R.id.workoutsParticipated);

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
        personalAreaButton.setOnClickListener(this);

        personalAreaButton.setBackgroundResource(R.drawable.personalareachosen);


        friendsListContainer = findViewById(R.id.friendsListContainer);

        // Retrieve user data
        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();



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
                        month = Integer.parseInt(dateArray[1]) - 1;
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

                        loadUserWorkouts(); // load workouts only after user data is available. in order to get username first name and last name
                    }
                }
            });

            loadFriends();

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

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editGender.setAdapter(adapter);

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

                            firstName_personal.setText(newFirstName);
                            lastName_personal.setText(newLastName);
                            gender_personal.setText(newGender);
                            age_personal.setText(String.valueOf(calculateAge(year, month, day)));

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

        final EditText usernameInput = dialogView.findViewById(R.id.editFriendName);

        builder.setTitle("Send Friend Request")
                .setPositiveButton("Send", null)
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button sendButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String friendUsername = usernameInput.getText().toString().trim();

                        if (friendUsername.isEmpty()) {
                            usernameInput.setError("Please enter a username");
                            return;
                        }

                        sendFriendRequest(friendUsername);
                        dialog.dismiss();
                    }
                });
            }
        });

        dialog.show();
    }

    private void sendFriendRequest(String friendUsername) {
        if (friendUsername.equals(pUsername)) {
            Toast.makeText(PersonalArea.this, "You cannot send a friend request to yourself.", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference usersCollectionRef = fstore.collection("users");

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

                                if (friendRequests != null && friendRequests.contains(userId)) {
                                    Toast.makeText(PersonalArea.this, "Friend request already sent.", Toast.LENGTH_SHORT).show();
                                } else if (friendsList != null && friendsList.contains(userId)) {
                                    Toast.makeText(PersonalArea.this, "You are already friends.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Send the friend request
                                    friendDocRef.update("friendRequests", FieldValue.arrayUnion(userId))
                                            .addOnSuccessListener(aVoid -> Toast.makeText(PersonalArea.this, "Friend request sent to " + friendUsername, Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e -> Toast.makeText(PersonalArea.this, "Failed to send friend request", Toast.LENGTH_SHORT).show());
                                }
                            }
                        }).addOnFailureListener(e -> Toast.makeText(PersonalArea.this, "Error checking friend requests", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(PersonalArea.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(PersonalArea.this, "Error searching for user", Toast.LENGTH_SHORT).show());
    }


    private void showPendingRequestsPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_pending_requests, null);
        builder.setView(dialogView);

        final LinearLayout pendingRequestsContainer = dialogView.findViewById(R.id.pendingRequestsContainer);

        builder.setTitle("Pending Friend Requests")
                .setPositiveButton("Close", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        DocumentReference userDocRef = fstore.collection("users").document(userId);
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> friendRequests = (List<String>) documentSnapshot.get("friendRequests");
                if (friendRequests != null && !friendRequests.isEmpty()) {
                    for (String requestId : friendRequests) {
                        fstore.collection("users").document(requestId).get().addOnSuccessListener(friendSnapshot -> {
                            if (friendSnapshot.exists()) {
                                String requestUsername = friendSnapshot.getString("username");

                                View requestView = inflater.inflate(R.layout.pending_request_item, null);
                                TextView requestUsernameTextView = requestView.findViewById(R.id.requestTextView);
                                Button acceptButton = requestView.findViewById(R.id.acceptButton);
                                Button declineButton = requestView.findViewById(R.id.rejectButton);

                                requestUsernameTextView.setText(requestUsername);

                                acceptButton.setOnClickListener(v -> {
                                    acceptFriendRequest(requestId, requestView);
                                });

                                declineButton.setOnClickListener(v -> {
                                    declineFriendRequest(requestId, requestView);
                                });

                                pendingRequestsContainer.addView(requestView);
                            }
                        });
                    }
                } else {
                    TextView noRequestsTextView = new TextView(this);
                    noRequestsTextView.setText("No pending friend requests");
                    pendingRequestsContainer.addView(noRequestsTextView);
                }
            }
        });
    }

    private void acceptFriendRequest(String requestId, View requestView) {
        DocumentReference userDocRef = fstore.collection("users").document(userId);
        DocumentReference friendDocRef = fstore.collection("users").document(requestId);

        userDocRef.update("friendsList", FieldValue.arrayUnion(requestId), "friendRequests", FieldValue.arrayRemove(requestId))
                .addOnSuccessListener(aVoid -> friendDocRef.update("friendsList", FieldValue.arrayUnion(userId))
                        .addOnSuccessListener(aVoid1 -> {
                            Toast.makeText(PersonalArea.this, "Friend request accepted", Toast.LENGTH_SHORT).show();
                            ((LinearLayout) requestView.getParent()).removeView(requestView);
                            addFriendToList(requestId);
                        })
                        .addOnFailureListener(e -> Toast.makeText(PersonalArea.this, "Failed to accept friend request", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> Toast.makeText(PersonalArea.this, "Failed to update friend list", Toast.LENGTH_SHORT).show());
    }

    private void declineFriendRequest(String requestId, View requestView) {
        DocumentReference userDocRef = fstore.collection("users").document(userId);
        userDocRef.update("friendRequests", FieldValue.arrayRemove(requestId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PersonalArea.this, "Friend request declined", Toast.LENGTH_SHORT).show();
                    ((LinearLayout) requestView.getParent()).removeView(requestView);
                })
                .addOnFailureListener(e -> Toast.makeText(PersonalArea.this, "Failed to decline friend request", Toast.LENGTH_SHORT).show());
    }

    private void addFriendToList(String friendId) {
        fstore.collection("users").document(friendId).get().addOnSuccessListener(friendSnapshot -> {
            if (friendSnapshot.exists()) {
                String friendUsername = friendSnapshot.getString("username");
                String friendGender = friendSnapshot.getString("gender");

                View friendView = getLayoutInflater().inflate(R.layout.friend_item, friendsListContainer, false);
                TextView friendUsernameTextView = friendView.findViewById(R.id.friendUsernameTextView);
                ImageView friendImageView = friendView.findViewById(R.id.friendImageView);
                Button removeFriendButton = friendView.findViewById(R.id.removeFriendButton);

                friendUsernameTextView.setText(friendUsername);
                if (friendGender != null && friendGender.equalsIgnoreCase("male")) {
                    friendImageView.setImageResource(R.drawable.person_image);
                } else {
                    friendImageView.setImageResource(R.drawable.women);
                }
                friendImageView.setOnClickListener(v -> openChat(friendUsername));

                removeFriendButton.setOnClickListener(v -> showRemoveFriendDialog(friendId, friendView, friendUsername));

                friendsListContainer.addView(friendView);
            }
        });
    }


    private void showRemoveFriendDialog(String friendId, View friendView, String friendUsername) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Friend" + " " +friendUsername)
                .setMessage(R.string.confirm_remove_message)
                .setPositiveButton("Yes", (dialog, which) -> removeFriend(friendId, friendView))
                .setNegativeButton("No", null)
                .show();
    }

    private void removeFriend(String friendId, View friendView) {
        DocumentReference userDocRef = fstore.collection("users").document(userId);
        DocumentReference friendDocRef = fstore.collection("users").document(friendId);

        userDocRef.update("friendsList", FieldValue.arrayRemove(friendId))
                .addOnSuccessListener(aVoid -> {
                    friendDocRef.update("friendsList", FieldValue.arrayRemove(userId))
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(PersonalArea.this, "Friend removed", Toast.LENGTH_SHORT).show();
                                friendsListContainer.removeView(friendView);
                            })
                            .addOnFailureListener(e -> Toast.makeText(PersonalArea.this, "Failed to remove friend from friend's list", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(PersonalArea.this, "Failed to remove friend", Toast.LENGTH_SHORT).show());
    }


    private void openChat(String friendUsername) {
        CollectionReference chatsRef = fstore.collection("chats");
        String chatId = generateChatId(pUsername, friendUsername);

        chatsRef.document(chatId).get().addOnCompleteListener(task -> {
             Intent intent = new Intent(PersonalArea.this, ChatActivity.class);
            intent.putExtra("chatId", chatId);
            intent.putExtra("friendUsername", friendUsername);
            startActivity(intent);
        });
    }

    private String generateChatId(String username1, String username2) {
        return username1.compareTo(username2) < 0 ? username1 + "_" + username2 : username2 + "_" + username1;
    }


    private void loadFriends() {
        DocumentReference userDocRef = fstore.collection("users").document(userId);
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> friendsList = (List<String>) documentSnapshot.get("friendsList");
                if (friendsList != null && !friendsList.isEmpty()) {
                    for (String friendId : friendsList) {
                        addFriendToList(friendId);
                    }
                }
            }
        });
    }

    private void showDatePickerDialog(final Button setBirthdatebtn) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        setBirthdatebtn.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                        birthdate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        calculateAge(year, monthOfYear, dayOfMonth);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private int calculateAge(int year, int month, int day) {
        LocalDate birthDate = LocalDate.of(year, month, day);
        LocalDate currentDate = LocalDate.now();
        return Period.between(birthDate, currentDate).getYears();
    }

    private String getFullName() {
        return firstName + " " + lastName + " (" + pUsername + ")";
    }

    private void loadUserWorkouts() {
        loadJoinedWorkouts();
        loadCreatedWorkouts();
    }




    private void loadJoinedWorkouts() {
        String myName = getFullName();
        CollectionReference workoutsRef = fstore.collection("workouts");
        workoutsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    LinearLayout joinedWorkoutsContainer = findViewById(R.id.joinedWorkoutsContainer);
                    joinedWorkoutsContainer.removeAllViews();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        List<String> participants = (List<String>) document.get("Participants");
                        if (participants != null && participants.contains(myName)) {
                            Log.d("document", "another joined workout:");
                            Map<String, Object> workout = document.getData();
                            createPersonalWorkoutButton(joinedWorkoutsContainer, workout, false);
                        }
                    }

                });
    }

    private void loadCreatedWorkouts() {
        String creator = fAuth.getUid();
        CollectionReference workoutsRef = fstore.collection("workouts");
        workoutsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    LinearLayout createdWorkoutsContainer = findViewById(R.id.createdWorkoutsContainer);
                    createdWorkoutsContainer.removeAllViews();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        if(document.getString("CreatorID").equals(creator)) {
                            Map<String, Object> workout = document.getData();
                            createPersonalWorkoutButton(createdWorkoutsContainer, workout, true);
                        }
                    }
                });
    }

    private void createPersonalWorkoutButton(LinearLayout container, Map<String, Object> workout, boolean isCreator) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View workoutButton = inflater.inflate(R.layout.workout_button_personal_area, container, false);

        // Populate workout button with data (same as createWorkoutButton)
        populateWorkoutButtonData(workoutButton, workout);

        // Add Leave/Delete button
        Button actionButton = workoutButton.findViewById(R.id.actionButton);
        actionButton.setText(isCreator ? "Delete Workout" : "Leave Workout");
        actionButton.setTextColor(Color.RED);
        actionButton.setOnClickListener(v -> {
            if (isCreator) {
                deleteWorkout(workout);
            } else {
                leaveWorkout(workout);
            }
        });

        // Add the action button to the workout button layout
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        container.addView(workoutButton);
    }

    private void populateWorkoutButtonData(View workoutButton, Map<String, Object> workout) {
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

    }
    private void leaveWorkout(Map<String, Object> workout) {
        String workoutId = (String) workout.get("workoutID");
        String userId = fAuth.getUid();
        List<String> participants = (List<String>) workout.get("Participants");

        DocumentReference workoutRef = fstore.collection("workouts").document(workoutId);
        DocumentReference userRef = fstore.collection("users").document(userId);

        workoutRef.update("Participants", FieldValue.arrayRemove(getFullName()),
                        "ParticipantsAmount", FieldValue.increment(-1))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userRef.update("workoutJoined", FieldValue.increment(-1))
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(this, "You have left the workout.", Toast.LENGTH_SHORT).show();
                                        loadUserWorkouts();
                                    } else {
                                        Toast.makeText(this, "Failed to update participation count.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Failed to leave the workout.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWorkout(Map<String, Object> workout) {
        String workoutId = (String) workout.get("workoutID");
        fstore.collection("workouts").document(workoutId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Workout deleted successfully.", Toast.LENGTH_SHORT).show();
                    loadUserWorkouts();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete the workout.", Toast.LENGTH_SHORT).show();
                });
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


}
