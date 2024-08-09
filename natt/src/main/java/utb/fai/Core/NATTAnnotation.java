package utb.fai.Core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Obsahuje vsechny definice anotaci, ktere jsou v ramci tohoto nastoje
 * vyuzivany
 */
public class NATTAnnotation {

    /**
     * Anotace pro jednoduch a prehledene definovani parametru keyword
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface Keyword {
        public String name();
    }

    /**
     * Anotace pro jednoduch a prehledene definovani parametru modulu
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface Module {
        public String value();
    }

}
