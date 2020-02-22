package com.discoveregypttourism.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.discoveregypttourism.DB.TinyDB;
import com.discoveregypttourism.MainActivity;
import com.discoveregypttourism.Services.NotificationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.discoveregypttourism.R ;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private Button  loginbtn;
    private ImageButton backbtn;
    private TextView reset_password , register;
    private CheckBox remember_me ;
    private TinyDB tinyDB ;
    private String email , password ;
    private ProgressDialog progress_spinner ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            startService(new Intent(LoginActivity.this , NotificationServices.class));
            finish();
        }

        // set the view now
        setContentView(R.layout.activity_login);


        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        backbtn = findViewById(R.id.back_button);
        loginbtn = findViewById(R.id.login_button);
        reset_password = findViewById(R.id.forget_password_reset);
        register = findViewById(R.id.register_text);
        remember_me = findViewById(R.id.remember);
        backbtn = findViewById(R.id.back_button);

        progress_spinner = new ProgressDialog(LoginActivity.this , R.style.MyAlertDialogStyle);
        progress_spinner.setMessage(getResources().getString(R.string.signning_in));
        progress_spinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        tinyDB = new TinyDB(this);
        if (tinyDB.getString("remember").equals("no") || tinyDB.getString("remember").equals("")){
            remember_me.setChecked(false);
        }
        else {
            remember_me.setChecked(true);
            inputEmail.setText(tinyDB.getString("email"));
            inputPassword.setText(tinyDB.getString("password"));
        }

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                intent.putExtra("edit" , "no");
                startActivity(intent);
            }
        });

        reset_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress_spinner.show();
                email = inputEmail.getText().toString();
                password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.enter_email_address), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
                    return;
                }

                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    if (password.length() < 6) {
                                        inputPassword.setError(getString(R.string.minimum_password));
                                    } else {
                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    checkRemember();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                progress_spinner.hide();
                            }
                        });
            }
        });
    }

    private void checkRemember(){
        if (remember_me.isChecked()){
            tinyDB.putString("remember" , "yes");
            tinyDB.putString("email" , email);
            tinyDB.putString("password" , password);
        }
        else {
            tinyDB.putString("remember" , "no");
        }
    }
}