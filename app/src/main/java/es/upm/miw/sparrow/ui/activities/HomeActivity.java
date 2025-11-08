package es.upm.miw.sparrow.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.widget.Toolbar;

import es.upm.miw.sparrow.R;
import es.upm.miw.sparrow.data.local.AvatarPrefs;
import es.upm.miw.sparrow.data.local.AvatarUrlBuilder;
import es.upm.miw.sparrow.ui.fragments.ArtFragment;
import es.upm.miw.sparrow.ui.fragments.EditProfileFragment;
import es.upm.miw.sparrow.ui.fragments.EnglishFragment;
import es.upm.miw.sparrow.ui.fragments.LanguageFragment;
import es.upm.miw.sparrow.ui.fragments.MatchesFragment;
import es.upm.miw.sparrow.ui.fragments.MathsFragment;
import es.upm.miw.sparrow.ui.fragments.MusicFragment;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences.Editor prefs;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    ImageButton btnMusic;
    ImageButton btnArt;
    ImageButton btnMaths;
    ImageButton btnLanguage;
    ImageButton btnEnglish;
    private int initialBackStack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        initialBackStack = getSupportFragmentManager().getBackStackEntryCount();

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            int count = getSupportFragmentManager().getBackStackEntryCount();
            enableButtons(count == initialBackStack);
        });
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
            SharedPreferences sp = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
            String email = sp.getString("email", null);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, EditProfileFragment.newInstance(email))
                    .addToBackStack("edit_profile")
                    .commit();
        } else if (id == R.id.matches){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new MatchesFragment())
                    .addToBackStack("matches")
                    .commit();

        }else if (id == R.id.nav_settings) {

        } else if (id == R.id.btnLogout) {
            dataClear();
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this, AuthActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }else if (id == R.id.btnExit){
            finishAffinity();
            System.exit(0);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHeaderAvatar();
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
        refreshHeaderAvatar();
    }

    public void refreshHeaderAvatar() {
        View header = navigationView.getHeaderView(0);
        ImageView photo = header.findViewById(R.id.profilePhoto);

        SharedPreferences sp = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        String email = sp.getString("email", null);

        String seed = AvatarPrefs.getSeed(this, email);
        String url = AvatarUrlBuilder.buildAutoBg(seed, 256);
        Glide.with(this).load(url).placeholder(R.drawable.ic_sparrow_rounded).into(photo);
    }

    public void dataClear(){
        prefs.clear();
        prefs.apply();
    }

    public void setButtonListeners(){
        btnMusic = findViewById(R.id.btnMusic);
        btnArt = findViewById(R.id.btnArt);
        btnMaths = findViewById(R.id.btnMaths);
        btnLanguage = findViewById(R.id.btnLanguage);
        btnEnglish = findViewById(R.id.btnEnglish);

        btnMusic.setOnClickListener(v->{
            enableButtons(false);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new MusicFragment())
                    .addToBackStack("music")
                    .commit();
        });
        btnArt.setOnClickListener(v->{
            enableButtons(false);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new ArtFragment())
                    .addToBackStack("art")
                    .commit();
        });
        btnMaths.setOnClickListener(v->{
            enableButtons(false);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new MathsFragment())
                    .addToBackStack("maths")
                    .commit();
        });
        btnLanguage.setOnClickListener(v->{
            enableButtons(false);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new LanguageFragment())
                    .addToBackStack("language")
                    .commit();
        });
        btnEnglish.setOnClickListener(v->{
            enableButtons(false);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new EnglishFragment())
                    .addToBackStack("english")
                    .commit();
        });
    }

    public void enableButtons(boolean enable){
        btnMusic.setEnabled(enable);
        btnArt.setEnabled(enable);
        btnMaths.setEnabled(enable);
        btnLanguage.setEnabled(enable);
        btnEnglish.setEnabled(enable);
    }

}