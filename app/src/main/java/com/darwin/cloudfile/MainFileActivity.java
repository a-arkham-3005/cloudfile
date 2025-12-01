package com.darwin.cloudfile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.os.BuildCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainFileActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    OnBackPressedCallback callback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_file);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        drawerLayout = findViewById(R.id.window);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar); // Set the toolbar as the activity's app bar

        // Setup the ActionBarDrawerToggle to sync the drawer with the action bar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle navigation view item clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Swap the fragment in the container
                selectDrawerItem(item);
                return true;
            }
        });
        // Load the default fragment initially
        if (savedInstanceState == null) {
            loadFragment(new FilesFragment(), "/");
            navigationView.setCheckedItem(R.id.nav_files);
        }
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(navigationView)) {
                    drawerLayout.closeDrawer(navigationView);
                } else {
                    AlertDialog.Builder bld=new AlertDialog.Builder(MainFileActivity.this);
                    bld.setTitle(R.string.info_dialogbox_title);
                    bld.setMessage("Вы действительно хотите выйти из программы?");
                    bld.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishAffinity();
                        }
                    });
                    bld.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    bld.create().show();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
    static class JsonQuotaUsed{
        long quota;
        long used;
        public long getQuota() {
            return quota;
        }
        public long getUsed() {
            return used;
        }
    }
    private void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment = null;
        Class fragmentClass;
        String title;
        if(menuItem.getItemId()==R.id.nav_files){
            fragmentClass = FilesFragment.class;
            title="/";
        }else if(menuItem.getItemId()==R.id.nav_personal){
            fragmentClass = PersonalFragment.class;
            title=menuItem.getTitle().toString();
        }else if(menuItem.getItemId()==R.id.nav_about){
            fragmentClass = AboutFragment.class;
            title=menuItem.getTitle().toString();
        }else{
            fragmentClass = FilesFragment.class;
            title="/";
        }
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadFragment(fragment, title);

        // Close the navigation drawer
        drawerLayout.closeDrawers();
    }
    private void loadFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Replace existing fragment with the new one
        fragmentTransaction.replace(R.id.fragCont, fragment, tag)
                .setReorderingAllowed(true)
                .commit();
        // Set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(tag);
        }
    }

}
