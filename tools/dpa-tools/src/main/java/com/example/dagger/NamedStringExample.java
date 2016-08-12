package com.example.dagger;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import java.sql.SQLException;
public class NamedStringExample {
    public static void main(String[] args) throws SQLException {
        Example2App app = DaggerNamedStringExample_Example2App.create();
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
        Printer providePrinter(@Named("before") String before, @Named("after") String after) {
            return msg -> System.out.println(before + " - " + msg + " - " + after);
        }
        @Provides @Named("before") String provideBefore() {
            return "Before";
        }
        @Provides @Named("after") String provideAfter() {
            return "After";
        }
    }
}
