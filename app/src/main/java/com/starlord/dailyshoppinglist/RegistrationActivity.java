package com.starlord.dailyshoppinglist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {

    private EditText email, pass;
    private TextView login;
    private Button btnReg;
    private ProgressDialog mDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email_reg);
        pass = findViewById(R.id.password_reg);
        login = findViewById(R.id.login_text);
        btnReg = findViewById(R.id.btn_reg);

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mEmail = email.getText().toString().trim();
                String mPass = pass.getText().toString().trim();

                if (TextUtils.isEmpty(mEmail)){
                    email.setError("Required field...");
                    return;
                }
                if (TextUtils.isEmpty(mPass)){
                    pass.setError("Required field...");
                    return;
                }

                mDialog.setMessage("Processing...");
                mDialog.show();

                mAuth.createUserWithEmailAndPassword(mEmail,mPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                       if (task.isSuccessful()){
                           startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                           Toast.makeText(RegistrationActivity.this, "User registration successful", Toast.LENGTH_SHORT).show();
                           mDialog.dismiss();
                           finish();
                       }
                       else{
                           Toast.makeText(RegistrationActivity.this, "User registration failed", Toast.LENGTH_SHORT).show();
                           mDialog.dismiss();
                       }
                    }
                });
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });
    }
}
