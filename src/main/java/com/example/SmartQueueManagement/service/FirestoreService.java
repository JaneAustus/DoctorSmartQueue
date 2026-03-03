package com.example.SmartQueueManagement.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FirestoreService {

    private final Firestore db;

    public String save(String collection, Object data, String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(collection).document(id);
        ApiFuture<WriteResult> result = docRef.set(data);
        return result.get().getUpdateTime().toString();
    }

    public String saveWithAutoId(String collection, Object data) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(collection).document();
        ApiFuture<WriteResult> result = docRef.set(data);
        return docRef.getId();
    }

    public <T> T get(String collection, String id, Class<T> type) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(collection).document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(type);
        }
        return null;
    }

    public <T> List<T> getAll(String collection, Class<T> type) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = db.collection(collection).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream().map(doc -> doc.toObject(type)).collect(Collectors.toList());
    }

    public <T> List<T> query(String collection, String field, Object value, Class<T> type)
            throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = db.collection(collection).whereEqualTo(field, value).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream().map(doc -> doc.toObject(type)).collect(Collectors.toList());
    }

    public void delete(String collection, String id) {
        db.collection(collection).document(id).delete();
    }
}
