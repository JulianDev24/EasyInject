package xyz.juliandev.easy;

import xyz.juliandev.easy.annotations.Provides;
import xyz.juliandev.easy.injector.EasyInjector;
import xyz.juliandev.easy.module.AbstractModule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModuleOverrideTest {
    @Test
    public void dependencyOverwrittenByModule() {
        EasyInjector easyInjector = Easy.createInjector(new PlainStubOverrideModule());
        assertEquals(PlainStub.class, easyInjector.getInstance(Plain.class).getClass());
    }


    @Test
    public void moduleOverwrittenBySubClass() {
        assertEquals("foo", Easy.createInjector(new FooModule()).getInstance(String.class));
        assertEquals("bar", Easy.createInjector(new FooOverrideModule()).getInstance(String.class));
    }

    public static class Plain {
    }

    public static class PlainStub extends Plain {

    }

    public static class PlainStubOverrideModule extends AbstractModule {
        @Provides
        public Plain plain(PlainStub plainStub) {
            return plainStub;
        }

    }

    public static class FooModule extends AbstractModule{
        @Provides
        String foo() {
            return "foo";
        }
    }

    public static class FooOverrideModule extends FooModule {
        @Provides
        @Override
        String foo() {
            return "bar";
        }
    }




}
