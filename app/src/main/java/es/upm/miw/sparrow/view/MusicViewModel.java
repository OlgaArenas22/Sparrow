package es.upm.miw.sparrow.view;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.miw.sparrow.data.QuestionDTO;
import es.upm.miw.sparrow.domain.Question;

public class MusicViewModel extends ViewModel {

    public static final int QUESTION_SECONDS = 10;
    public static final long QUESTION_MILLIS = QUESTION_SECONDS * 1000L;

    private static final long HILIGHT_MILLIS = 900L;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private CountDownTimer timer;

    private final MutableLiveData<List<Question>> questions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> currentIndex = new MutableLiveData<>(0);

    private final MutableLiveData<Integer> selectedIndex = new MutableLiveData<>(null); // puede ser null
    private final MutableLiveData<Integer> correctIndex = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> locked = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> finished = new MutableLiveData<>(false);

    private final MutableLiveData<Exception> error = new MutableLiveData<>(null);

    private boolean loaded = false;

    public int getQuestionSeconds() { return QUESTION_SECONDS; }
    public long getQuestionMillis() { return QUESTION_MILLIS; }

    public @Nullable Question getCurrentQuestionSync() {
        List<Question> qs = questions.getValue();
        Integer i = currentIndex.getValue();
        if (qs == null || qs.isEmpty() || i == null || i < 0 || i >= qs.size()) return null;
        return qs.get(i);
    }

    public void loadQuestionsIfNeeded() {
        if (loaded) return;
        loaded = true;

        db.collection("musicQuestions")
                .get()
                .addOnSuccessListener(snap -> {
                    List<Question> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        QuestionDTO dto = doc.toObject(QuestionDTO.class);
                        if (dto != null &&
                                dto.question != null &&
                                dto.correct != null &&
                                dto.wrong1 != null &&
                                dto.wrong2 != null &&
                                dto.wrong3 != null) {
                            list.add(Question.fromDTO(dto));
                        }
                    }
                    if (list.isEmpty()) {
                        error.setValue(new IllegalStateException("No se encontraron preguntas en la base de datos."));
                        return;
                    }
                    Collections.shuffle(list);
                    if (list.size() > 5) {
                        list = new ArrayList<>(list.subList(0, 5));
                    }
                    questions.setValue(list);
                    currentIndex.setValue(0);
                    startQuestion();
                })
                .addOnFailureListener(error::setValue);
    }

    private void startQuestion() {
        locked.setValue(false);
        selectedIndex.setValue(null);

        Question q = getCurrentQuestionSync();
        if (q == null) {
            finished.setValue(true);
            return;
        }

        correctIndex.setValue(q.correctIndex);

        cancelTimer();
        timer = new CountDownTimer(QUESTION_MILLIS, 1_000) {
            public void onTick(long ms) {
            }
            public void onFinish() {
                locked.setValue(true);
                selectedIndex.setValue(null);
                handler.postDelayed(MusicViewModel.this::nextQuestion, HILIGHT_MILLIS);
            }
        }.start();
    }

    public void answer(int idx) {
        Boolean isLocked = locked.getValue();
        if (isLocked != null && isLocked) return;

        locked.setValue(true);
        cancelTimer();

        Question q = getCurrentQuestionSync();
        if (q == null) return;

        selectedIndex.setValue(idx);
        handler.postDelayed(this::nextQuestion, HILIGHT_MILLIS);
    }

    private void nextQuestion() {
        List<Question> qs = questions.getValue();
        Integer i = currentIndex.getValue();
        if (qs == null || i == null) return;

        int next = i + 1;
        if (next >= qs.size()) {
            finished.setValue(true);
            cancelTimer();
            return;
        }
        currentIndex.setValue(next);
        startQuestion();
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelTimer();
        handler.removeCallbacksAndMessages(null);
    }

    //region Getters
    public LiveData<List<Question>> getQuestions()      { return questions; }
    public LiveData<Integer> getCurrentIndex()          { return currentIndex; }
    public LiveData<Integer> getSelectedIndex()         { return selectedIndex; }
    public LiveData<Integer> getCorrectIndex()          { return correctIndex; }
    public LiveData<Boolean> getLocked()                { return locked; }
    public LiveData<Boolean> getFinished()              { return finished; }
    public LiveData<Exception> getError()               { return error; }
    //endregion
}
