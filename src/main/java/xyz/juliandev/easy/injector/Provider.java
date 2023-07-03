package xyz.juliandev.easy.injector;

public interface Provider<T> {
    T get();
}