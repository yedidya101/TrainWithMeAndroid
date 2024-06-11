package co.il.trainwithme;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth fAuth;
    TextView ForgotPassword, SignUpText, SignUplink;
    EditText etusername, etpassword;
    Button btnLogin;
    String hashedPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ForgotPassword = findViewById(R.id.forgotPasswordButton);
        ForgotPassword.setPaintFlags(ForgotPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        SignUplink = findViewById(R.id.signUplink);
        SignUplink.setPaintFlags(SignUplink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        SignUpText = findViewById(R.id.signUptext);

        etusername = findViewById(R.id.etUserName);
        etpassword = findViewById(R.id.etpassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(this);
        ForgotPassword.setOnClickListener(this);
        SignUplink.setOnClickListener(this);

        fAuth = FirebaseAuth.getInstance();

        if (fAuth.getCurrentUser() != null) {
            Intent loginIntent = new Intent(MainActivity.this, HomePage.class);
            startActivity(loginIntent);
            finish();
        }

    }

    @Override
    public void onClick(View v) {
        if (v == ForgotPassword) {
            showForgotPasswordDialog();
        } else if (v == SignUplink) {
            Intent SignUpintent = new Intent(MainActivity.this, SignUp.class);
            startActivity(SignUpintent);
        } else if (v == btnLogin) {
            loginUser();
        }
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_forgot_password, null);
        builder.setView(dialogView);

        final EditText etEmail = dialogView.findViewById(R.id.etEmail);
        Button btnSend = dialogView.findViewById(R.id.btnSend);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(MainActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                    return;
                }

                fAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(MainActivity.this, "Error: " + "Email or password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void loginUser() {
        String email = etusername.getText().toString().trim();
        String password = etpassword.getText().toString().trim();


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

        fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Logged in successfully.", Toast.LENGTH_SHORT).show();
                    Intent loginIntent = new Intent(MainActivity.this, HomePage.class);
                    startActivity(loginIntent);
                } else {
                    Toast.makeText(MainActivity.this, "Login failed: "+ "Wrong Email or Password", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}
