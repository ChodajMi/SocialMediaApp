package com.example.socialmediaapplication;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String myuid;
    ActionBar actionBar;
    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile Activity");
        firebaseAuth = FirebaseAuth.getInstance();

        // Fix the navigationView ID
        navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);
        actionBar.setTitle("Feed");

        // When we open the application first time, the home/feed fragment should be shown
        HomeFragment fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        // Fix the content ID to flFragment
        fragmentTransaction.replace(R.id.flFragment, fragment, "");
        fragmentTransaction.commit();
    }

    // Use if-else instead of switch-case
    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            int itemId = menuItem.getItemId();

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            if (itemId == R.id.nav_home) {
                actionBar.setTitle("Home");
                HomeFragment fragment = new HomeFragment();
                fragmentTransaction.replace(R.id.flFragment, fragment, "");
                fragmentTransaction.commit();
                return true;

            } else if (itemId == R.id.nav_profile) {
                actionBar.setTitle("Profile");
                ProfileFragment fragment1 = new ProfileFragment();
                fragmentTransaction.replace(R.id.flFragment, fragment1);
                fragmentTransaction.commit();
                return true;

            } else if (itemId == R.id.nav_users) {
                actionBar.setTitle("Users");
                UsersFragment fragment2 = new UsersFragment();
                fragmentTransaction.replace(R.id.flFragment, fragment2, "");
                fragmentTransaction.commit();
                return true;

            } else if (itemId == R.id.nav_chat) {
                actionBar.setTitle("Notification");
                NotificationFragment listFragment = new NotificationFragment();
                fragmentTransaction.replace(R.id.flFragment, listFragment, "");
                fragmentTransaction.commit();
                return true;

            } else if (itemId == R.id.nav_addblogs) {
                actionBar.setTitle("Add Blogs");
                AddBlogsFragment fragment4 = new AddBlogsFragment();
                fragmentTransaction.replace(R.id.flFragment, fragment4, "");
                fragmentTransaction.commit();
                return true;
            }

            return false;
        }
    };
}
