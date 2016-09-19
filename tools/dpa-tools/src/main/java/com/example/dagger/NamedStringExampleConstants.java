package com.example.dagger;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;

import javax.inject.Named;
import javax.inject.Singleton;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

public class NamedStringExampleConstants {
    public static final String BEFORE = "before";
    public static final String AFTER = "after";

    public static void main(String[] args) throws SQLException {
        Map<String, String> map = new TreeMap<>();
        map.put(BEFORE, "Before");
        map.put(AFTER, "After");
        Example2App app = DaggerNamedStringExampleConstants_Example2App.builder().configurationMap(new ConfigurationMap(map)).build();
        app.getPrinter().printMsg("Hello World");
    }

    interface Printer {
        void printMsg(String msg);
    }

    @Singleton // https://github.com/google/dagger/issues/107#issuecomment-71524636
    @Component(modules = {ConfigurationMap.class, ConsoleModule.class})
    interface Example2App {
        Printer getPrinter();
    }

    @Module
    static class ConsoleModule {
        @Provides
        Printer providePrinter(@Named(BEFORE) String before, @Named(AFTER) String after) {
            return msg -> System.out.println(before + " - " + msg + " - " + after);
        }

        @Provides
        @Named(BEFORE)
        String provideBefore(ConfigurationMap map) {
            return map.get(BEFORE);
        }

        @Provides
        @Named(AFTER)
        String provideAfter(ConfigurationMap map) {
            return map.get(AFTER);
        }

        @Provides
        @Named(AFTER + BEFORE) // unused!
        String provideAfterBefore(ConfigurationMap map) {
            return map.get(AFTER + BEFORE);
        }
    }

}
