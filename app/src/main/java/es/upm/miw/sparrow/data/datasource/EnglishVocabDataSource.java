package es.upm.miw.sparrow.data.datasource;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.upm.miw.sparrow.data.QuestionsDataSource;
import es.upm.miw.sparrow.data.QuestionsRepository;
import es.upm.miw.sparrow.data.remote.RetrofitClient;
import es.upm.miw.sparrow.data.remote.english.EnglishMapper;
import es.upm.miw.sparrow.data.remote.mymemory.MyMemoryApi;
import es.upm.miw.sparrow.data.remote.mymemory.MyMemoryResponse;
import es.upm.miw.sparrow.data.remote.randomword.RandomWordApi;
import es.upm.miw.sparrow.data.remote.rae.RaeApi;
import es.upm.miw.sparrow.data.remote.rae.RaeRandomResponse;
import es.upm.miw.sparrow.domain.Question;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnglishVocabDataSource implements QuestionsDataSource {

    private final RandomWordApi randomWord = RetrofitClient.randomWord();
    private final MyMemoryApi myMemory = RetrofitClient.myMemory();
    private final RaeApi rae = RetrofitClient.rae();

    @Override
    public void fetch(int amount, @Nullable String difficulty, QuestionsRepository.Callback cb) {
        randomWord.getWords(amount <= 0 ? 1 : amount).enqueue(new Callback<List<String>>() {
            @Override public void onResponse(Call<List<String>> call, Response<List<String>> resp) {
                if (!resp.isSuccessful() || resp.body() == null || resp.body().isEmpty()) {
                    cb.onError(new RuntimeException("RandomWord API error: " + resp.code()));
                    return;
                }
                buildQuestionsSequentially(resp.body(), 0, new ArrayList<>(), cb);
            }
            @Override public void onFailure(Call<List<String>> call, Throwable t) { cb.onError(t); }
        });
    }

    private void buildQuestionsSequentially(List<String> words, int idx,
                                            List<Question> acc, QuestionsRepository.Callback cb) {
        if (idx >= words.size()) { cb.onSuccess(acc); return; }

        String english = sanitize(words.get(idx));
        if (english.isEmpty() || !isCleanEnglishToken(english)) {
            buildQuestionsSequentially(words, idx + 1, acc, cb);
            return;
        }

        myMemory.translate(english, "en|es").enqueue(new Callback<MyMemoryResponse>() {
            @Override public void onResponse(Call<MyMemoryResponse> call, Response<MyMemoryResponse> resp) {
                if (!resp.isSuccessful() || resp.body()==null || resp.body().responseData==null) {
                    buildQuestionsSequentially(words, idx + 1, acc, cb);
                    return;
                }
                String correctEs = normalizeEs(resp.body().responseData.translatedText);
                if (!looksSpanishWord(correctEs)) {
                    buildQuestionsSequentially(words, idx + 1, acc, cb);
                    return;
                }

                fetchRaeDistractors(correctEs, 3, /*maxAttempts*/ 12,
                        new HashSet<>(), new ArrayList<>(), esList -> {
                            Question q = EnglishMapper.toQuestion(english, correctEs, esList);
                            acc.add(q);
                            buildQuestionsSequentially(words, idx + 1, acc, cb);
                        });
            }
            @Override public void onFailure(Call<MyMemoryResponse> call, Throwable t) {
                buildQuestionsSequentially(words, idx + 1, acc, cb);
            }
        });
    }

    // ======= Distractores via RAE =========

    private interface DistractorsReady { void onDone(List<String> esWords); }

    private void fetchRaeDistractors(String correctEs, int needed, int maxAttempts,
                                     Set<String> seenLower, List<String> acc, DistractorsReady done) {
        if (acc.size() >= needed) { done.onDone(acc); return; }
        if (maxAttempts <= 0) { done.onDone(acc); return; }

        rae.randomWord().enqueue(new Callback<RaeRandomResponse>() {
            @Override public void onResponse(Call<RaeRandomResponse> call, Response<RaeRandomResponse> resp) {
                if (resp.isSuccessful() && resp.body()!=null && resp.body().ok && resp.body().data!=null) {
                    String cand = sanitize(resp.body().data.word);
                    if (looksSpanishWord(cand)
                            && !cand.equalsIgnoreCase(correctEs)
                            && seenLower.add(cand.toLowerCase())) {
                        acc.add(cand);
                    }
                }
                fetchRaeDistractors(correctEs, needed, maxAttempts - 1, seenLower, acc, done);
            }

            @Override public void onFailure(Call<RaeRandomResponse> call, Throwable t) {
                fetchRaeDistractors(correctEs, needed, maxAttempts - 1, seenLower, acc, done);
            }
        });
    }

    // ======= Utilidades =========

    private static String sanitize(String s) {
        if (s == null) return "";
        return s.trim()
                .replaceAll("[\\n\\r\\t]+"," ")
                .replaceAll("^\"|\"$","")
                .replace("\u2014","-")
                .replace("\u00A0"," ");
    }

    private static boolean isCleanEnglishToken(String s) {
        return s != null && s.matches("(?i)[a-z]{2,20}");
    }

    private static final String SP_CHARS = "A-Za-zÁÉÍÓÚÜÑáéíóúüñ";
    private static boolean looksSpanishWord(String s) {
        return s != null && s.matches("(?i)[" + SP_CHARS + "]{2,24}");
    }

    private static String normalizeEs(String s) {
        if (s == null) return "";
        String dec = s.replace("&quot;","\"").replace("&#39;","'");
        dec = dec.replaceAll("\\(.*?\\)", "");
        dec = dec.split("[,;/]")[0];
        return sanitize(dec);
    }
}
