package es.upm.miw.sparrow.ui.audio;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;

import androidx.annotation.RawRes;

import es.upm.miw.sparrow.R;

/**
 * Gestor único de música de fondo para los quizzes.
 * - Respeta la preferencia 'music_enabled'
 * - Guarda/aplica volumen 'music_volume' (0..1)
 * - Gestiona AudioFocus
 */
public final class MusicManager {

    public static final String PREF_KEY_MUSIC_ENABLED = "music_enabled";
    public static final String PREF_KEY_MUSIC_VOLUME  = "music_volume";  // float 0..1

    private static MusicManager INSTANCE;

    private final Context appCtx;
    private final AudioManager audioManager;
    private final AudioAttributes audioAttrs;
    private final AudioFocusRequest focusRequest;

    private MediaPlayer player;
    private @RawRes int currentRes = 0;

    private MusicManager(Context ctx) {
        this.appCtx = ctx.getApplicationContext();
        this.audioManager = (AudioManager) appCtx.getSystemService(Context.AUDIO_SERVICE);

        audioAttrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttrs)
                .setOnAudioFocusChangeListener(focus -> {
                    if (focus == AudioManager.AUDIOFOCUS_LOSS
                            || focus == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        pause();
                    }
                })
                .build();
    }

    public static synchronized MusicManager get(Context ctx) {
        if (INSTANCE == null) INSTANCE = new MusicManager(ctx);
        return INSTANCE;
    }

    private SharedPreferences prefs() {
        return appCtx.getSharedPreferences(appCtx.getString(R.string.prefs_file), Context.MODE_PRIVATE);
    }

    // ---------- Preferencias ----------
    public boolean isEnabled() {
        return prefs().getBoolean(PREF_KEY_MUSIC_ENABLED, true); // por defecto ON
    }

    public void setEnabled(boolean enabled) {
        prefs().edit().putBoolean(PREF_KEY_MUSIC_ENABLED, enabled).apply();
        if (!enabled) stopAndRelease();
    }

    /** Volumen en rango [0..1] */
    public float getVolume() {
        return prefs().getFloat(PREF_KEY_MUSIC_VOLUME, 0.25f);
    }

    /** Guarda volumen (0..1) y lo aplica si hay música sonando. */
    public void setVolume(float vol01) {
        float v = Math.max(0f, Math.min(1f, vol01));
        prefs().edit().putFloat(PREF_KEY_MUSIC_VOLUME, v).apply();
        if (player != null) player.setVolume(v, v);
    }

    // ---------- Reproducción ----------
    /** Arranca música (en loop) si está habilitada. Idempotente. */
    public void startIfEnabled(@RawRes int resId) {
        if (!isEnabled()) return;

        // Si ya está sonando esa misma pista, no hagas nada
        if (player != null && currentRes == resId && player.isPlaying()) return;

        // Prepara de cero
        stopAndRelease();

        player = MediaPlayer.create(appCtx, resId);
        if (player == null) return;

        player.setAudioAttributes(audioAttrs);
        player.setLooping(true);

        // ⬇️ AQUI "modificamos el inicio del player": aplicamos el volumen guardado
        float vol = getVolume();
        player.setVolume(vol, vol);

        currentRes = resId;

        int result = audioManager.requestAudioFocus(focusRequest);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            player.start();
        }
    }

    public void pause() {
        if (player != null && player.isPlaying()) {
            player.pause();
            audioManager.abandonAudioFocusRequest(focusRequest);
        }
    }

    public void stopAndRelease() {
        if (player != null) {
            try { player.stop(); } catch (Exception ignored) {}
            player.release();
            player = null;
            currentRes = 0;
        }
        audioManager.abandonAudioFocusRequest(focusRequest);
    }
}
