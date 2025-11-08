package es.upm.miw.sparrow.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import es.upm.miw.sparrow.R;
import es.upm.miw.sparrow.data.local.AvatarUrlBuilder;
import es.upm.miw.sparrow.data.users.UsersRepository;

public class AuthActivity extends AppCompatActivity {
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.title), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);

        analyticsEvent();
        session();
        setupFirebase();
    }

    @Override
    public void onStart(){
        super.onStart();
        View authLayout = findViewById(R.id.main);
        authLayout.setVisibility(View.VISIBLE);
    }

    private void analyticsEvent(){
        var analytics = FirebaseAnalytics.getInstance(this);
        var bundle = new Bundle();
        bundle.putString("message", "Integración de Firebase completa");
        analytics.logEvent("InitScreen", bundle);
    }

    private void session(){
        String email = prefs.getString("email", null);
        if(email != null){
            View authLayout = findViewById(R.id.main);
            authLayout.setVisibility(View.INVISIBLE);
            moveHome(email);
        }
    }

    private void setupFirebase(){
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        Button btnGoogle = findViewById(R.id.googleLogin);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);

        btnSignUp.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                if(!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty()){
                    FirebaseAuth.getInstance()
                            .createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        createFirebaseUser();
                                        moveHome(email.getText().toString());
                                    }else{
                                        showAlert("Error: datos inválidos", "Recuerde que la contraseña tiene que tener mínimo 6 caracteres, 1 mayúscula, 1 minúscula y un caracter especial");
                                    }
                                }
                            });
                }else showAlert("Error: datos inválidos", "Recuerde que la contraseña tiene que tener mínimo 6 caracteres, 1 mayúscula, 1 minúscula y un caracter especial");
            }
        });

        btnLogin.setOnClickListener(v->{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                if(!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty()){
                    FirebaseAuth.getInstance()
                            .signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        moveHome(email.getText().toString());
                                    }else{
                                        showAlert("Error", "No se ha podido iniciar sesión");
                                    }
                                }
                            });
                }else showAlert("Error", "El correo o la contraseña no son válidos");
            }
        });

        btnGoogle.setOnClickListener(v -> {
            GoogleSignInOptions googleConf = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient googleClient = GoogleSignIn.getClient(this, googleConf);
            googleClient.signOut();
            startActivityForResult(googleClient.getSignInIntent(), 100);
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
                                    createFirebaseUserIfMissing();
                                    moveHome(account.getEmail());
                                } else {
                                    showAlert("Error", "Error al crear usuario");
                                }
                            });
                }

            } catch (ApiException e) {
                showAlert("Error", "Error al iniciar sesión con Google");
            }
        }
    }

    private void showAlert(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Aceptar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void moveHome(String email){
        Intent home = new Intent(this, HomeActivity.class);
        home.putExtra("email",email);
        startActivity(home);
    }
    public void createFirebaseUser(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            String email = user.getEmail();
            String defaultAvatar = AvatarUrlBuilder.buildAutoBg(uid, 256);

            UsersRepository usersRepo = new UsersRepository();
            usersRepo.createOrMergeUserByUid(uid, email, defaultAvatar, new UsersRepository.CompletionListener() {
                @Override public void onSuccess() {}
                @Override public void onError(@NonNull Exception e) { }
            });
        }
    }
    private void createFirebaseUserIfMissing(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(snap -> {
                    if (snap.exists()) {
                        return;
                    }
                    createFirebaseUser();
                })
                .addOnFailureListener(err -> {
                    showAlert("Error", "Error al iniciar sesión con Google");
                });
    }
}
