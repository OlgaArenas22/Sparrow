package es.upm.miw.sparrow.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import es.upm.miw.sparrow.R;
import es.upm.miw.sparrow.data.local.AvatarUrlBuilder;
import es.upm.miw.sparrow.data.users.UsersRepository;
import es.upm.miw.sparrow.ui.fragments.sheet.AvatarPickerBottomSheet;

public class EditProfileFragment extends Fragment {

    private static final String ARG_EMAIL = "arg_email";
    public static final String FR_KEY = "avatar_pick_key";
    public static final String FR_BUNDLE_SEED = "seed";

    private String email;
    private ImageButton btnAvatar, btnReturn;
    private ImageView ivAvatar;
    private TextView tvEmail;

    private UsersRepository usersRepo;
    private ListenerRegistration selfReg;

    public static EditProfileFragment newInstance(String email) {
        EditProfileFragment f = new EditProfileFragment();
        Bundle b = new Bundle();
        b.putString(ARG_EMAIL, email);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usersRepo = new UsersRepository();

        if (getArguments() != null) {
            email = getArguments().getString(ARG_EMAIL);
        }
        if (email == null) {
            FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
            if (fu != null) email = fu.getEmail();
        }
        if (email == null) {
            SharedPreferences sp = requireContext()
                    .getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
            email = sp.getString("email", null);
        }

        getParentFragmentManager().setFragmentResultListener(FR_KEY, this, (requestKey, result) -> {
            String seed = result.getString(FR_BUNDLE_SEED);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && seed != null) {
                String uid = user.getUid();
                String newUrl = AvatarUrlBuilder.buildAutoBg(seed, 256);

                usersRepo.updateAvatarByUid(uid, newUrl, new UsersRepository.CompletionListener() {
                    @Override public void onSuccess() {
                        refreshOnce(uid);
                        refreshNavHeaderInActivity();
                    }
                    @Override public void onError(@NonNull Exception e) {
                        // opcional: Toast/log
                    }
                });
            }
        });
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        tvEmail = v.findViewById(R.id.tvEmail);
        btnAvatar = v.findViewById(R.id.btnAvatar);
        ivAvatar = v.findViewById(R.id.ivAvatar);
        btnReturn = v.findViewById(R.id.btnReturn);

        tvEmail.setText(email != null ? email : getString(R.string.emailHint));

        btnAvatar.setOnClickListener(view ->
                new AvatarPickerBottomSheet().show(getParentFragmentManager(), "avatar_picker")
        );

        btnReturn.setOnClickListener(view ->
                getParentFragmentManager().popBackStack()
        );

        startSelfObserver();
    }

    @Override
    public void onResume() {
        super.onResume();
        startSelfObserver();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (selfReg != null) {
            selfReg.remove();
            selfReg = null;
        }
    }

    private void startSelfObserver() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || ivAvatar == null) return;

        String uid = user.getUid();

        if (selfReg != null) selfReg.remove();
        selfReg = usersRepo.observeUserByUid(uid, new UsersRepository.UserListener() {
            @Override
            public void onUser(String emailFromDb, String avatarUrl) {
                String url = (avatarUrl != null && !avatarUrl.isEmpty())
                        ? avatarUrl
                        : AvatarUrlBuilder.buildAutoBg(uid, 256);

                Glide.with(EditProfileFragment.this)
                        .load(url)
                        .placeholder(R.drawable.ic_sparrow_rounded)
                        .into(ivAvatar);
            }

            @Override
            public void onError(@NonNull Exception e) {
                String fallback = AvatarUrlBuilder.buildAutoBg(uid, 256);
                Glide.with(EditProfileFragment.this)
                        .load(fallback)
                        .placeholder(R.drawable.ic_sparrow_rounded)
                        .into(ivAvatar);
            }
        });
    }

    private void refreshOnce(String uid) {
        usersRepo.getUserOnceByUid(uid, new UsersRepository.UserListener() {
            @Override
            public void onUser(String emailFromDb, String avatarUrl) {
                String url = (avatarUrl != null && !avatarUrl.isEmpty())
                        ? avatarUrl
                        : AvatarUrlBuilder.buildAutoBg(uid, 256);

                Glide.with(EditProfileFragment.this)
                        .load(url)
                        .placeholder(R.drawable.ic_sparrow_rounded)
                        .into(ivAvatar);
            }

            @Override
            public void onError(@NonNull Exception e) {
                String fallback = AvatarUrlBuilder.buildAutoBg(uid, 256);
                Glide.with(EditProfileFragment.this)
                        .load(fallback)
                        .placeholder(R.drawable.ic_sparrow_rounded)
                        .into(ivAvatar);
            }
        });
    }

    private void refreshNavHeaderInActivity() {
        if (getActivity() instanceof es.upm.miw.sparrow.ui.activities.HomeActivity) {
            ((es.upm.miw.sparrow.ui.activities.HomeActivity) getActivity())
                    .refreshHeaderAvatar();
        }
    }
}
