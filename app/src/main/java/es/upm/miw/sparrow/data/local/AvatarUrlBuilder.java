package es.upm.miw.sparrow.data.local;

import android.net.Uri;
import android.text.TextUtils;

public final class AvatarUrlBuilder {
    private static final String BASE = "https://api.dicebear.com/9.x";
    private static final String STYLE = "fun-emoji";

    // Paleta (sin #). Cambia/añade los que quieras.
    private static final String[] PALETTE = new String[]{
            "FFE082", // amber suave
            "AED581", // green suave
            "81D4FA", // light blue
            "FFAB91", // light orange
            "CE93D8", // purple
            "80CBC4", // teal
            "FFF59D", // yellow suave
            "F8BBD0"  // pink
    };

    private AvatarUrlBuilder() {}

    /** Usa color de fondo elegido automáticamente a partir de la seed */
    public static String buildAutoBg(String seed, int size) {
        String bg = pickBgFromSeed(seed);
        return build(seed, size, bg);
    }

    /** Construye URL con fondo especificado (hex sin #) */
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
