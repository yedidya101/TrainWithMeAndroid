package co.il.trainwithme;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
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
    String Password, username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        ForgotPassword = (TextView)findViewById(R.id.forgotPasswordButton); // connect forgot password in screen to xml
        ForgotPassword.setPaintFlags(ForgotPassword.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG); // set forgot password text underline


        SignUplink = (TextView)findViewById(R.id.signUplink); // connect sign up text in screen to xml
        SignUplink.setPaintFlags(SignUplink.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG); // set sign up text underline

        SignUpText = (TextView)findViewById(R.id.signUptext);

        //connect between buttons and edit text to xml
        etusername = (EditText)findViewById(R.id.etUserName);
        etpassword = (EditText)findViewById(R.id.etpassword);
        btnLogin = (Button)findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(this); //listen to login button to logg in.
        ForgotPassword.setOnClickListener(this); //listen to forgot password text to get recovery email
        SignUplink.setOnClickListener(this); //listen to sign up text to move to sign up page

        fAuth = FirebaseAuth.getInstance();

    }

    /*@Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    } */
    @Override
    public void onClick(View v) {
        if(v == ForgotPassword){
            Intent Forgotpassintent = new Intent(MainActivity.this, ForgotPassword.class);
            startActivity(Forgotpassintent);
        }
        if(v == SignUplink){

            Intent SignUpintent = new Intent(MainActivity.this, SignUp.class);
            startActivity(SignUpintent);
        }

        if(v == btnLogin)
        {

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
                Password = etpassword.getText().toString();
                username = etusername.getText().toString();
                etusername.setText("");
                etpassword.setText("");

                fAuth.signInWithEmailAndPassword(username, Password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "logged in successfully.",Toast.LENGTH_SHORT).show();
                            Intent loginintent = new Intent(MainActivity.this, HomePage.class);
                            startActivity(loginintent);
                        } else {
                            Toast.makeText(MainActivity.this, "logg in failed email or password is incorrect." + task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                // redirect to home page.

            }
            else
            {
                Toast.makeText(this,"Incorrect Username or Password.",Toast.LENGTH_LONG).show();
            }
        }

    }




