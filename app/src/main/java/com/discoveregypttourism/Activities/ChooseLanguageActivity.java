package com.discoveregypttourism.Activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.discoveregypttourism.DB.TinyDB;
import com.discoveregypttourism.R;
import com.discoveregypttourism.Utils.Statics;

import java.util.Locale;

public class ChooseLanguageActivity extends AppCompatActivity {

    private LinearLayout choose_languages , languages_list ;
    private ImageView arrow ;
    private boolean opened ;
    private TextView choose_language_text , language_text , english , arabic , frensh , italian , russian;
    private TinyDB db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_language);

         db = new TinyDB(this);

        if (db.getString("selectlan") != null && db.getString("selectlan").equals("yes")){
            changeLanguage(db.getString("lang"));
            startActivity(new Intent(ChooseLanguageActivity.this , LoginActivity.class));
            finish();
        }

        choose_languages = findViewById(R.id.choose_language);
        languages_list = findViewById(R.id.languages_layout);
        arrow = findViewById(R.id.arrow);
        choose_language_text = findViewById(R.id.choose_language_text);
        english = findViewById(R.id.english);
        arabic = findViewById(R.id.arabic);
        frensh = findViewById(R.id.frensh);
        russian = findViewById(R.id.russian);
        italian = findViewById(R.id.italic);
        language_text = findViewById(R.id.language_text);

        opened = false ;

        languages_list.setVisibility(View.GONE);

        choose_language_text.setTypeface(Statics.regular(this) , Typeface.BOLD);
        language_text.setTypeface(Statics.regular(this));

        english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeLanguage("en");
            }

        });

        arabic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeLanguage("ar");
            }
        });

        frensh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeLanguage("fr");
            }
        });

        russian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeLanguage("ru");
            }
        });

        italian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeLanguage("it");
            }
        });

        choose_languages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reverseCase();
            }
        });


    }

    private void reverseCase () {
        if (opened){
            opened = false ;
            languages_list.setVisibility(View.GONE);
            arrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_drop_down_black_24dp));
        }
        else {
            opened = true ;
            languages_list.setVisibility(View.VISIBLE);
            arrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_drop_up_black_24dp));
        }
    }

    private void changeLanguage(String language_code){
        Resources res = ChooseLanguageActivity.this.getResources();
// Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(new Locale(language_code.toLowerCase())); // API 17+ only.
        }
// Use conf.locale = new Locale(...) if targeting lower versions
        res.updateConfiguration(conf, dm);
        db.putString("selectlan" , "yes");
        db.putString("lang" , language_code);
        startActivity(new Intent(ChooseLanguageActivity.this , WelcomeActivity.class));
    }
}
