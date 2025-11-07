package es.upm.miw.sparrow.view;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upm.miw.sparrow.R;
import es.upm.miw.sparrow.data.QuestionDTO;
import es.upm.miw.sparrow.data.QuestionsRepository;
import es.upm.miw.sparrow.data.QuestionsRepositoryImpl;
import es.upm.miw.sparrow.data.datasource.FirestoreQuestionsDataSource;
import es.upm.miw.sparrow.domain.Question;

public class LanguageViewModel extends AndroidViewModel {

    public static final int QUESTION_SECONDS = 10;
    public static final long QUESTION_MILLIS = QUESTION_SECONDS * 1000L;
    private static final long HILIGHT_MILLIS = 900L;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int points = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private CountDownTimer timer;
    private long timeLeftInMillis;
    private Integer currentSelectedIndex;
    private long startTime;

    private final MutableLiveData<List<Question>> questions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> currentIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedIndex = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> correctIndex = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> locked = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> finished = new MutableLiveData<>(false);
    private final MutableLiveData<Exception> error = new MutableLiveData<>(null);

    private boolean loaded = false;

    private final QuestionsRepository repo;

    public LanguageViewModel(@NonNull Application application) {
        super(application);
        this.repo = new QuestionsRepositoryImpl(
                java.util.Arrays.asList(
                        new FirestoreQuestionsDataSource("languageQuestions")
                )
        );
    }

    public @Nullable Question getCurrentQuestionSync() {
        List<Question> qs = questions.getValue();
        Integer i = currentIndex.getValue();
        if (qs == null || qs.isEmpty() || i == null || i < 0 || i >= qs.size()) return null;
        return qs.get(i);
    }

    public int getPoints() { return points; }
    public int getTotalQuestions() {
        List<Question> qs = questions.getValue();
        return qs != null ? qs.size() : 0;
    }

    public void loadQuestionsIfNeeded() {
        if (loaded) return;
        loaded = true;

        db.collection("languageQuestions")
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
        timeLeftInMillis = QUESTION_MILLIS;
        startTime = System.currentTimeMillis();
        correctIndex.setValue(q.correctIndex);

        cancelTimer();
        timer = new CountDownTimer(QUESTION_MILLIS, 1_000) {
            public void onTick(long ms) {
            }
            public void onFinish() {
                locked.setValue(true);
                selectedIndex.setValue(null);
                handler.postDelayed(LanguageViewModel.this::nextQuestion, HILIGHT_MILLIS);
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

        if (idx == q.correctIndex) points++;

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
            saveResult();
            return;
        }
        currentIndex.setValue(next);
        startQuestion();
    }

    private void saveResult() {
        Context context = getApplication().getApplicationContext();

        int numPoints = points;
        int totalQuestions = getTotalQuestions();

        String userEmail;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && user.getEmail() != null) {
            userEmail = user.getEmail();
        } else {
            SharedPreferences prefs = context.getSharedPreferences(
                    context.getString(R.string.prefs_file),
                    Context.MODE_PRIVATE
            );
            userEmail = prefs.getString("email", "no-auth-user@guest.com");
        }

        final String finalUserEmail = userEmail;

        Map<String, Object> matchData = new HashMap<>();
        matchData.put("email", finalUserEmail);
        matchData.put("numPoints", numPoints);
        matchData.put("totalPoints", totalQuestions);
        matchData.put("category", "Lengua");
        matchData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("matches").document()
                .set(matchData)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Última partida guardadq en matches/" + finalUserEmail))
                .addOnFailureListener(e -> Log.e("Firebase", "Error al guardar el match", e));
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void pauseTimer(){
        if(timer != null){
            long timeElapsed = System.currentTimeMillis() - startTime;
            timeLeftInMillis = timeLeftInMillis - timeElapsed;
        }
        currentSelectedIndex = selectedIndex.getValue();
        cancelTimer();
    }

    public void resumeTimer(){
        cancelTimer();

        startTime = System.currentTimeMillis();
        selectedIndex.setValue(currentSelectedIndex);

        long millisToStart = (timeLeftInMillis > 0 && timeLeftInMillis < QUESTION_MILLIS)
                ? timeLeftInMillis
                : QUESTION_MILLIS;
        timer = new CountDownTimer(millisToStart, 1_000) {
            public void onTick(long ms) {
            }
            public void onFinish() {
                locked.setValue(true);
                selectedIndex.setValue(null);
                handler.postDelayed(LanguageViewModel.this::nextQuestion, timeLeftInMillis);
            }
        }.start();
    }

    @Override protected void onCleared() {
        super.onCleared();
        cancelTimer();
        handler.removeCallbacksAndMessages(null);
    }

    // Getters LiveData
    public LiveData<List<Question>> getQuestions() { return questions; }
    public LiveData<Integer> getCurrentIndex() { return currentIndex; }
    public LiveData<Integer> getSelectedIndex() { return selectedIndex; }
    public LiveData<Integer> getCorrectIndex() { return correctIndex; }
    public LiveData<Boolean> getLocked() { return locked; }
    public LiveData<Boolean> getFinished() { return finished; }
    public LiveData<Exception> getError() { return error; }
    public long getTimeLeftInMillis(){return timeLeftInMillis;}
}
