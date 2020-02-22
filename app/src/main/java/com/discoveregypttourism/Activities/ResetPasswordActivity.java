package com.discoveregypttourism.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.discoveregypttourism.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText inputEmail;
    private Button btnReset;
    private ImageButton btnBack;
    private FirebaseAuth auth;
    private ProgressDialog progress_spinner ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        inputEmail = (EditText) findViewById(R.id.email);
        btnReset = (Button) findViewById(R.id.send_button);
        btnBack = findViewById(R.id.back_button);


        auth = FirebaseAuth.getInstance();

        progress_spinner = new ProgressDialog(ResetPasswordActivity.this);
        progress_spinner.setMessage(getResources().getString(R.string.processing));
        progress_spinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progress_spinner.show();

                String email = inputEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplication(), getResources().getString(R.string.enter_email_address), Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ResetPasswordActivity.this, getResources().getString(R.string.we_have_sent_you_instructions_to_reset_your_password), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ResetPasswordActivity.this, getResources().getString(R.string.failed_to_send_reset_email), Toast.LENGTH_SHORT).show();
                                }

                                progress_spinner.hide();
                            }
                        });
            }
        });
    }

}