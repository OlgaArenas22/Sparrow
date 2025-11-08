package es.upm.miw.sparrow.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import es.upm.miw.sparrow.R;
import es.upm.miw.sparrow.ui.adapters.MatchAdapter;
import es.upm.miw.sparrow.view.MatchesViewModel;

public class MatchesFragment extends Fragment {

    private MatchesViewModel vm;
    private RecyclerView rv;
    private ProgressBar progress;
    private MatchAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_matches, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        progress = v.findViewById(R.id.loading_spinner);
        View container = v.findViewById(R.id.quiz_content_container);
        rv = v.findViewById(R.id.matches_recycler);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MatchAdapter(requireContext(), new ArrayList<>());
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(MatchesViewModel.class);

        vm.loading().observe(getViewLifecycleOwner(), isLoading -> {
            boolean loading = Boolean.TRUE.equals(isLoading);
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            container.setVisibility(loading ? View.GONE : View.VISIBLE);
        });

        vm.matches().observe(getViewLifecycleOwner(), list -> {
            adapter.submit(list);
            progress.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
        });

        vm.error().observe(getViewLifecycleOwner(), err -> {
            if (err != null) {
                progress.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);
                android.util.Log.e("MatchesFragment", "Error cargando partidas", err);
            }
        });

        vm.load();
    }

}
