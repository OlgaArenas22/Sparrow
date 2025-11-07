package es.upm.miw.sparrow.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class AvatarPrefs {
    private static final String PREFS = "avatar_prefs";
    private static final String KEY_PREFIX = "avatar_seed_";

    private AvatarPrefs() {}

    private static String emailKey(String email) {
        if (TextUtils.isEmpty(email)) return KEY_PREFIX + "guest";
        return KEY_PREFIX + sha1(email);
    }

    private static String sha1(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] b = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(s.hashCode());
        }
    }

    public static void saveSeed(Context ctx, String email, String seed) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(emailKey(email), seed).apply();
    }

    public static String getSeed(Context ctx, String email) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getString(emailKey(email), null);
    }
}
