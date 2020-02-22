package com.discoveregypttourism.Activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.discoveregypttourism.Messenger.StaticConfig;
import com.discoveregypttourism.Messenger.data.SharedPreferenceHelper;
import com.discoveregypttourism.Messenger.model.User;
import com.discoveregypttourism.Messenger.util.ImageUtils;
import com.discoveregypttourism.Services.NotificationServices;
import com.discoveregypttourism.SocialComponents.activities.BaseActivity;
import com.discoveregypttourism.SocialComponents.model.Profile;
import com.discoveregypttourism.SocialComponents.utils.PreferencesUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.discoveregypttourism.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ybs.countrypicker.CountryPicker;
import com.ybs.countrypicker.CountryPickerListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignupActivity extends BaseActivity {

    private EditText inputEmail, inputPassword , name , country , birth , phone ;
    private RadioGroup gender ;
    private FirebaseAuth auth;
    private CountryPicker picker ;
    private TextView btnSignIn ;
    private Button register ;
    private ProgressDialog progress_spinner ;
    private String email , password , gender_text , date_text , country_text , phone_text , name_text , image_text ;
    private Calendar myCalendar ;
    private final int PICK_IMAGE_REQUEST = 71;
    private final int VIDEO_REQUEST = 72;
    private Uri filePath;
    private FirebaseStorage storage;
    private StorageReference storageReference ;
    private CircleImageView image ;
    private Bitmap bitmap ;
    private Profile user ;
    private TextInputLayout input_country , input_date;
    private String imageBase64 ;
    private String intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnSignIn = findViewById(R.id.login);
        name = findViewById(R.id.full_name);
        country = findViewById(R.id.country);
        birth = findViewById(R.id.date);
        phone = findViewById(R.id.phone_number);
        register = findViewById(R.id.register_button);
        gender = findViewById(R.id.gender);
        image = findViewById(R.id.image);
        input_country = findViewById(R.id.input_country);
        input_date = findViewById(R.id.input_date);

        intent = getIntent().getStringExtra("edit");

        if (intent.equals("yes")){
            gender.setVisibility(View.GONE);
            inputEmail.setVisibility(View.GONE);
            inputPassword.setVisibility(View.GONE);
            btnSignIn.setVisibility(View.GONE);
            findViewById(R.id.alredy_have_account).setVisibility(View.GONE);
            register.setText(R.string.edit);
            setEditedFields();
        }


        picker = CountryPicker.newInstance(getResources().getString(R.string.select_country));  // dialog title

        myCalendar = Calendar.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        image_text = "";
        imageBase64 = "default";

        final DatePickerDialog.OnDateSetListener date = new
                DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateLabel();
                    }

                };
   /*     birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    new DatePickerDialog(SignupActivity.this, date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
        });
        */

        birth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b == true){
                    new DatePickerDialog(SignupActivity.this,R.style.MyAlertDialogStyle, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            myCalendar.set(Calendar.YEAR, year);
                            myCalendar.set(Calendar.MONTH, monthOfYear);
                            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            updateLabel();
                        }
                    }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rb = findViewById(gender.getCheckedRadioButtonId());
                gender_text = rb.getText().toString();
            }
        });

     /*   country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
            }
        });
        */

        country.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b == true){
                    picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
                }
            }
        });

        picker.setListener(new CountryPickerListener() {
            @Override
            public void onSelectCountry(String s, String s1, String s2, int i) {
                country.setText(s);
                picker.dismiss();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        progress_spinner = new ProgressDialog(SignupActivity.this , R.style.MyAlertDialogStyle);
        progress_spinner.setMessage(getResources().getString(R.string.signing_up));
        progress_spinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress_spinner.setCancelable(false);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                email = inputEmail.getText().toString().trim();
                password = inputPassword.getText().toString().trim();
                country_text = country.getText().toString();
                phone_text = phone.getText().toString();
                date_text = birth.getText().toString();
                name_text = name.getText().toString();

                if (TextUtils.isEmpty(name_text)) {
                    name.setError(getResources().getString(R.string.enter_your_name));
                    name.setFocusable(true);
                    return;
                }

                if (TextUtils.isEmpty(country_text)) {
                    country.setError(getResources().getString(R.string.select_country));
                    country.setFocusable(true);
                    return;
                }
                if (TextUtils.isEmpty(date_text)) {
                    birth.setError(getResources().getString(R.string.enter_date));
                    birth.setFocusable(true);
                    return;
                }
                if (TextUtils.isEmpty(phone_text)) {
                    phone.setError(getResources().getString(R.string.enter_your_phone));
                    phone.setFocusable(true);
                    return;
                }

                if (TextUtils.isEmpty(image_text)) {
                    image.setFocusable(true);
                    return;
                }
                if (intent.equals("yes")){
                    updateUser(name_text , country_text , date_text , phone_text);
                    startActivity(new Intent(SignupActivity.this, InterestesActivity.class));
                    progress_spinner.hide();
                    finish();
                }
                else {
                    if (TextUtils.isEmpty(email)) {
                        inputEmail.setError(getResources().getString(R.string.enter_email_address));
                        inputEmail.setFocusable(true);
                        return;
                    }

                    if (TextUtils.isEmpty(password)) {
                        inputPassword.setError(getResources().getString(R.string.enter_password));
                        inputPassword.setFocusable(true);
                        return;
                    }

                    if (password.length() < 6) {
                        inputPassword.setError(getResources().getString(R.string.password_short));
                        inputPassword.setFocusable(true);
                        return;
                    }

                    progress_spinner.show();
                    //create user
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(SignupActivity.this, getResources().getString(R.string.auth_failed) + task.getException(),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        createUser(name_text, country_text, date_text, phone_text);
                                        startActivity(new Intent(SignupActivity.this, InterestesActivity.class));
                                        startService(new Intent(SignupActivity.this , NotificationServices.class));
                                        finish();
                                        PreferencesUtil.setProfileCreated(SignupActivity.this, true);
                                    }
                                    progress_spinner.hide();
                                }
                            });
                }

            }
        });
    }

    private void setEditedFields(){
        FirebaseDatabase.getInstance().getReference().child("profiles").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                name.setText(profile.getUsername());
                country.setText(profile.getCountry());
                birth.setText(profile.getBirthday());
                phone.setText(profile.getPhone());
                Glide.with(SignupActivity.this).load(profile.getPhotoUrl()).into(image);
                image_text = profile.getPhotoUrl();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("user/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imageBase64 = dataSnapshot.child("avata").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createUser (String name , String country , String birth , String phone) {

        user = new Profile (image_text , gender_text , name , country , birth , phone);
        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();
            User newUser = new User();
            newUser.email = firebaseUser.getEmail();
            newUser.name = name;
            newUser.avata = imageBase64;
        SharedPreferenceHelper preferenceHelper = SharedPreferenceHelper.getInstance(this);
        preferenceHelper.saveUserInfo(newUser);
        StaticConfig.UID = firebaseUser.getUid();
            FirebaseDatabase.getInstance().getReference().child("user/" + firebaseUser.getUid()).setValue(newUser);
        FirebaseDatabase.getInstance().getReference().child("profiles").child(firebaseUser.getUid()).setValue(user);

    }

    private void updateUser(String name , String country , String birth , String phone){
        user = new Profile (image_text , gender_text , name , country , birth , phone);
        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        User newUser = new User();
        newUser.email = firebaseUser.getEmail();
        newUser.name = name;
        newUser.avata = imageBase64;
        SharedPreferenceHelper preferenceHelper = SharedPreferenceHelper.getInstance(this);
        preferenceHelper.saveUserInfo(newUser);
        StaticConfig.UID = firebaseUser.getUid();
        FirebaseDatabase.getInstance().getReference().child("user/" + firebaseUser.getUid()).setValue(newUser);
        FirebaseDatabase.getInstance().getReference().child("profiles").child(firebaseUser.getUid()).setValue(user);

    }

    private void updateLabel() {

        String myFormat = "MM/dd/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        birth.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        progress_spinner.hide();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                uploadImage();

                InputStream inputStream = this.getContentResolver().openInputStream(data.getData());

                Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);
                imgBitmap = ImageUtils.cropToSquare(imgBitmap);
                InputStream is = ImageUtils.convertBitmapToInputStream(imgBitmap);
                final Bitmap liteImage = ImageUtils.makeImageLite(is,
                        imgBitmap.getWidth(), imgBitmap.getHeight(),
                        ImageUtils.AVATAR_WIDTH, ImageUtils.AVATAR_HEIGHT);

                imageBase64 = ImageUtils.encodeBase64(liteImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {

        if (filePath != null) {
            //displaying progress dialog while image is uploading
            final ProgressDialog progressDialog = new ProgressDialog(this , R.style.MyAlertDialogStyle);
            progressDialog.setTitle(getResources().getString(R.string.uploading));
            progressDialog.show();
            final StorageReference sRef = storageReference.child("uploads/" + System.currentTimeMillis() + "." + getFileExtension(filePath));
            UploadTask uploadTask = sRef.putFile(filePath) ;
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    progressDialog.dismiss();
                                    image_text = uri.toString();
                                    image.setImageBitmap(bitmap);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.failed_to_upload_photo), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //displaying the upload progress
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage(getResources().getString(R.string.uploading) + " " + ((int) progress) + "%...");
                            progressDialog.setCanceledOnTouchOutside(false);
                        }
                    });
        } else {
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}