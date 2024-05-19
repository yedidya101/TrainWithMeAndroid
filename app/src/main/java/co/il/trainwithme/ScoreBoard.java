package co.il.trainwithme;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ScoreBoard extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout llUsersList;
    private FirebaseFirestore db;
    private ImageButton personal3, createWorkout3, scoreboard3, homepage3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board);


        personal3 = findViewById(R.id.personal3);
        createWorkout3 = findViewById(R.id.createWorkout3);
        scoreboard3 = findViewById(R.id.scoreboard3);
        homepage3 = findViewById(R.id.homepage3);

        personal3.setOnClickListener(this);
        createWorkout3.setOnClickListener(this);
        scoreboard3.setOnClickListener(this);
        homepage3.setOnClickListener(this);



        db = FirebaseFirestore.getInstance();

        // Fetch users from Firestore
        getUsersFromFirestore();
    }

    private void getUsersFromFirestore() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<HashMap<String, Object>> userList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            HashMap<String, Object> user = (HashMap<String, Object>) document.getData();
                            if (user != null && user.containsKey("workoutJoined")) {
                                userList.add(user);
                            }
                        }
                        // Sort users based on workoutJoined
                        Collections.sort(userList, new Comparator<HashMap<String, Object>>() {
                            @Override
                            public int compare(HashMap<String, Object> u1, HashMap<String, Object> u2) {
                                int workoutJoined1 = (int) u1.getOrDefault("workoutJoined", 0);
                                int workoutJoined2 = (int) u2.getOrDefault("workoutJoined", 0);
                                return workoutJoined2 - workoutJoined1;
                            }
                        });
                        // Populate scoreboard with sorted users
                        populateScoreboard(userList);
                    } else {
                        // Handle errors
                    }
                });
    }

    private void populateScoreboard(List<HashMap<String, Object>> userList) {
        // Clear existing views
        //llUsersList.removeAllViews();
        /*
        // Iterate through sorted user list
        for (int i = 0; i < userList.size(); i++) {
            HashMap<String, Object> user = userList.get(i);
            View userView = getLayoutInflater().inflate(R.layout.user_item, null);

            TextView tvPlace = userView.findViewById(R.id.tvPlace);
            TextView tvUserName = userView.findViewById(R.id.tvUserName);
            TextView tvWorkouts = userView.findViewById(R.id.tvWorkouts);

            tvPlace.setText(String.valueOf(i + 1)); // Set place
            String firstName = (String) user.getOrDefault("firstName", "");
            String lastName = (String) user.getOrDefault("lastName", "");
            tvUserName.setText(firstName + " " + lastName); // Set username
            int workoutJoined = (int) user.getOrDefault("workoutJoined", 0);
            tvWorkouts.setText(String.valueOf(workoutJoined)); // Set workout count

            llUsersList.addView(userView); */
        }


    @Override
    public void onClick(View v) {
        if(v == personal3){
            startActivity(new Intent(this, PersonalArea.class));
            finish();
        }
        else if(v == createWorkout3){
            startActivity(new Intent(this, addWorkout.class));
            finish();
        }
        else if(v == scoreboard3){
            startActivity(new Intent(this, ScoreBoard.class));
            finish();
        }
        else if(v == homepage3){
            startActivity(new Intent(this, HomePage.class));
            finish();
        }
    }
}
