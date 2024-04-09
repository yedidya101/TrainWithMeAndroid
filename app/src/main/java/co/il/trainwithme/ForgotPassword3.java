package co.il.trainwithme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ForgotPassword3 extends AppCompatActivity implements View.OnClickListener{

    TextView BackText;
    Button ConfirmButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password3);

        BackText = (TextView) findViewById(R.id.Back3); // connect sign up text in screen to xml
        BackText.setPaintFlags(BackText.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG); // set sign up text underline

        ConfirmButton = (Button) findViewById(R.id.btnConfirm3);

        ConfirmButton.setOnClickListener(this);
        BackText.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == BackText)
        {
            Intent intent = new Intent(ForgotPassword3.this, ForgotPassword.class);
            startActivity(intent);
        }
        if(v == ConfirmButton)
        {
            Toast.makeText(this,"The password has been updated",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ForgotPassword3.this, HomePage.class);
            startActivity(intent);
        }
    }
}