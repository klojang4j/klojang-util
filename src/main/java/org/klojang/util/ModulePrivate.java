package org.klojang.util;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * This annotation is meant to be applied to types, fields and methods within the
 * exported packages of a Java 9+ module that should nevertheless be treated as
 * module-private. They should not be used by clients of the module. They are meant
 * to be accessed from the non-exported packages in the module only. Elements
 * annotated with this annotation may disappear or change in any respect without
 * notice in a subsequent version of the module. Note that this is an
 * {@code @Documented} annotation, so the element's quasi-invisibility is in a sense
 * part of the module's public API.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE, CONSTRUCTOR, FIELD, METHOD})
public @interface ModulePrivate {
}
