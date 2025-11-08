package es.upm.miw.sparrow.data.users;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public final class UsersRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface UserListener {
        void onUser(@Nullable String email, @Nullable String avatarUrl);
        void onError(@NonNull Exception e);
    }

    public interface CompletionListener {
        void onSuccess();
        void onError(@NonNull Exception e);
    }

    /** Observa el documento del usuario por UID en tiempo real. */
    public ListenerRegistration observeUserByUid(@NonNull String uid,
                                                 @NonNull UserListener listener) {
        DocumentReference ref = db.collection("users").document(uid);
        return ref.addSnapshotListener((snap, err) -> {
            if (err != null) {
                listener.onError(err);
                return;
            }
            if (snap != null && snap.exists()) {
                listener.onUser(snap.getString("email"), snap.getString("avatarUrl"));
            } else {
                listener.onUser(null, null);
            }
        });
    }

    /** Lee una vez el usuario por UID. */
    public void getUserOnceByUid(@NonNull String uid,
                                 @NonNull UserListener listener) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(snap -> {
                    if (snap.exists()) {
                        listener.onUser(snap.getString("email"), snap.getString("avatarUrl"));
                    } else {
                        listener.onUser(null, null);
                    }
                })
                .addOnFailureListener(listener::onError);
    }

    /** Crea (o actualiza) el documento users/{uid} con email y avatar por defecto. */
    public void createOrMergeUserByUid(@NonNull String uid,
                                       @NonNull String email,
                                       @NonNull String defaultAvatarUrl,
                                       @NonNull CompletionListener cb) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("email", email);
        doc.put("avatarUrl", defaultAvatarUrl);
        doc.put("createdAt", FieldValue.serverTimestamp());
        doc.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("users").document(uid)
                .set(doc, SetOptions.merge())
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    /** Actualiza el avatar del usuario por UID. */
    public void updateAvatarByUid(@NonNull String uid,
                                  @NonNull String newUrl,
                                  @NonNull CompletionListener cb) {
        Map<String, Object> up = new HashMap<>();
        up.put("avatarUrl", newUrl);
        up.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("users").document(uid)
                .update(up)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }
}
