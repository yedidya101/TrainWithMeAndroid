package co.il.trainwithme;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

public class SignUp extends AppCompatActivity implements View.OnClickListener {
    private TextView chosenDate, Back4, agreementText;
    private Calendar selectedDate;
    private Button btnSignUp;
    private EditText mFirstName, mLastName, mEmail, mPassword, mConfirmPassword;
    private RadioGroup radioGroupGender;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        chosenDate = findViewById(R.id.chosenDate);
        btnSignUp = findViewById(R.id.btnSignUp);
        Back4 = findViewById(R.id.Back4);
        agreementText = findViewById(R.id.agreementText);

        // Initialize TextInputLayout
        TextInputLayout firstNameLayout = findViewById(R.id.firstNameLayout);
        TextInputLayout lastNameLayout = findViewById(R.id.lastNameLayout);
        TextInputLayout emailLayout = findViewById(R.id.emailLayout);
        TextInputLayout passwordLayout = findViewById(R.id.passwordLayout);
        TextInputLayout confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);

        // Retrieve EditText from TextInputLayout
        mFirstName = firstNameLayout.getEditText();
        mLastName = lastNameLayout.getEditText();
        mEmail = emailLayout.getEditText();
        mPassword = passwordLayout.getEditText();
        mConfirmPassword = confirmPasswordLayout.getEditText();

        radioGroupGender = findViewById(R.id.radioGroupGender);

        Back4.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
        chosenDate.setOnClickListener(this);

        fAuth = FirebaseAuth.getInstance();
        /*if (fAuth.getCurrentUser() != null) {
            Intent loginIntent = new Intent(SignUp.this, HomePage.class);
            startActivity(loginIntent);
            finish();
        } */
    }

    @Override
    public void onClick(View v) {
        if (v == btnSignUp) {
            registerUser();
        } else if (v == Back4) {
            navigateBackToMain();
        } else if (v == chosenDate) {
            showDatePickerDialog();
        }
    }

    private void registerUser() {
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String confirmPassword = mConfirmPassword.getText().toString().trim();
        String firstName = mFirstName.getText().toString().trim();
        String lastName = mLastName.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be >= 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SignUp.this, "User Created.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUp.this, HomePage.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignUp.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateBackToMain() {
        Intent intent = new Intent(SignUp.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        chosenDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }
}
