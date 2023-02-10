package xyz.juliandev.easy;

import xyz.juliandev.easy.annotations.Inject;
import xyz.juliandev.easy.annotations.Named;
import xyz.juliandev.easy.annotations.Provides;
import xyz.juliandev.easy.injector.EasyInjector;
import xyz.juliandev.easy.injector.Key;
import xyz.juliandev.easy.module.AbstractModule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PolymorphicDependencyTest {
    @Test
    public void multipleImplementations() {
        EasyInjector easyInjector = Easy.createInjector(new Module());
        assertEquals(FooA.class, easyInjector.getInstance(Key.of(Foo.class, "A")).getClass());
        assertEquals(FooB.class, easyInjector.getInstance(Key.of(Foo.class, "B")).getClass());
    }

    public static class Module extends AbstractModule {
        @Provides
        @Named("A")
        Foo a(FooA fooA) {
            return fooA;
        }

        @Provides @Named("B")
        Foo a(FooB fooB) {
            return fooB;
        }
    }

    interface Foo {

    }

    public static class FooA implements Foo {
        @Inject
        public FooA() {
        }
    }

    public static class FooB implements Foo {
        @Inject
        public FooB() {
        }

    }
}
