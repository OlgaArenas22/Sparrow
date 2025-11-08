package es.upm.miw.sparrow.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import android.widget.ArrayAdapter;

import es.upm.miw.sparrow.R;
import es.upm.miw.sparrow.domain.Match;
import es.upm.miw.sparrow.ui.adapters.MatchAdapter;
import es.upm.miw.sparrow.view.MatchesViewModel;

public class MatchesFragment extends Fragment {

    private MatchesViewModel vm;
    private RecyclerView rv;
    private ProgressBar progress;
    private MatchAdapter adapter;

    // Filtros UI
    private View filtersContainer;
    private View btnFilter;
    private MaterialAutoCompleteTextView dropCategory, dropResult, dropDate;

    // Datos
    private final List<Match> allMatches = new ArrayList<>();

    // Estado de filtros
    private String selectedCategory = "Todos";
    private String selectedResult = "Todos";
    private String selectedDate = "Más reciente";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_matches, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        progress = v.findViewById(R.id.loading_spinner);
        View contentContainer = v.findViewById(R.id.quiz_content_container);
        rv = v.findViewById(R.id.matches_recycler);

        filtersContainer = v.findViewById(R.id.filters_container);
        btnFilter = v.findViewById(R.id.btnFilter);
        dropCategory = v.findViewById(R.id.dropCategory);
        dropResult = v.findViewById(R.id.dropResult);
        dropDate = v.findViewById(R.id.dropDate);

        btnFilter.setOnClickListener(view -> {
            int vis = filtersContainer.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            filtersContainer.setVisibility(vis);
        });

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.filter_categories)
        );
        dropCategory.setAdapter(catAdapter);
        dropCategory.setText(selectedCategory, false);
        dropCategory.setOnItemClickListener((p, vv, pos, id) -> {
            selectedCategory = (String) p.getItemAtPosition(pos);
            applyFilters();
        });

        ArrayAdapter<String> resAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.filter_results)
        );
        dropResult.setAdapter(resAdapter);
        dropResult.setText(selectedResult, false);
        dropResult.setOnItemClickListener((p, vv, pos, id) -> {
            selectedResult = (String) p.getItemAtPosition(pos);
            applyFilters();
        });

        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.filter_dates)
        );
        dropDate.setAdapter(dateAdapter);
        dropDate.setText(selectedDate, false);
        dropDate.setOnItemClickListener((p, vv, pos, id) -> {
            selectedDate = (String) p.getItemAtPosition(pos);
            applyFilters();
        });

        // ----- Recycler -----
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MatchAdapter(requireContext(), new ArrayList<>());
        rv.setAdapter(adapter);

        // ----- ViewModel -----
        vm = new ViewModelProvider(this).get(MatchesViewModel.class);

        vm.loading().observe(getViewLifecycleOwner(), isLoading -> {
            boolean loading = Boolean.TRUE.equals(isLoading);
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            contentContainer.setVisibility(loading ? View.GONE : View.VISIBLE);
        });

        vm.matches().observe(getViewLifecycleOwner(), list -> {
            allMatches.clear();
            if (list != null) allMatches.addAll(list);
            applyFilters();
            progress.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
        });

        vm.error().observe(getViewLifecycleOwner(), err -> {
            if (err != null) {
                progress.setVisibility(View.GONE);
                contentContainer.setVisibility(View.VISIBLE);
                android.util.Log.e("MatchesFragment", "Error cargando partidas", err);
            }
        });

        vm.load();
    }
    private void applyFilters() {
        List<Match> filtered = new ArrayList<>();
        for (Match m : allMatches) {
            // Categoría
            if (!"Todos".equalsIgnoreCase(selectedCategory)) {
                if (m.category == null || !m.category.equalsIgnoreCase(selectedCategory)) continue;
            }
            // Resultado
            if ("Victorias".equalsIgnoreCase(selectedResult) && !m.isPassed()) continue;
            if ("Derrotas".equalsIgnoreCase(selectedResult) &&  m.isPassed())  continue;

            filtered.add(m);
        }

        // Orden por fecha
        java.util.Collections.sort(filtered, (a, b) -> {
            int cmp = a.timestamp.toDate().compareTo(b.timestamp.toDate());
            return "Más reciente".equalsIgnoreCase(selectedDate) ? -cmp : cmp;
        });

        adapter.submit(filtered);
    }
}
