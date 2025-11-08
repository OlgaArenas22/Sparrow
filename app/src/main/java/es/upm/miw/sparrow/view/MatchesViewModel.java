package es.upm.miw.sparrow.view;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import es.upm.miw.sparrow.data.datasource.MatchesDataSource;
import es.upm.miw.sparrow.domain.Match;

public class MatchesViewModel extends AndroidViewModel {

    private final MatchesDataSource ds;
    private final MutableLiveData<List<Match>> _matches = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    private final MutableLiveData<Throwable> _error = new MutableLiveData<>(null);

    public MatchesViewModel(@NonNull Application app) {
        super(app);
        ds = new MatchesDataSource(app.getApplicationContext());
    }

    public LiveData<List<Match>> matches() { return _matches; }
    public LiveData<Boolean> loading() { return _loading; }
    public LiveData<Throwable> error() { return _error; }

    public void load() {
        _loading.setValue(true);
        ds.fetchMyMatches(new MatchesDataSource.Callback() {
            @Override public void onSuccess(List<Match> matches) {
                _loading.postValue(false);
                _matches.postValue(matches);
            }
            @Override public void onError(Throwable t) {
                _loading.postValue(false);
                _error.postValue(t);
            }
        });
    }
}
