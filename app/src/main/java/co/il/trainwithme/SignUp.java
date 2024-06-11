package co.il.trainwithme;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity implements View.OnClickListener {
    int workoutJoined = 0;
    private TextView chosenDate, Back4, agreementText;
    private Calendar selectedDate;
    private Button btnSignUp;
    private EditText mFirstName, mLastName, mEmail, mPassword, mConfirmPassword, mUsername;
    private RadioGroup radioGroupGender;
    private RadioButton radioButton;
    private FirebaseAuth fAuth;
    private String gender, userID, birthdate, hashedpassword;
    private FirebaseFirestore fstore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        chosenDate = findViewById(R.id.chosenDate);
        btnSignUp = findViewById(R.id.btnSignUp);
        Back4 = findViewById(R.id.Back4);
        agreementText = findViewById(R.id.agreementText);
        radioGroupGender = findViewById(R.id.radioGroupGender);

        // Initialize TextInputLayout
        TextInputLayout usernameLayout = findViewById(R.id.usernameLayout);
        TextInputLayout firstNameLayout = findViewById(R.id.firstNameLayout);
        TextInputLayout lastNameLayout = findViewById(R.id.lastNameLayout);
        TextInputLayout emailLayout = findViewById(R.id.emailLayout);
        TextInputLayout passwordLayout = findViewById(R.id.passwordLayout);
        TextInputLayout confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);


        // Retrieve EditText from TextInputLayout
        mUsername = usernameLayout.getEditText();
        mFirstName = firstNameLayout.getEditText();
        mLastName = lastNameLayout.getEditText();
        mEmail = emailLayout.getEditText();
        mPassword = passwordLayout.getEditText();
        mConfirmPassword = confirmPasswordLayout.getEditText();


        agreementText.setPaintFlags(agreementText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        Back4.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
        chosenDate.setOnClickListener(this);

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
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
        String username = mUsername.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String confirmPassword = mConfirmPassword.getText().toString().trim();
        String firstName = mFirstName.getText().toString().trim();
        String lastName = mLastName.getText().toString().trim();
        gender = null;
        int selectedId = radioGroupGender.getCheckedRadioButtonId(); // Get the id of the selected radio button
        if (selectedId != -1) {
            radioButton = (RadioButton) findViewById(selectedId);
            gender = radioButton.getText().toString(); // Get the text from the selected RadioButton
        }

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Username is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gender == null) {
            Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must contain >= 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(firstName)) {
            Toast.makeText(this, "First Name is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            Toast.makeText(this, "Last Name is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(birthdate)) {
            Toast.makeText(this, "Birthdate is required.", Toast.LENGTH_SHORT).show();
            return;
        }


        if (selectedDate != null && selectedDate.after(Calendar.getInstance())) {
            Toast.makeText(this, "Birthdate cannot be in the future.", Toast.LENGTH_SHORT).show();
            return;
        }


        fstore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) { // Check if the query was successful
                            if (!task.getResult().isEmpty()) { // Check if the username is already taken
                                Toast.makeText(SignUp.this, "Username occupied, please choose another one.", Toast.LENGTH_SHORT).show();
                            } else {
                                createFirebaseUser(username, email, password, firstName, lastName, birthdate, gender);
                            }
                        } else {
                            Log.d("TAG", "Error 5555: " + task.getException().getMessage());
                            Toast.makeText(SignUp.this, "Error checking username availability: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void createFirebaseUser(String username, String email, String password, String firstName, String lastName, String birthdate, String gender) {
        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Send verification email
                    FirebaseUser fuser = fAuth.getCurrentUser();
                    fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(SignUp.this, "Verification Email Sent.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignUp.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    hashedpassword = hashPassword(password);

                    userID = fAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = fstore.collection("users").document(userID);
                    Map<String, Object> user = new HashMap<>();
                    user.put("username", username);
                    user.put("firstName", firstName);
                    user.put("lastName", lastName);
                    user.put("birthdate", birthdate);
                    user.put("email", email);
                    user.put("password", hashedpassword);
                    user.put("gender", gender);
                    user.put("workoutJoined", workoutJoined);
                    user.put("workoutCreated", 0);

                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TAG", "DocumentSnapshot added with ID: " + userID);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("TAG", "Error adding document", e);
                        }
                    });

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
                    public void onDateSet(DatePicker view, int chosenyear, int chosenmonth, int dayOfMonth) {
                        selectedDate = Calendar.getInstance();
                        selectedDate.set(chosenyear, chosenmonth, dayOfMonth);
                        birthdate = dayOfMonth + "/" + (chosenmonth + 1) + "/" + chosenyear;
                        chosenDate.setText("Birthdate: " + dayOfMonth + "/" + (chosenmonth + 1) + "/" + chosenyear);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
