package xyz.juliandev.easy.injector;

public interface EasyInjector {

    /**
     * @return an instance of type
     */
    <T> T getInstance(Class<T> type);

    /**
     * @return instance specified by key (type and qualifier)
     */
    <T> T getInstance(Key<T> key);

    /**
     * @return provider of type
     */
    <T> Provider<T> getProvider(Class<T> type);

    /**
     * @return provider of key (type, qualifier)
     */
    <T> Provider<T> getProvider(Key<T> key);

    boolean injectObjectFields(Object target);

}
