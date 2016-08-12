package com.example.dagger;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import java.sql.SQLException;
public class NamedStringExampleConstants {
    public static final String BEFORE = "before";
    public static final String AFTER = "after";
    public static void main(String[] args) throws SQLException {
        Example2App app = DaggerNamedStringExampleConstants_Example2App.create();
        app.getPrinter().printMsg("Hello World");
    }
    interface Printer {
        void printMsg(String msg);
    }
    @Component(modules = ConsoleModule.class)
    interface Example2App {
        Printer getPrinter();
    }
    @Module
    static class ConsoleModule {
        @Provides
        Printer providePrinter(@Named(BEFORE) String before, @Named(AFTER) String after) {
            return msg -> System.out.println(before + " - " + msg + " - " + after);
        }
        @Provides @Named(BEFORE) String provideBefore() {
            return "Before";
        }
        @Provides @Named(AFTER) String provideAfter() {
            return "After";
        }
    }
}
