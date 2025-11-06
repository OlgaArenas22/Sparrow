package es.upm.miw.sparrow.common;

import androidx.core.text.HtmlCompat;

public final class HtmlUtils {
    private HtmlUtils(){}

    public static String decode(String html) {
        if (html == null) return "";
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
    }
}
