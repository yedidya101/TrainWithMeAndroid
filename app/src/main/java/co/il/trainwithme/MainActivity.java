package co.il.trainwithme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.net.*;
import java.io.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView ForgotPassword, SignUpText, SignUplink;
    EditText etusername, etpassword;
    Button btnLogin;
    String Password, username;
        //Socket clientSocket = new Socket("loaclhost", 5555);
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

    }

    @Override
    public void onClick(View v) {
        if(v == btnLogin)
        {
            if(etusername.getText().toString().length() > 0 && etpassword.getText().toString().length() > 0) {
                //try {
                    //PrintWriter pr = new PrintWriter(clientSocket.getOutputStream());

                //}

                Password = etpassword.getText().toString();
                username = etusername.getText().toString();
                etusername.setText("");
                etpassword.setText("");

                // redirect to home page.
                Intent loginintent = new Intent(MainActivity.this, HomePage.class);
                startActivity(loginintent);
            }
            else
            {
                Toast.makeText(this,"The UserName or The Password Is Incorrect.",Toast.LENGTH_LONG).show();
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