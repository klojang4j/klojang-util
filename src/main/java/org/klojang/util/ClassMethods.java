package org.klojang.util;

import org.klojang.check.Check;
import org.klojang.check.CommonChecks;

import java.util.*;

import static org.klojang.check.CommonChecks.notNull;
import static org.klojang.check.CommonChecks.sameAs;
import static org.klojang.check.Tag.TYPE;
import static org.klojang.util.CollectionMethods.swapAndFreeze;

/**
 * Methods for inspecting types.
 *
 * @author Ayco Holleman
 */
public final class ClassMethods {

  // primitive-to-wrapper
  private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.of(
      double.class,
      Double.class,
      float.class,
      Float.class,
      long.class,
      Long.class,
      int.class,
      Integer.class,
      char.class,
      Character.class,
      short.class,
      Short.class,
      byte.class,
      Byte.class,
      boolean.class,
      Boolean.class,
      void.class,
      Void.class);

  // wrapper-to-primitive
  private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE = swapAndFreeze(PRIMITIVE_TO_WRAPPER);

  private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = Map.of(int.class,
      0,
      boolean.class,
      Boolean.FALSE,
      double.class,
      0D,
      long.class,
      0L,
      float.class,
      0F,
      short.class,
      (short) 0,
      byte.class,
      (byte) 0,
      char.class,
      '\0');

  private static final Set<Class<?>> PRIMITIVE_NUMBER_TYPES = Set.of(int.class,
      double.class,
      float.class,
      long.class,
      short.class,
      byte.class);

  private ClassMethods() {
    throw new UnsupportedOperationException();
  }

  /**
   * Performs a brute-force cast to {@code <R>} of the specified object. This method may be useful when
   * bumping up against the limits of generics:
   *
   * <blockquote><pre>{@code
   * List<CharSequence> list1 = List.of("Hello", "world");
   * // WON'T COMPILE: List<String> list2 = list1;
   * List<String> list2 = ClassMethods.cast(list1);
   * }</pre></blockquote>
   *
   * <p>Handle with care, however, as it completely bypasses the Java type system:
   *
   * <blockquote><pre>{@code
   * String s = ClassMethods.cast(new File("/tmp/foo.txt"));
   * // Compiles! But will throw a ClassCastException at runtime.
   * }</pre></blockquote>
   *
   * @param obj the object whose type to cast
   * @param <T> the type of the object
   * @param <R> the type to case it to
   * @return an instance of type {@code <R>}
   */
  @SuppressWarnings({"unchecked"})
  public static <T, R> R cast(T obj) {
    return (R) obj;
  }

  /**
   * Alias for {@link Class#isInstance(Object) type.isInstance(obj)}.
   *
   * @param obj the object to test
   * @param type the class or interface to test the object against
   * @return whether the 1st argument is an instance of the 2nd argument
   */
  public static boolean isA(Object obj, Class<?> type) {
    Check.that(obj, "instance").is(notNull()).and(type, TYPE).is(notNull());
    return type.isInstance(obj);
  }

  /**
   * Tests whether the first class is the same as, or a subtype of the second class. In other words, whether
   * {@code type0} extends or implements {@code type1}.
   *
   * @param type0 the class or interface you are interested in
   * @param type1 the class or interface to compare it against
   * @return {@code true} if the first class is the same, or a subtype of the second class; {@code false}
   *     otherwise
   * @see CommonChecks#subtypeOf()
   */
  public static boolean isSameOrSubtype(Class<?> type0, Class<?> type1) {
    Check.notNull(type0, "type0");
    Check.notNull(type1, "type1");
    return type1.isAssignableFrom(type0);
  }

  /**
   * Tests whether the first class is a subtype of the second class. In other words, whether {@code class0}
   * extends or implements {@code class1}.
   *
   * @param type0 the class or interface you are interested in
   * @param type1 the class or interface to compare it against
   * @return {@code true} if the first class is the same, or a subtype of the second class; {@code false}
   *     otherwise
   * @see CommonChecks#subtypeOf()
   */
  public static boolean isSubtype(Class<?> type0, Class<?> type1) {
    Check.notNull(type0, "type0");
    Check.notNull(type1, "type1");
    return type0 != type1 && type1.isAssignableFrom(type0);
  }

  /**
   * Tests whether the first class is the same as, or a supertype of the second class. In other words, whether
   * {@code type0} is extended or implemented by {@code type1}.
   *
   * @param type0 the class or interface you are interested in
   * @param type1 the class or interface to compare it against
   * @return {@code true} if the first class is a supertype of the second class; {@code false} otherwise
   * @see CommonChecks#supertypeOf()
   */
  public static boolean isSameOrSupertype(Class<?> type0, Class<?> type1) {
    Check.notNull(type0, "type0");
    Check.notNull(type1, "type1");
    return type0.isAssignableFrom(type1);
  }

  /**
   * Tests whether the first class is a supertype of the second class. In other words, whether {@code type0}
   * is extended or implemented by {@code type1}.
   *
   * @param type0 the class or interface you are interested in
   * @param type1 the class or interface to compare it against
   * @return {@code true} if the first class is a supertype of the second class; {@code false} otherwise
   * @see CommonChecks#supertypeOf()
   */
  public static boolean isSupertype(Class<?> type0, Class<?> type1) {
    Check.notNull(type0, "type0");
    Check.notNull(type1, "type1");
    return type0 != type1 && type0.isAssignableFrom(type1);
  }

  /**
   * Returns {@code true} if the specified class is one of the primitive number classes. Note that this does
   * not include {@code char.class} &#8212; just as {@link Character} does not extend {@link Number}.
   *
   * @param type the class to test
   * @return whether the specified class is one of the primitive number classes
   */
  public static boolean isPrimitiveNumber(Class<?> type) {
    return Check.notNull(type).ok(PRIMITIVE_NUMBER_TYPES::contains);
  }

  /**
   * Returns {@code true} if the specified object is an array of a primitive type
   * <i>or</i> a {@code Class} object representing an array of a primitive type.
   *
   * @param obj the object to test
   * @return {@code true} if the specified object is an array of a primitive type, or a {@code Class} object
   *     representing an array of a primitive type
   */
  public static boolean isPrimitiveArray(Object obj) {
    if (obj instanceof Class<?> c) {
      return isPrimitiveArray(c);
    }
    return obj != null && isPrimitiveArray(obj.getClass());
  }

  /**
   * Returns {@code true} if the specified {@code Class} object represents an array of a primitive type.
   *
   * @param type the class to test
   * @return {@code true} if the specified {@code Class} object represents an array of a primitive type
   */
  public static boolean isPrimitiveArray(Class<?> type) {
    Check.notNull(type);
    return type.isArray() && type.getComponentType().isPrimitive();
  }

  /**
   * Returns {@code true} if the specified type represents an array with a primitive type as its innermost
   * component type. So this method will return {@code true} not just for {@code int[]}, but also for
   * {@code int[][]}, {@code int[][][]}, etc.
   *
   * @param type the class to test
   * @return {@code true} if the specified type represents an array with a primitive type as its deepest-level
   *     component type
   * @see ArrayMetaData
   */
  public static boolean hasPrimitiveElements(Class<?> type) {
    Check.notNull(type);
    return type.isArray() && ArrayMetaData.of(type).getBaseType().isPrimitive();
  }

  /**
   * Returns {@code true} if the specified class is one of the primitive wrapper classes.
   *
   * @param type the class to test
   * @return {@code true} if the specified class is one of the primitive wrapper classes
   */
  public static boolean isWrapper(Class<?> type) {
    return WRAPPER_TO_PRIMITIVE.containsKey(type);
  }

  /**
   * Returns {@code true} if first argument is a primitive wrapper (like {@code Integer}) and the second
   * argument is the corresponding primitive type ({@code int}). If the first argument is not a wrapper class,
   * this method returns {@code false}.
   *
   * @param type the class to test
   * @param primitiveType the class to compare it with (supposedly, but not necessarily, a primitive
   *     type)
   * @return whether instances of the first class will be auto-unboxed into instances of the second class
   */
  public static boolean isAutoUnboxedAs(Class<?> type, Class<?> primitiveType) {
    Check.notNull(type);
    Check.notNull(primitiveType).is(Class::isPrimitive, "not a primitive type: ${arg}");
    return PRIMITIVE_TO_WRAPPER.get(primitiveType) == type;
  }

  /**
   * Returns {@code true} if first argument is a primitive type and the second argument is the corresponding
   * wrapper class. If the first class is not a primitive type (like {@code int.class}), this method returns
   * {@code false}.
   *
   * @param type the class to test
   * @param wrapperType the class to compare it with (supposedly, but not necessarily, a primitive wrapper
   *     type)
   * @return whether instances of the first class will be auto-unboxed into instances of the second class
   */
  public static boolean isAutoBoxedAs(Class<?> type, Class<?> wrapperType) {
    Check.notNull(type);
    Check.notNull(wrapperType).is(ClassMethods::isWrapper, "not a wrapper type: ${arg}");
    return WRAPPER_TO_PRIMITIVE.get(wrapperType) == type;
  }

  /**
   * Returns the wrapper class corresponding to the specified class <i>if</i> it is a primitive type; else the
   * class itself is returned.
   *
   * @param type the (primitive) class
   * @return The corresponding wrapper class
   */
  public static Class<?> box(Class<?> type) {
    Check.notNull(type);
    return PRIMITIVE_TO_WRAPPER.getOrDefault(type, type);
  }

  /**
   * Returns the primitive type corresponding to the specified class <i>if</i> it is a wrapper class; else the
   * class itself is returned.
   *
   * @param type the (wrapper) class
   * @return The corresponding primitive class
   */
  public static Class<?> unbox(Class<?> type) {
    Check.notNull(type);
    return WRAPPER_TO_PRIMITIVE.getOrDefault(type, type);
  }

  /**
   * Returns the class hierarchy of the specified class up to, and including {@code Object.class}.
   *
   * @param type the class for which to get the class hierarchy.
   * @return The superclasses of the specified class.
   */
  public static List<Class<?>> getAncestors(Class<?> type) {
    Check.notNull(type).isNot(Class::isInterface, "cannot get ancestors for interface type ${arg}");
    List<Class<?>> ancestors = new ArrayList<>(5);
    for (Class<?> x = type.getSuperclass(); x != null; x = x.getSuperclass()) {
      ancestors.add(x);
    }
    return ancestors;
  }

  /**
   * Returns the number of classes in the class hierarchy of the specified class.
   *
   * @param type the class for which to count the number of classes in its class hierarchy
   * @return the number of classes in the class hierarchy of the specified class
   */
  public static int countAncestors(Class<?> type) {
    Check.notNull(type).isNot(Class::isInterface, "cannot count ancestors for interface type ${arg}");
    int i = 0;
    for (Class<?> x = type.getSuperclass(); x != null; x = x.getSuperclass()) {
      ++i;
    }
    return i;
  }

  /**
   * Returns the entire interface hierarchy, both "horizontal" and "vertical", associated with specified class
   * or interface. Returns an empty set if the argument is a top-level interface, or if the class is a regular
   * class that does not implement any interface (directly, or indirectly via its superclass).
   *
   * @param type the {@code Class} object for which to retrieve the interface hierarchy
   * @return The interface hierarchy for the specified {@code Class} object
   */
  public static Set<Class<?>> getAllInterfaces(Class<?> type) {
    Check.notNull(type);
    Set<Class<?>> bucket = new LinkedHashSet<>();
    collectInterfaces(type, bucket);
    for (Class<?> c = type.getSuperclass(); c != null; c = c.getSuperclass()) {
      collectInterfaces(c, bucket);
    }
    return bucket;
  }

  private static void collectInterfaces(Class<?> type, Set<Class<?>> bucket) {
    Class<?>[] interfaces = type.getInterfaces();
    bucket.addAll(Arrays.asList(interfaces));
    Arrays.stream(interfaces).forEach(i -> collectInterfaces(i, bucket));
  }

  /**
   * Returns a prettified version of the specified object's fully-qualified class name. Equivalent to
   * {@link #className(Class) className(obj.getClass())}.
   *
   * @param obj the object whose class name to return
   * @return The class name
   */
  public static String className(Object obj) {
    Check.notNull(obj);
    return className(obj.getClass());
  }

  /**
   * Returns a prettified version of the fully-qualified class name. If the provided type is a non-array type,
   * this method simply forwards to {@link Class#getName()}; otherwise it is equivalent to
   * {@link ArrayMetaData#getArrayClassName() ArrayType.forClass(clazz).arrayClassName()}.
   *
   * @param type the class whose name to return
   * @return The class name
   */
  public static String className(Class<?> type) {
    Check.notNull(type);
    if (type.isArray()) {
      return ArrayMetaData.of(type).getArrayClassName();
    }
    return type.getName();
  }

  /**
   * Returns a prettified version of an object's simple class name. If the type is a non-array type this
   * method simply forwards to {@linkplain Class#getSimpleName()}; otherwise it is equivalent to
   * {@code ArrayType.forClass(clazz).toString()}.
   *
   * @param obj the object whose class name to return
   * @return The class name
   */
  public static String simpleClassName(Object obj) {
    Check.notNull(obj);
    return simpleClassName(obj.getClass());
  }

  /**
   * Returns a prettified version of the simple class name. If the provided type is a non-array type this
   * method simply forwards to {@linkplain Class#getSimpleName()}; otherwise it is equivalent to
   * {@code ArrayType.forClass(clazz).toString()}.
   *
   * @param clazz the class whose ame to return
   * @return The class name
   */
  public static String simpleClassName(Class<?> clazz) {
    Check.notNull(clazz);
    if (clazz.isArray()) {
      return ArrayMetaData.of(clazz).toString();
    }
    return clazz.getSimpleName();
  }

  /**
   * Returns a short description of the argument. Unless the argument is {@code null}, the description will at
   * least contain the simple class name of the argument.
   *
   * <ul>
   * <li>If the argument is {@code null}, the string "null" is returned.
   * <li>If the argument is a {@link Collection}, the returned string will contain
   * the collection's simple class name and its size. For example: "{@code ArrayList[113]}"
   * <li>If the argument is a {@link Map}, the returned string will contain the map's
   * simple class name and its size. For example: "{@code TreeMap[97]}"
   * <li>If the argument is an array, the returned string will contain the simple
   * class name of the array's innermost
   * {@linkplain Class#getComponentType() component type} and its length. For
   * example: "{@code String[42][][]}"
   * <li>If the argument is a {@code Class} object, the returned string will look
   * like this: "{@code FileOutputStream.class}" (with ".class" appended to the simple class
   * name)
   * <li>Otherwise, the simple class name of the argument is returned.
   * </ul>
   *
   * @param obj the object to describe
   * @return a description of the object
   * @see ArrayMetaData#describe(Object)
   */
  public static String describe(Object obj) {
    if (obj == null) {
      return "null";
    } else if (obj.getClass() == Class.class) {
      return ((Class<?>) obj).getSimpleName() + ".class";
    } else if (obj instanceof Collection<?> c) {
      return c.getClass().getSimpleName() + '[' + c.size() + ']';
    } else if (obj instanceof Map<?, ?> m) {
      return m.getClass().getSimpleName() + '[' + m.size() + ']';
    } else if (obj.getClass().isArray()) {
      return ArrayMetaData.describe(obj);
    }
    return obj.getClass().getSimpleName();
  }

  /**
   * Returns zero, cast to the appropriate type, for primitive types; {@code null} for any other type.
   *
   * @param <T> The type of the class
   * @param type the class for which to retrieve the default value
   * @return The default value
   */
  @SuppressWarnings("unchecked")
  public static <T> T getTypeDefault(Class<T> type) {
    return Check.notNull(type, "type").isNot(sameAs(), void.class).ok().isPrimitive()
        ? (T) PRIMITIVE_DEFAULTS.get(type)
        : null;
  }

}
