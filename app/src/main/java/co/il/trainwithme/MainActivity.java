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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView ForgotPassword, SignUpText, SignUplink;
    EditText etusername, etpassword;
    Button btnLogin;
    String Password, username;
    Socket sock;
    PrintWriter printWriter;
    int port = 5555;
    String ip = "3.75.158.163";
    boolean FirstConnection = true;

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
            if(etusername.getText().toString().length() > 0 && etpassword.getText().toString().length() > 0) { //check if username and password are not empty
                Password = etpassword.getText().toString();
                username = etusername.getText().toString();
                etusername.setText("");
                etpassword.setText("");
                String opcode = "2";
                String userInfo = username + "," + Password;



                org.json.simple.JSONObject jsonObject = new org.json.simple.JSONObject(); // convert dictionary to json object for sending to server
                jsonObject.put("name", username);
                jsonObject.put("opcode", opcode);
                jsonObject.put("msg", userInfo);


                try {
                    if (sock == null) {
                        sock = new Socket(ip, port);
                    }

                    DataOutputStream outToServer = new DataOutputStream(sock.getOutputStream());
                    if (FirstConnection) {
                        outToServer.writeBytes( "Fuck\n");
                        // Receive response from the server
                        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                        String response = inFromServer.readLine();
                        FirstConnection = false;
                    }
                    PrintWriter pr = new PrintWriter(sock.getOutputStream());
                    pr.println(jsonObject);
                    pr.flush(); // sending massages to server


                    InputStream inputStream = sock.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    String data = br.readLine();
                    System.out.println(data);

                    // redirect to home page.
                    Intent loginintent = new Intent(MainActivity.this, HomePage.class);
                    startActivity(loginintent);

                } catch(IOException e) {
                    Intent loginintent = new Intent(MainActivity.this, HomePage.class);
                    startActivity(loginintent);
                }
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

    private boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException e) {
            try {
                new JSONArray(test);
            }
            catch (JSONException ex) {
                return false;
            }
        }
        return true;
    }

}



