package es.upm.miw.sparrow.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.widget.Toolbar;

import es.upm.miw.sparrow.R;
import es.upm.miw.sparrow.ui.fragments.MusicFragment;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences.Editor prefs;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_view), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupDrawer(toolbar);

        prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit();

        Bundle bundle = getIntent().getExtras();
        String email = bundle.getString("email", null);
        setup(email);
        setButtonListeners();
    }

    //region Menu
    public void setupDrawer(Toolbar toolbar){
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();

        if (id == R.id.nav_edit_profile) {
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.btnLogout) {
            dataClear();
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this, AuthActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    //endregion

    public void setup(String email){
        View header = navigationView.getHeaderView(0);
        TextView emailText = header.findViewById(R.id.email);
        emailText.setText(email);
        prefs.putString("email", email);
        prefs.apply();
    }

    public void dataClear(){
        prefs.clear();
        prefs.apply();
    }

    public void setButtonListeners(){
        ImageButton btnMusic = findViewById(R.id.btnMusic);
        ImageButton btnArt = findViewById(R.id.btnArt);
        ImageButton btnMaths = findViewById(R.id.btnMaths);
        ImageButton btnLanguage = findViewById(R.id.btnLanguage);
        ImageButton btnEnglish = findViewById(R.id.btnEnglish);

        btnMusic.setOnClickListener(v->{
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new MusicFragment())
                    .addToBackStack("music")
                    .commit();
        });
        btnArt.setOnClickListener(v->{

        });
        btnMaths.setOnClickListener(v->{

        });
        btnLanguage.setOnClickListener(v->{

        });
        btnEnglish.setOnClickListener(v->{

        });

    }
}