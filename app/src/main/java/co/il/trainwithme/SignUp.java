package co.il.trainwithme;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
public class SignUp extends AppCompatActivity implements View.OnClickListener {
    private TextView  chosenDate, Back4;
    private Calendar selectedDate;
    private Button btnSignUp;
    EditText mFirstName,mLastName, mpassword, memail ;
    FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        chosenDate = findViewById(R.id.chosenDate);
        btnSignUp = findViewById(R.id.btnSignUp);
        Back4 = findViewById(R.id.Back4);

        mFirstName = findViewById(R.id.FirstName);
        mLastName = findViewById(R.id.lastName);
        mpassword = findViewById(R.id.SetPasswordText);
        memail = findViewById(R.id.EmailRegister);



        Back4.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);

        fAuth = FirebaseAuth.getInstance();
        if(fAuth.getCurrentUser() != null)
        {
            Intent loginintent = new Intent(SignUp.this, HomePage.class);
            startActivity(loginintent);
            finish();
        }

    }

    @Override
    public void onClick(View v) {
        if(v == btnSignUp)
        {
            String email = memail.getText().toString().trim();
            String password = mpassword.getText().toString().trim();
            String firstName = mFirstName.getText().toString().trim();
            String lastName = mLastName.getText().toString().trim();
            chosenDate = findViewById(R.id.chosenDate);
            if(TextUtils.isEmpty(email)){
                memail.setError("Email is Required.");
                return;
            }

            if(TextUtils.isEmpty(password)){
                mpassword.setError("Password is Required.");
                return;
            }
            if(password.length() < 6){
                mpassword.setError("Password Must be >= 6 Characters");
                return;
            }
            //register the user in firebase
            fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(SignUp.this, "user created.",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), HomePage.class);
                        startActivity(intent);
                    }else{
                        Toast.makeText(SignUp.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

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