package com.example.rahulsingh.pseudo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity
{
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;

    private Button ResetPasswordSendEmailButton;
    private EditText ResetEmailInput;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar)findViewById(R.id.forgot_password_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reset Password");

        ResetPasswordSendEmailButton = (Button)findViewById(R.id.reset_password_btn);
        ResetEmailInput = (EditText)findViewById(R.id.reset_password_email);

        ResetPasswordSendEmailButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String userEmail = ResetEmailInput.getText().toString();

                if(TextUtils.isEmpty(userEmail))
                {
                    Toast.makeText(ResetPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(ResetPasswordActivity.this, "Password reset link has been sent to your email", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(ResetPasswordActivity.this, "Error occured: "+message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}