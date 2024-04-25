package co.il.trainwithme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class HomePage extends AppCompatActivity implements View.OnClickListener  {
    ImageButton addWorkoutButton;
    ImageButton personalAreaButton;
    ImageButton ScoreBoardButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        addWorkoutButton = (ImageButton) findViewById(R.id.createWorkout);
        personalAreaButton = (ImageButton) findViewById(R.id.personal);
        ScoreBoardButton = (ImageButton) findViewById(R.id.scoreboard);


        addWorkoutButton.setOnClickListener(this);
        personalAreaButton.setOnClickListener(this);
        ScoreBoardButton.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        if(v == addWorkoutButton){
            Intent intent = new Intent(HomePage.this, addWorkout.class);
            startActivity(intent);
        }
        if(v == personalAreaButton){
            Intent intent = new Intent(HomePage.this, PersonalArea.class);
            startActivity(intent);
        }
        if(v == ScoreBoardButton){
            Intent intent = new Intent(HomePage.this, ScoreBoard.class);
            startActivity(intent);
        }
    }
}