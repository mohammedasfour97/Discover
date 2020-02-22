package com.discoveregypttourism;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.discoveregypttourism.Activities.LoginActivity;
import com.discoveregypttourism.Messenger.fragments.FriendsFragment;
import com.discoveregypttourism.SocialComponents.fragments.HomeFragment;
import com.discoveregypttourism.SocialComponents.fragments.ProfileFragment;
import com.discoveregypttourism.SocialComponents.fragments.TrendFragment;


public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.home:
                               selectedFragment = HomeFragment.newInstance();
                                item.setIcon(R.drawable.ic_house_black_silhouette_without_door);
                                bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.ic_ttrend);
                                bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.message);
                                bottomNavigationView.getMenu().getItem(3).setIcon(R.drawable.pprofile);

                                break;
                            case R.id.trending:
                                selectedFragment = new TrendFragment();
                                item.setIcon(R.drawable.ic_trend);
                                bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.ic_hhouse);
                                bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.message);
                                bottomNavigationView.getMenu().getItem(3).setIcon(R.drawable.pprofile);
                                break;
                            case R.id.message:
                                selectedFragment = new FriendsFragment();
                                item.setIcon(R.drawable.messaage_fill);
                                bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.ic_ttrend);
                                bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.ic_hhouse);
                                bottomNavigationView.getMenu().getItem(3).setIcon(R.drawable.pprofile);
                                break;
                            case R.id.profile:
                                selectedFragment = new ProfileFragment();
                                item.setIcon(R.drawable.profile);
                                bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.ic_ttrend);
                                bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.ic_hhouse);
                                bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.message);
                                break;

                        }

                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_layout, selectedFragment);
                        transaction.commit();


                        return true;
                    }
                });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, HomeFragment.newInstance());
        transaction.commit();
        bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.ic_house_black_silhouette_without_door);

    }

    public void goToLogin () {
        startActivity(new Intent(MainActivity.this , LoginActivity.class));
        finish();
    }
}