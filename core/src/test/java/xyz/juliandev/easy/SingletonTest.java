package xyz.juliandev.easy;

import xyz.juliandev.easy.annotations.Singleton;
import xyz.juliandev.easy.injector.EasyInjector;
import xyz.juliandev.easy.injector.Provider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SingletonTest {
    @Test
    public void nonSingleton() {
        EasyInjector easyInjector = Easy.createInjector();
        assertNotEquals(easyInjector.getInstance(Plain.class), easyInjector.getInstance(Plain.class));
    }

    @Test
    public void singleton() {
        EasyInjector easyInjector = Easy.createInjector();
        assertEquals(easyInjector.getInstance(SingletonObj.class), easyInjector.getInstance(SingletonObj.class));
    }

    @Test
    public void singletonThroughProvider() {
        EasyInjector easyInjector = Easy.createInjector();
        Provider<SingletonObj> provider = easyInjector.getProvider(SingletonObj.class);
        assertEquals(provider.get(), provider.get());
    }

    public static class Plain {

    }

    @Singleton
    public static class SingletonObj {

    }
}
