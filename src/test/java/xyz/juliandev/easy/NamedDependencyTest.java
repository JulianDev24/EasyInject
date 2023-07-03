package xyz.juliandev.easy;

import xyz.juliandev.easy.annotations.Named;
import xyz.juliandev.easy.annotations.Provides;
import xyz.juliandev.easy.injector.EasyInjector;
import xyz.juliandev.easy.injector.Key;
import xyz.juliandev.easy.module.AbstractModule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NamedDependencyTest {
    @Test
    public void namedInstanceWithModule() {
        EasyInjector easyInjector = Easy.createInjector(new HelloWorldModule());
        assertEquals("Hello!", easyInjector.getInstance(Key.of(String.class, "hello")));
        assertEquals("Hi!", easyInjector.getInstance(Key.of(String.class, "hi")));
    }

    public static class HelloWorldModule extends AbstractModule {
        @Provides
        @Named("hello")
        String hello() {
            return "Hello!";
        }

        @Provides
        @Named("hi")
        String hi() {
            return "Hi!";
        }
    }

}
