package es.upm.miw.sparrow.data.datasource;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import es.upm.miw.sparrow.R;
import es.upm.miw.sparrow.domain.Match;


public class MatchesDataSource {

    private static final String COLLECTION = "matches";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SharedPreferences prefs;

    public interface Callback {
        void onSuccess(List<Match> matches);
        void onError(Throwable t);
    }

    public MatchesDataSource(@NonNull Context context) {
        this.prefs = context.getSharedPreferences(context.getString(R.string.prefs_file), Context.MODE_PRIVATE);
    }

    public void fetchMyMatches(@NonNull Callback callback) {
        String email = prefs.getString("email", null);
        if (email == null || email.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        db.collection(COLLECTION)
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Match> list = new ArrayList<>();
                    for (DocumentSnapshot d : snapshot) {
                        String id = d.getId();
                        String category = d.getString("category");
                        String e = d.getString("email");
                        Long np = d.getLong("numPoints");
                        Long tp = d.getLong("totalPoints");
                        Timestamp ts = d.getTimestamp("timestamp");
                        list.add(new Match(
                                id,
                                category == null ? "" : category,
                                e == null ? "" : e,
                                np == null ? 0 : np.intValue(),
                                tp == null ? 0 : tp.intValue(),
                                ts == null ? Timestamp.now() : ts
                        ));
                    }
                    java.util.Collections.sort(list, (a, b) ->
                            b.timestamp.toDate().compareTo(a.timestamp.toDate()));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("MatchesDataSource", "Firestore error", e);
                    callback.onError(e);
                });
    }
}
