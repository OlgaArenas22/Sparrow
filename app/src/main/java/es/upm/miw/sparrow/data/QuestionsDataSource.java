package es.upm.miw.sparrow.data;

import androidx.annotation.Nullable;

public interface QuestionsDataSource {
    void fetch(int amount, @Nullable String difficulty, QuestionsRepository.Callback cb);
}
