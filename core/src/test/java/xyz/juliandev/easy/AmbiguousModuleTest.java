package xyz.juliandev.easy;

import xyz.juliandev.easy.annotations.Provides;
import xyz.juliandev.easy.injector.EasyInjectorRuntimeException;
import xyz.juliandev.easy.module.AbstractModule;
import org.junit.Test;

public class AmbiguousModuleTest {
    @Test(expected = EasyInjectorRuntimeException.class)
    public void ambiguousModule() {
        Easy.createInjector(new Module());
    }

    public static class Module extends AbstractModule {
        @Provides
        String foo() {
            return "foo";
        }

        @Provides
        String bar() {
            return "bar";
        }
    }
}