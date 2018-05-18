package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import io.vavr.control.Either;

import java.util.concurrent.Callable;


public class Eithers {
    /**
     * Executes a Callable and if successful returns an Either.right() with the result, or if an exception is
     * thrown returns an Either.left with the exception.   This is needed because the expression is too complex for
     * <code>javac</code> to figure out the appropriate return type of the expression without some help.
     */
    @Deprecated
    public static <R> Either<Exception, R> tryCatch(Callable<R> callable) {
        try {
            return Either.right(callable.call());
        } catch (Exception e) {
            return Either.left(e);
        }
    }
}
