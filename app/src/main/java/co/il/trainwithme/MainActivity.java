package co.il.trainwithme;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //private FirebaseAuth mAuth;
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

        /*mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null)
        {
            Intent loginintent = new Intent(MainActivity.this, HomePage.class);
            startActivity(loginintent);
            finish();
        }
*/
    }

    /*@Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    } */
    @Override
    public void onClick(View v) {
        if(v == btnLogin)
        {
            if(etusername.getText().toString().length() > 0 && etpassword.getText().toString().length() > 0) { //check if username and password are not empty
                Password = etpassword.getText().toString();
                username = etusername.getText().toString();
                etusername.setText("");
                etpassword.setText("");

               /* mAuth.signInWithEmailAndPassword(username, Password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(MainActivity.this, "Authentication success.",Toast.LENGTH_SHORT).show();
                            Intent loginintent = new Intent(MainActivity.this, HomePage.class);
                            startActivity(loginintent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed." + task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                // redirect to home page.
*/
            }
            else
            {
                Toast.makeText(this,"Incorrect Username or Password.",Toast.LENGTH_LONG).show();
            }
        }
        if(v == ForgotPassword){
            Intent Forgotpassintent = new Intent(MainActivity.this, ForgotPassword.class);
            startActivity(Forgotpassintent);
        }
        if(v == SignUplink){

            Intent SignUpintent = new Intent(MainActivity.this, SignUp.class);
            startActivity(SignUpintent);
        }
    }
}



