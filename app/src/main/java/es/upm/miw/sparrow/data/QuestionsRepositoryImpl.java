package es.upm.miw.sparrow.data;

import androidx.annotation.Nullable;
import java.util.List;
import es.upm.miw.sparrow.domain.Question;

public class QuestionsRepositoryImpl implements QuestionsRepository {

    private final List<QuestionsDataSource> sources;

    public QuestionsRepositoryImpl(List<QuestionsDataSource> sources) {
        this.sources = sources;
    }

    @Override
    public void fetch(int amount, @Nullable String difficulty, Callback cb) {
        fetchFrom(0, amount, difficulty, cb);
    }

    private void fetchFrom(int idx, int amount, @Nullable String difficulty, Callback cb) {
        if (idx >= sources.size()) {
            cb.onError(new IllegalStateException("Sin data sources válidos"));
            return;
        }
        sources.get(idx).fetch(amount, difficulty, new Callback() {
            @Override public void onSuccess(java.util.List<Question> questions) {
                cb.onSuccess(questions);
            }
            @Override public void onError(Throwable t) {
                fetchFrom(idx + 1, amount, difficulty, cb);
            }
        });
    }
}
