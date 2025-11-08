package es.upm.miw.sparrow.data.remote.english;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.upm.miw.sparrow.data.QuestionDTO;
import es.upm.miw.sparrow.domain.Question;
public final class EnglishMapper {
    private EnglishMapper(){}

    public static Question toQuestion(String english, String correctEs, List<String> wrongs) {
        List<String> safeWrongs = ensureThreeDistractors(correctEs, wrongs);

        QuestionDTO dto = new QuestionDTO();
        dto.question = String.format(Locale.getDefault(),
                "¿Cómo se dice \"%s\" en español?", english);
        dto.correct = correctEs;
        dto.wrong1  = safeWrongs.get(0);
        dto.wrong2  = safeWrongs.get(1);
        dto.wrong3  = safeWrongs.get(2);
        dto.image   = null;

        return Question.fromDTO(dto);
    }

    private static List<String> ensureThreeDistractors(String correctEs, List<String> wrongs) {
        List<String> out = new ArrayList<>();
        if (wrongs != null) {
            for (String w : wrongs) {
                if (w == null) continue;
                String s = w.trim();
                if (s.isEmpty()) continue;
                if (!s.equalsIgnoreCase(correctEs) && !containsIgnoreCase(out, s)) {
                    out.add(s);
                }
                if (out.size() == 3) break;
            }
        }
        while (out.size() < 3) {
            out.add(fallbackDistractor(correctEs, out.size()));
        }
        return out;
    }

    private static boolean containsIgnoreCase(List<String> list, String s) {
        for (String it : list) if (it.equalsIgnoreCase(s)) return true;
        return false;
    }

    private static String fallbackDistractor(String correct, int idx) {
        switch (idx) {
            case 0: return correct + "s";
            case 1: return "la " + correct;
            default:return correct + " pequeña";
        }
    }
}
