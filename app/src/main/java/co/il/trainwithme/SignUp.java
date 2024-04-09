package co.il.trainwithme;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.DatePicker;
import android.app.DatePickerDialog;
import java.util.Calendar;
public class SignUp extends AppCompatActivity implements View.OnClickListener {
    private TextView  chosenDate, Back4;
    private Calendar selectedDate;
    private Button btnSignUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        chosenDate = findViewById(R.id.chosenDate);
        btnSignUp = findViewById(R.id.btnSignUp);
        Back4 = findViewById(R.id.Back4);

        Back4.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == btnSignUp)
        {
            Intent loginintent = new Intent(SignUp.this, HomePage.class);
            startActivity(loginintent);
        }
        if(v == Back4)
        {
            Intent loginintent = new Intent(SignUp.this, MainActivity.class);
            startActivity(loginintent);
        }
    }

    public void showDatePickerDialog(View v) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(SignUp.this,
                new DatePickerDialog.OnDateSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Set the selected date to the calendar
                        selectedDate = Calendar.getInstance();
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, monthOfYear);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // Calculate age
                        int age = calculateAge(selectedDate);

                        // Update the button text with the selected date and age
                        chosenDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year + " (Age: " + age + ")");
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private int calculateAge(Calendar birthDate) {
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }
}