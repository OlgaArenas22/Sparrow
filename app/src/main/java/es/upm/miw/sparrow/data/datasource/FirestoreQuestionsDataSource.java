package es.upm.miw.sparrow.data.datasource;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.miw.sparrow.data.QuestionsRepository.Callback;
import es.upm.miw.sparrow.data.QuestionDTO;
import es.upm.miw.sparrow.data.QuestionsDataSource;
import es.upm.miw.sparrow.domain.Question;

public class FirestoreQuestionsDataSource implements QuestionsDataSource {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String collectionPath;

    public FirestoreQuestionsDataSource(@NonNull String collectionPath) {
        this.collectionPath = collectionPath;
    }

    @Override
    public void fetch(int amount, String ignoredDifficulty, @NonNull Callback callback) {
        db.collection(collectionPath).get()
                .addOnSuccessListener(snap -> {
                    List<Question> out = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        QuestionDTO dto = d.toObject(QuestionDTO.class);
                        if (dto == null) continue;

                        if (dto.question == null || dto.correct == null ||
                                dto.wrong1 == null || dto.wrong2 == null || dto.wrong3 == null) {
                            continue;
                        }

                        out.add(Question.fromDTO(dto));
                    }
                    Collections.shuffle(out);
                    if (out.size() > amount) out = new ArrayList<>(out.subList(0, amount));
                    callback.onSuccess(out);
                })
                .addOnFailureListener(e -> callback.onError(e));
    }
}
