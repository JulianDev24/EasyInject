package xyz.juliandev.easy.injector;

import xyz.juliandev.easy.annotations.Inject;
import xyz.juliandev.easy.annotations.Qualifier;
import xyz.juliandev.easy.annotations.Singleton;
import xyz.juliandev.easy.module.AbstractModule;
import xyz.juliandev.easy.annotations.Provides;


import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

public final class EasyInjectorImpl implements EasyInjector{

    private final Map<Key<?>, Provider<?>> providers = new ConcurrentHashMap<>();
    private final Map<Key<?>, Object> singletons = new ConcurrentHashMap<>();

    public EasyInjectorImpl(Iterable<? extends AbstractModule> modules) {
        providers.put(Key.of(EasyInjectorImpl.class), (Provider<EasyInjectorImpl>) () -> EasyInjectorImpl.this);
        for (final AbstractModule module : modules) {
            for (Method providerMethod : providers(module.getClass())) {
                providerMethod(module, providerMethod);
            }
        }
    }


    /**
     * @return an instance of type
     */
    @Override
    public <T> T getInstance(Class<T> type) {
        return provider(Key.of(type), null).get();
    }

    /**
     * @return instance specified by key (type and qualifier)
     */
    @Override
    public <T> T getInstance(Key<T> key) {
        return provider(key, null).get();
    }

    /**
     * @return provider of type
     */
    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        return provider(Key.of(type), null);
    }

    /**
     * @return provider of key (type, qualifier)
     */
    @Override
    public <T> Provider<T> getProvider(Key<T> key) {
        return provider(key, null);
    }

    /**
     * Injects fields to the target object
     */
    @Override
    public boolean injectObjectFields(Object target) {
        Object[][] fieldsToInject = injectFields(target.getClass());
        return Arrays.stream(fieldsToInject)
                .parallel()
                .allMatch(fieldData -> {
                    Field field = (Field) fieldData[0];
                    Key<?> key = (Key<?>) fieldData[2];
                    try {
                        field.set(target, (boolean) fieldData[1] ? getProvider(key) : getInstance(key));
                        return true;
                    } catch (IllegalAccessException e) {
                        return false;
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private <T> Provider<T> provider(final Key<T> key, Set<Key<?>> chain) {
        if (!providers.containsKey(key)) {
            final Constructor<?> constructor = constructor(key);
            final Provider<?>[] paramProviders = paramProviders(key, constructor.getParameterTypes(), constructor.getGenericParameterTypes(), constructor.getParameterAnnotations(), chain);
            providers.put(key, singletonProvider(key, key.getType().getAnnotation(Singleton.class), (Provider<?>) () -> {
                        try {
                            return constructor.newInstance(params(paramProviders));
                        } catch (Exception e) {
                            throw new EasyInjectorRuntimeException(String.format("Can't instantiate %s", key), e);
                        }
                    })
            );
        }
        return (Provider<T>) providers.get(key);
    }

    private void providerMethod(AbstractModule module, Method m) {
        Key<?> key = Key.of(m.getReturnType(), qualifier(m.getAnnotations()));
        if (providers.containsKey(key)) {
            throw new EasyInjectorRuntimeException("Multiple providers for " + key + " in module " + module.getClass());
        }

        Singleton singleton = m.getAnnotation(Singleton.class);
        Provider<?>[] paramProviders = paramProviders(key, m.getParameterTypes(), m.getGenericParameterTypes(), m.getParameterAnnotations(), Collections.singleton(key));
        providers.put(key, singletonProvider(key, singleton, () -> {
            try {
                return m.invoke(module, params(paramProviders));
            } catch (Exception e) {
                throw new EasyInjectorRuntimeException("Cannot instantiate " + key + " with provider", e);
            }
        }));
    }

    @SuppressWarnings("unchecked")
    private <T> Provider<T> singletonProvider(final Key<?> key, Singleton singleton, final Provider<T> provider) {
        return singleton != null ? () -> {
            if (!singletons.containsKey(key)) {
                synchronized (singletons) {
                    if (!singletons.containsKey(key)) {
                        singletons.put(key, provider.get());
                    }
                }
            }
            return (T) singletons.get(key);
        } : provider;
    }

    private Provider<?>[] paramProviders(
            final Key<?> key,
            Class<?>[] parameterClasses,
            Type[] parameterTypes,
            Annotation[][] annotations,
            final Set<Key<?>> chain
    ) {
        Provider<?>[] providers = new Provider<?>[parameterTypes.length];
        CompletableFuture<?>[] futureProviders = new CompletableFuture[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            int finalI = i;
            futureProviders[i] = CompletableFuture.supplyAsync(() -> {
                Class<?> parameterClass = parameterClasses[finalI];
                Annotation qualifier1 = Key.qualifier(annotations[finalI]);
                Class<?> providerType = Provider.class.equals(parameterClass) ?
                        (Class<?>) ((ParameterizedType) parameterTypes[finalI]).getActualTypeArguments()[0] :
                        null;
                if (providerType == null) {
                    final Key<?> newKey = Key.of(parameterClass, qualifier1);
                    final Set<Key<?>> newChain = append(chain, key);
                    if (newChain.contains(newKey)) {
                        throw new EasyInjectorRuntimeException(String.format("Circular dependency: %s", chain(newChain, newKey)));
                    }
                    return provider(newKey, newChain).get();
                } else {
                    final Key<?> newKey = Key.of(providerType, qualifier1);
                    return provider(newKey, null);
                }
            });
        }
        try {
            CompletableFuture.allOf(futureProviders).join();
        } catch (CompletionException e) {
            throw new EasyInjectorRuntimeException("Error while processing parameters in parallel", e);
        }
        for (int i = 0; i < parameterTypes.length; ++i) {
            int finalI = i;
            providers[i] = () -> futureProviders[finalI].join();
        }
        return providers;
    }

    private Object[] params(Provider<?>[] paramProviders) {
        Object[] params = new Object[paramProviders.length];
        for (int i = 0; i < paramProviders.length; ++i) {
            params[i] = paramProviders[i].get();
        }
        return params;
    }

    private Set<Key<?>> append(Set<Key<?>> set, Key<?> newKey) {
        if (set != null && !set.isEmpty()) {
            Set<Key<?>> appended = new LinkedHashSet<>(set);
            appended.add(newKey);
            return appended;
        } else {
            return Collections.singleton(newKey);
        }
    }

    private Object[][] injectFields(Class<?> target) {
        Set<Field> fields = getAnnotatedFields(target);
        Object[][] fs = new Object[fields.size()][];
        int i = 0;
        for (Field f : fields) {
            Class<?> providerType = f.getType().equals(Provider.class) ?
                    (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0] :
                    null;

            Class<?> providerClass = providerType != null ? providerType : f.getType();

            fs[i++] = new Object[]{
                    f,
                    providerType != null,
                    Key.of(providerClass, qualifier(f.getAnnotations()))
            };
        }
        return fs;
    }

    private Set<Field> getAnnotatedFields(Class<?> type) {
        Class<?> current = type;
        Set<Field> fields = new HashSet<>();
        while (!current.equals(Object.class)) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    private String chain(Set<Key<?>> chain, Key<?> lastKey) {
        StringBuilder chainString = new StringBuilder();
        for (Key<?> key : chain) {
            chainString.append(key.toString()).append(" -> ");
        }
        return chainString.append(lastKey.toString()).toString();
    }

    private Constructor<?> constructor(Key<?> key) {
        Constructor<?> inject = null;
        Constructor<?> noarg = null;
        for (Constructor<?> c : key.getType().getDeclaredConstructors()) {
            if (c.isAnnotationPresent(Inject.class)) {
                if (inject == null) {
                    inject = c;
                } else {
                    throw new EasyInjectorRuntimeException(String.format("%s has multiple @Inject constructors", key.getType()));
                }
            } else if (c.getParameterTypes().length == 0) {
                noarg = c;
            }
        }
        Constructor<?> constructor = inject != null ? inject : noarg;
        if (constructor != null) {
            constructor.setAccessible(true);
            return constructor;
        } else {
            throw new EasyInjectorRuntimeException(String.format("%s doesn't have an @Inject or no-arg constructor, or a module provider", key.getType().getName()));
        }
    }

    private Set<Method> providers(Class<?> type) {
        Class<?> current = type;
        Set<Method> providers = new HashSet<>();
        while (!current.equals(Object.class)) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Provides.class) && (type.equals(current) || !providerInSubClass(method, providers))) {
                    method.setAccessible(true);
                    providers.add(method);
                }
            }
            current = current.getSuperclass();
        }
        return providers;
    }

    private Annotation qualifier(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                return annotation;
            }
        }
        return null;
    }

    private boolean providerInSubClass(Method method, Set<Method> discoveredMethods) {
        for (Method discovered : discoveredMethods) {
            if (discovered.getName().equals(method.getName()) && Arrays.equals(method.getParameterTypes(), discovered.getParameterTypes())) {
                return true;
            }
        }
        return false;
    }

}
