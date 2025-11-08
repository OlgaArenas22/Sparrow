package es.upm.miw.sparrow.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import es.upm.miw.sparrow.R;
import es.upm.miw.sparrow.ui.audio.MusicManager;

public class SettingsFragment extends Fragment {

    private SwitchMaterial swMusic;
    private Slider sliderVolume;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        swMusic = v.findViewById(R.id.swMusic);
        sliderVolume = v.findViewById(R.id.sliderVolume);

        final MusicManager mm = MusicManager.get(requireContext());

        // Estado inicial desde preferencias
        boolean enabled = mm.isEnabled();
        swMusic.setChecked(enabled);
        if (sliderVolume != null) {
            sliderVolume.setValue(mm.getVolume() * 100f); // 0..100
            sliderVolume.setEnabled(enabled);
        }
        // Estado inicial
        sliderVolume.setEnabled(swMusic.isChecked());

        // Reacciona a cambios del switch
        swMusic.setOnCheckedChangeListener((btn, isChecked) -> {
            sliderVolume.setEnabled(isChecked);   // <-- clave para que cambien los colores por estado
        });

        // Cambiar ON/OFF música
        swMusic.setOnCheckedChangeListener((btn, isChecked) -> {
            mm.setEnabled(isChecked);
            if (!isChecked) {
                mm.stopAndRelease();
            }
            if (sliderVolume != null) sliderVolume.setEnabled(isChecked);
        });

        // Cambiar volumen en tiempo real
        if (sliderVolume != null) {
            sliderVolume.addOnChangeListener((slider, value, fromUser) -> {
                float vol01 = value / 100f;   // convertir 0..100 a 0..1
                mm.setVolume(vol01);
            });
        }
    }
}
