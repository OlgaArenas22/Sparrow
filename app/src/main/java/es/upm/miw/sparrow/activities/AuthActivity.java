package es.upm.miw.sparrow.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

import es.upm.miw.sparrow.ProviderType;
import es.upm.miw.sparrow.R;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.email), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        analyticsEvent();
        setupFirebase();
    }

    private void analyticsEvent(){
        var analytics = FirebaseAnalytics.getInstance(this);
        var bundle = new Bundle();
        bundle.putString("message", "Integración de Firebase completa");
        analytics.logEvent("InitScreen", bundle);
    }

    private void setupFirebase(){
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        Button btnGoogle = findViewById(R.id.googleLogin);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);

        btnSignUp.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                if(!email.getText().isEmpty() && !password.getText().isEmpty()){
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                moveHome();

                            }else{
                                showAlert();
                            }
                        }
                    });
                }else showAlert();
            }
        });

        btnLogin.setOnClickListener(v->{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                if(!email.getText().isEmpty() && !password.getText().isEmpty()){
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                moveHome();
                            }else{
                                showAlert();
                            }
                        }
                    });
                }else showAlert();
            }
        });
        btnGoogle.setOnClickListener(v -> {
            GoogleSignInOptions googleConf = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient googleClient = GoogleSignIn.getClient(this, googleConf);
            Intent signInIntent = googleClient.getSignInIntent();
            startActivityForResult(signInIntent, 100);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account != null) {
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                    FirebaseAuth.getInstance()
                            .signInWithCredential(credential)
                            .addOnCompleteListener(this, t -> {
                                if (t.isSuccessful()) {
                                    moveHome();
                                } else {
                                    showAlert();
                                }
                            });
                }

            } catch (ApiException e) {
                showAlert();
            }
        }
    }

    private void showAlert(){
        var builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Error de autenticación");
        builder.setPositiveButton("Aceptar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void moveHome(){
        Intent home = new Intent(this, HomeActivity.class);
        startActivity(home);
    }
}