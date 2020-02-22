package com.discoveregypttourism.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.discoveregypttourism.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ChangePasswordActivity extends AppCompatActivity {

    private ImageButton backntn ;
    private EditText new_password ;
    private Button change ;
    private ProgressDialog progress_spinner ;
    private FirebaseUser user;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        backntn = findViewById(R.id.back_button);
        new_password = findViewById(R.id.new_password);
        change = findViewById(R.id.change_button);

        progress_spinner = new ProgressDialog(ChangePasswordActivity.this);
        progress_spinner.setMessage(getResources().getString(R.string.processing));
        progress_spinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        user = FirebaseAuth.getInstance().getCurrentUser();

        backntn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress_spinner.show();
                user.updatePassword(new_password.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ChangePasswordActivity.this, getResources().getString(R.string.password_updated), Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(ChangePasswordActivity.this, getResources().getString(R.string.password_faild_updated), Toast.LENGTH_SHORT).show();
                                }
                                progress_spinner.hide();
                            }

                        });
            }
        });
    }
}
