package com.example.kidapp.Repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kidapp.Model.Number;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class NumberRepository {
    private final FirebaseFirestore db;

    public NumberRepository(Application application) {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Number>> getAllNumbers() {
        MutableLiveData<List<Number>> liveData = new MutableLiveData<>();
        db.collection("numbers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Number> numberList = queryDocumentSnapshots.toObjects(Number.class);
                        liveData.setValue(numberList);
                    } else {
                        liveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> liveData.setValue(null));
        return liveData;
    }
} 