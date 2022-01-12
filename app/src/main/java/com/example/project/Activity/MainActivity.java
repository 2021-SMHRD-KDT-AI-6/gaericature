package com.example.project.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.project.Fragment.Gallery;
import com.example.project.Fragment.Home;
import com.example.project.Fragment.MyPage;
import com.example.project.Fragment.Purchase;
import com.example.project.R;
import com.example.project.RbPreference;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 0;
    ImageView imgGallery;

    BottomNavigationView navView;
    Gallery gallery;
    Home home;
    Purchase purchase;
    MyPage myPage;

   public static int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgGallery = findViewById(R.id.imgGallery);

        navView = findViewById(R.id.navView);
        gallery = new Gallery();
        home = new Home();
        purchase = new Purchase();
        myPage = new MyPage();

        getSupportFragmentManager().beginTransaction().replace(R.id.frame, home).commit();

        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.ItemGallery:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame, gallery).commit();
                        break;
                    case R.id.ItemHome:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame, home).commit();
                        break;
                    case R.id.itemPurchase:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame, purchase).commit();
                        break;
                    case R.id.ItemMyPage:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame, myPage).commit();
                        break;
                }
                return true;
            }
        });
        // 세션에서 아이디 가져오기
//        RbPreference pref = new RbPreference(this);
//        String user_id = pref.getValue("user_id", null);
//
//        Log.d("session", user_id);
    }
}