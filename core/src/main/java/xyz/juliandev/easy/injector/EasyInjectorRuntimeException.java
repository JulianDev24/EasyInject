package xyz.juliandev.easy.injector;

public class EasyInjectorRuntimeException extends RuntimeException{
    EasyInjectorRuntimeException(String message) {
        super(message);
    }

    EasyInjectorRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
