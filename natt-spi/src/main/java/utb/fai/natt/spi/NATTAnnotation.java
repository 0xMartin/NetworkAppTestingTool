package utb.fai.natt.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import utb.fai.natt.spi.NATTKeyword.ParamValType;

/**
 * Contains all annotation definitions used within this tool.
 */
public class NATTAnnotation {

    /**
     * Annotation for full definition of NATT keyword description.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface Keyword {
        public String name();

        public String description();

        public String[] parameters() default {};

        public ParamValType[] types() default {};

        public String kwGroup();
    }

    /**
     * Annotation for simple and clear definition of NATT module.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface Module {
        public String value();
    }

}
