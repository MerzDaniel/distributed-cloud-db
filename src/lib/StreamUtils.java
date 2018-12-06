package lib;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtils {
    /**
     * Util for creating a stream from an interator.
     *
     * Source: https://stackoverflow.com/a/28118885
     */
    public static <T> Stream<T> asStream(Iterator<T> sourceIterator, boolean parallel) {
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }

    /**
     * Util for creating a stream from an enumeration
     *
     * Source: https://stackoverflow.com/a/33243700/5836424
     * @param e
     * @param <T>
     * @return
     */
    public static <T> Stream<T> enumerationAsStream(Enumeration<T> e) {
        return StreamSupport.stream(
                new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED) {
                    public boolean tryAdvance(Consumer<? super T> action) {
                        if(e.hasMoreElements()) {
                            action.accept(e.nextElement());
                            return true;
                        }
                        return false;
                    }
                    public void forEachRemaining(Consumer<? super T> action) {
                        while(e.hasMoreElements()) action.accept(e.nextElement());
                    }
                }, false);
    }
}
