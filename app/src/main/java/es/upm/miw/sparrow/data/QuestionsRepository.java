package es.upm.miw.sparrow.data;

import androidx.annotation.Nullable;
import java.util.List;
import es.upm.miw.sparrow.domain.Question;

public interface QuestionsRepository {
    void fetch(int amount, @Nullable String difficulty, Callback cb);

    interface Callback {
        void onSuccess(List<Question> questions);
        void onError(Throwable t);
    }
}
