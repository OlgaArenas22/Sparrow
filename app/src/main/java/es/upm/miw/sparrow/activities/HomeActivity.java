package es.upm.miw.sparrow.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

import es.upm.miw.sparrow.R;

public class HomeActivity extends AppCompatActivity {

    private SharedPreferences.Editor prefs;
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
        prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit();

        Bundle bundle = getIntent().getExtras();
        String email = bundle.getString("email", null);
        setup(email);
        dataSaving(email);
    }

    public void setup(String email){
        Button btnLogOut = findViewById(R.id.logOut);
        EditText emailText = findViewById(R.id.email);
        emailText.setText(email);

        btnLogOut.setOnClickListener(v -> {
            dataClear();
            FirebaseAuth.getInstance().signOut();
            onBackPressed();
        });
    }

    public void dataSaving(String email){
        prefs.putString("email", email);
        prefs.apply();
    }

    public void dataClear(){
        prefs.clear();
        prefs.apply();
    }
}