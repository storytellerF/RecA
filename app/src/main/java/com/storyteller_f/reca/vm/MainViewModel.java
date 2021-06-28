package com.storyteller_f.reca.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * @author faber
 */
public class MainViewModel extends ViewModel {
    private MutableLiveData<Boolean> permission;
    public LiveData<Boolean> getPermission() {
        if (permission == null) {
            permission = new MutableLiveData<>();
        }
        return permission;
    }

    public void setP(boolean granted) {
        permission.setValue(granted);
    }
}
