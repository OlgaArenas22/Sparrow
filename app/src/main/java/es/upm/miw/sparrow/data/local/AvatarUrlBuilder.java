package es.upm.miw.sparrow.data.local;

import android.net.Uri;
import android.text.TextUtils;
public final class AvatarUrlBuilder {
    private static final String BASE = "https://api.dicebear.com/9.x";
    private static final String STYLE = "fun-emoji";

    private static final String[] PALETTE = new String[]{
            "FFC107", //soft_amber
            "ABF650", //soft_green
            "77D2FB", //light_blue
            "FF4A89", //pink
            "E46FF8", //purple
            "FADB3B", //soft_yellow
            "009688", //teal
            "FB683F", //light_red
    };

    private AvatarUrlBuilder() {}
    public static String buildAutoBg(String seed, int size) {
        String bg = pickBgFromSeed(seed);
        return build(seed, size, bg);
    }
    public static String build(String seed, int size, String bgHexNoHash) {
        if (TextUtils.isEmpty(seed)) seed = "SparrowDefault";
        if (size > 256) size = 256; // PNG <= 256
        Uri.Builder b = Uri.parse(BASE + "/" + STYLE + "/png")
                .buildUpon()
                .appendQueryParameter("seed", seed)
                .appendQueryParameter("size", String.valueOf(size));
        if (!TextUtils.isEmpty(bgHexNoHash)) {
            b.appendQueryParameter("backgroundColor", bgHexNoHash);
        }
        return b.build().toString();
    }

    private static String pickBgFromSeed(String seed) {
        if (TextUtils.isEmpty(seed)) return PALETTE[0];
        int idx = Math.abs(seed.hashCode()) % PALETTE.length;
        return PALETTE[idx];
    }
}
