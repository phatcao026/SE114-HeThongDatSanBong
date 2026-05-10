package com.example.timsanbong.utils;

public interface RepositoryCallback<T> {
    void onSuccess(T data);
    void onError(String message);
}
