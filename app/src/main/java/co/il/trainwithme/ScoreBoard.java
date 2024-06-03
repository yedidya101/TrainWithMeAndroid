package co.il.trainwithme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ScoreBoard extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout llUsersList;
    private FirebaseFirestore db;
    private ImageButton personal3, createWorkout3, scoreboard3, homepage3;
    private TextView monthTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board);

        llUsersList = findViewById(R.id.llUsersList);
        personal3 = findViewById(R.id.personal3);
        createWorkout3 = findViewById(R.id.createWorkout3);
        scoreboard3 = findViewById(R.id.scoreboard3);
        homepage3 = findViewById(R.id.homepage3);

        personal3.setOnClickListener(this);
        createWorkout3.setOnClickListener(this);
        scoreboard3.setOnClickListener(this);
        homepage3.setOnClickListener(this);


        scoreboard3.setBackgroundResource(R.drawable.scoreboardchosen);

        monthTextView = findViewById(R.id.month);
        setMonthText();

        db = FirebaseFirestore.getInstance();

        // Fetch users from Firestore
        getUsersFromFirestore();
        scheduleMonthlyReset();
    }

    private void setMonthText() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        String month = monthFormat.format(new Date());
        monthTextView.setText("Month: " + month);
    }

    private void getUsersFromFirestore() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<HashMap<String, Object>> userList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HashMap<String, Object> user = (HashMap<String, Object>) document.getData();
                            if (user != null && user.containsKey("workoutJoined")) {
                                userList.add(user);
                            }
                        }
                        // Sort users based on workoutJoined
                        Collections.sort(userList, new Comparator<HashMap<String, Object>>() {
                            @Override
                            public int compare(HashMap<String, Object> u1, HashMap<String, Object> u2) {
                                int workoutJoined1 = ((Long) u1.getOrDefault("workoutJoined", 0L)).intValue();
                                int workoutJoined2 = ((Long) u2.getOrDefault("workoutJoined", 0L)).intValue();
                                return workoutJoined2 - workoutJoined1;
                            }
                        });
                        // Populate scoreboard with sorted users
                        populateScoreboard(userList);
                    } else {
                        Toast.makeText(ScoreBoard.this, "Error getting users data.", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error getting users data", task.getException());
                    }
                });
    }

    private void populateScoreboard(List<HashMap<String, Object>> userList) {
        llUsersList.removeAllViews();

        for (int i = 0; i < Math.min(userList.size(), 10); i++) {
            HashMap<String, Object> user = userList.get(i);
            View userView = getLayoutInflater().inflate(R.layout.user_item, null);

            TextView tvPlace = userView.findViewById(R.id.tvPlace);
            TextView tvUserName = userView.findViewById(R.id.tvUserName);
            TextView tvWorkouts = userView.findViewById(R.id.tvWorkouts);
            TextView topTraineeBadge = userView.findViewById(R.id.topTraineeBadge);

            tvPlace.setText(String.valueOf(i + 1));
            String firstName = (String) user.getOrDefault("firstName", "");
            String lastName = (String) user.getOrDefault("lastName", "");
            tvUserName.setText(firstName + " " + lastName);
            int workoutJoined = ((Long) user.getOrDefault("workoutJoined", 0L)).intValue();
            tvWorkouts.setText(String.valueOf(workoutJoined));

            // Show the badge for the top user
            if (i == 0) {
                topTraineeBadge.setVisibility(View.VISIBLE);
            } else {
                topTraineeBadge.setVisibility(View.GONE);
            }

            llUsersList.addView(userView);
        }
    }

    private void scheduleMonthlyReset() {
        Timer timer = new Timer();
        TimerTask resetTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    // Get the current top user and update their profile with "lastMonthTopTrainee"
                    db.collection("users")
                            .orderBy("workoutJoined", com.google.firebase.firestore.Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    DocumentReference topUserRef = task.getResult().getDocuments().get(0).getReference();
                                    topUserRef.update("lastMonthTopTrainee", true);
                                } else {
                                    Log.e("Firestore", "Error finding top user", task.getException());
                                }
                            });

                    // Reset the "workoutJoined" field for all users
                    db.collection("users")
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        DocumentReference userRef = document.getReference();
                                        userRef.update("workoutJoined", 0);
                                    }
                                } else {
                                    Log.e("Firestore", "Error resetting users data", task.getException());
                                }
                            });

                    // Refresh the scoreboard
                    getUsersFromFirestore();
                });
            }
        };

        // Schedule the task to run at the end of each month
        timer.schedule(resetTask, getEndOfMonthTime(), 30L * 24 * 60 * 60 * 1000); // every month
    }

    private Date getEndOfMonthTime() {
        // Get the date for the end of the current month
        Date now = new Date();
        Date endOfMonth = new Date(now.getYear(), now.getMonth() + 1, 0, 23, 59, 59);
        return endOfMonth;
    }

    @Override
    public void onClick(View v) {
        if (v == personal3) {
            startActivity(new Intent(this, PersonalArea.class));
            finish();
        } else if (v == createWorkout3) {
            startActivity(new Intent(this, addWorkout.class));
            finish();
        } else if (v == scoreboard3) {
            startActivity(new Intent(this, ScoreBoard.class));
            finish();
        } else if (v == homepage3) {
            startActivity(new Intent(this, HomePage.class));
            finish();
        }
    }
}
