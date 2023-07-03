package xyz.juliandev.easy;

import xyz.juliandev.easy.annotations.Inject;
import xyz.juliandev.easy.injector.EasyInjector;
import xyz.juliandev.easy.injector.Provider;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ProviderInjectionTest {
    @Test
    public void providerInjected() {
        EasyInjector easyInjector = Easy.createInjector();
        assertNotNull(easyInjector.getInstance(A.class).plainProvider.get());
    }

    public static class A {
        private final Provider<B> plainProvider;

        @Inject
        public A(Provider<B> plainProvider) {
            this.plainProvider = plainProvider;
        }
    }

    public static class B {

    }
}
