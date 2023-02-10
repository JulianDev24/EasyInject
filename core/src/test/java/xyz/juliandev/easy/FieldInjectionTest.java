package xyz.juliandev.easy;

import xyz.juliandev.easy.annotations.Inject;
import xyz.juliandev.easy.injector.EasyInjector;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class FieldInjectionTest {
    @Test
    public void fieldsInjected() {
        EasyInjector easyInjector = Easy.createInjector();
        Target target = new Target();
        easyInjector.injectObjectFields(target);
        assertNotNull(target.a);
    }


    public static class Target {
        @Inject
        private A a;
    }

    public static class A {

    }
}
