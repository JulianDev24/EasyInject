package xyz.juliandev.easy.injector;

import xyz.juliandev.easy.annotations.Named;
import xyz.juliandev.easy.annotations.Qualifier;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.stream.Stream;

public class Key<T> {

    static <T> Key<T> of(Class<T> type, Annotation qualifier) {
        if (qualifier == null) {
            return Key.of(type);
        } else {
            return qualifier.annotationType().equals(Named.class) ?
                    Key.of(type, ((Named) qualifier).value()) :
                    Key.of(type, qualifier.annotationType());
        }
    }

    public static Annotation qualifier(Annotation[] annotations) {
        return Stream.of(annotations)
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(Qualifier.class))
                .reduce((firstQualifier, secondQualifier) -> {
                    throw new EasyInjectorRuntimeException(
                            String.format("Multiple qualifier annotations found: %s, %s",
                                    firstQualifier.annotationType().getName(),
                                    secondQualifier.annotationType().getName()
                            )
                    );
                })
                .orElse(null);
    }


    private final Class<T> type;
    private final Class<? extends Annotation> qualifier;
    private final String name;

    private Key(Class<T> type, Class<? extends Annotation> qualifier, String name) {
        this.type = type;
        this.qualifier = qualifier;
        this.name = name;
    }


    /**
     * @return Key for a given type
     */
    public static <T> Key<T> of(Class<T> type) {
        return new Key<>(type, null, null);
    }

    /**
     * @return Key for a given type and qualifier annotation type
     */
    public static <T> Key<T> of(Class<T> type, Class<? extends Annotation> qualifier) {
        return new Key<>(type, qualifier, null);
    }

    /**
     * @return Key for a given type and name (@Named value)
     */
    public static <T> Key<T> of(Class<T> type, String name) {
        return new Key<>(type, Named.class, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Key<?> key)) return false;
        return type.equals(key.type) && Objects.equals(qualifier, key.qualifier) && Objects.equals(name, key.name);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (qualifier != null ? qualifier.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String suffix = name != null ? "@\"" + name + "\"" : qualifier != null ? "@" + qualifier.getSimpleName() : "";
        return type.getName() + suffix;
    }

    public Class<T> getType() {
        return type;
    }
}
