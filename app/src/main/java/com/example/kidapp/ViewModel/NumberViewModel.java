package com.example.kidapp.ViewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kidapp.Repository.NumberRepository;
import com.example.kidapp.Model.Number;

import java.util.List;

public class NumberViewModel extends AndroidViewModel {
    private final NumberRepository numberRepository;

    public NumberViewModel(Application application) {
        super(application);
        numberRepository = new NumberRepository(application);
    }

    // Lấy tất cả số
    public LiveData<List<Number>> getAllNumbers() {
        return numberRepository.getAllNumbers();
    }
} 