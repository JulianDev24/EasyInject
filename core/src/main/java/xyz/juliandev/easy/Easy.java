package xyz.juliandev.easy;

import xyz.juliandev.easy.injector.EasyInjector;
import xyz.juliandev.easy.injector.EasyInjectorImpl;
import xyz.juliandev.easy.module.AbstractModule;

import java.util.Arrays;

public final class Easy {

    private Easy() {}

    /**
     * Constructs EasyInjector with configuration modules
     */
    public static EasyInjector createInjector(AbstractModule... modules) {
        return new EasyInjectorImpl(Arrays.asList(modules));
    }

    /**
     * Constructs EasyInjector with configuration modules
     */
    public static EasyInjector createInjector(Iterable<? extends AbstractModule> modules) {
        return new EasyInjectorImpl(modules);
    }
}
