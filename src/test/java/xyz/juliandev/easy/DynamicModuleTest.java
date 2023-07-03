package xyz.juliandev.easy;

import org.junit.Test;
import xyz.juliandev.easy.annotations.Inject;
import xyz.juliandev.easy.annotations.Named;
import xyz.juliandev.easy.annotations.Provides;
import xyz.juliandev.easy.injector.EasyInjector;
import xyz.juliandev.easy.module.AbstractModule;

public class DynamicModuleTest {

    @Test
    public void dynamicModule() {
        EasyInjector injector = Easy.createInjector(new AbstractModule() {
            @Provides
            @Named("foo")
            String foo() {
                return "foo";
            }
        });
        injector.addDynamicModule(new AbstractModule() {

            @Provides
            @Named("bar")
            String bar() {
                return "bar";
            }
        });

        TestObj obj = injector.getInstance(TestObj.class);
        obj.print();


    }

    public record TestObj(@Named("foo") String foo, @Named("bar") String bar) {
        @Inject
        public TestObj {}

        public void print() {
            System.out.println("fooVar: " + foo + " barVar: " + bar);
        }
    }

}
