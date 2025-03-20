package org.klojang.util;

import org.klojang.check.Check;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.klojang.check.CommonChecks.*;
import static org.klojang.check.Tag.VARARGS;
import static org.klojang.util.ArrayMethods.prefix;
import static org.klojang.util.ArrayMethods.repeat;
import static org.klojang.util.InvokeMethods.getArrayLength;

/**
 * Provides metadata about an array or array type and allows you to generate arrays with a different base type
 * or dimensionality. The base type is the {@linkplain Class#getComponentType() component type} of the
 * innermost array of an n-dimensional array. So for {@code int[][][]} that would be {@code int}.
 * {@code int[][]} is the component type of {@code int[][][]}; {@code int} is its base type. In other words:
 * <i>the base type of an array always is a non-array type</i>. (NB while <i>component type</i> is standard
 * terminology, there is no commonly accepted term for the type of objects that ultimately occupy the slots in
 * the array &#8212; in any dimension. Here we use the term <i>base type</i>. <i>Element type</i> would
 * another reasonable option.)
 *
 * <blockquote><pre>{@code
 * ArrayMetaData metadata = ArrayMetaData.of(int[][].class);
 * Integer[][][] cube = metadata.box().addDimension().newHyperCube(10);
 * }</pre></blockquote>
 *
 * @author Ayco Holleman
 */
public final class ArrayMetaData {

  /**
   * Returns a description of the provided array. It contains the base type's simple class name and the length
   * of the outermost array. For example, for an array defined as {@code new Double[4][12]}, this method would
   * return "Double[4][]".
   *
   * @param array the array to describe
   * @return a description of the array
   */
  public static String describe(Object array) {
    ArrayMetaData metadata = forArray(array);
    int len = getArrayLength(array);
    StringBuilder sb = new StringBuilder(metadata.baseType.getSimpleName())
        .append('[')
        .append(len)
        .append(']');
    sb.append("[]".repeat(Math.max(0, metadata.dimensions - 1)));
    return sb.toString();
  }

  /**
   * Returns the base type of the specified array type. An {@link IllegalArgumentException} is thrown if the
   * provided class is not an array type.
   *
   * @param arrayType the array type to inspect
   * @return the base type of the specified array type
   */
  public static Class<?> getBaseType(Class<?> arrayType) {
    Check.notNull(arrayType).is(array());
    return baseType(arrayType);
  }

  /**
   * Returns the base type of the specified array. An {@link IllegalArgumentException} is thrown if the
   * provided object is not an array.
   *
   * @param array the array to inspect
   * @return the base type of the specified array
   */
  public static Class<?> getBaseType(Object array) {
    Check.notNull(array);
    if (array.getClass() == Class.class) {
      // JVM might end up here with dynamically invoked code (method handles / reflection)
      return baseType((Class<?>) array);
    }
    return baseType(array.getClass());
  }

  /**
   * Returns zero for non-array types, and the number of dimensions for array types.
   *
   * @param clazz the type for which to get the number of dimensions
   * @return the dimensionality of the type.
   */
  public static int countDimensions(Class<?> clazz) {
    Check.notNull(clazz);
    return dimensions(clazz);
  }

  /**
   * Returns zero for non-array objects, and the number of dimensions for arrays.
   *
   * @param obj the object for which to get the number of dimensions
   * @return the dimensionality of the object.
   */
  public static int countDimensions(Object obj) {
    Check.notNull(obj);
    if (obj.getClass() == Class.class) {
      return dimensions((Class<?>) obj);
    }
    return dimensions(obj.getClass());
  }

  /**
   * Returns an {@code ArrayMetaData} instance corresponding to the specified array object. An
   * {@link IllegalArgumentException} is thrown if the provided object is not an array.
   *
   * @param array the array
   * @return the {@code ArrayMetaData} instance
   */
  public static ArrayMetaData forArray(Object array) {
    Check.notNull(array).is(array());
    var c = array.getClass();
    return new ArrayMetaData(c, baseType(c), dimensions(c));
  }

  /**
   * Returns a {@code ArrayMetaData} instance corresponding to the specified array type. An
   * {@link IllegalArgumentException} is thrown if the provided class object does not represent an array
   * type.
   *
   * @param arrayType the array class
   * @return the {@code ArrayMetaData} instance
   */
  public static ArrayMetaData of(Class<?> arrayType) {
    Check.notNull(arrayType).is(array());
    return new ArrayMetaData(arrayType, baseType(arrayType), dimensions(arrayType));
  }


  /**
   * Creates a new {@code ArrayMetaData} instance. The provided type may or may not be an array type. If it is
   * an array type, its base type will become the base type of the {@code ArrayMetaData} instance and its
   * dimension count will be added to the provided number of dimensions. If the provided type is not an array
   * type, the type itself will become the base type of the {@code ArrayMetaData} instance. The specified
   * number of dimensions may be zero or negative, as long as the sum of the dimensions remains positive:
   *
   * <blockquote><pre>{@code
   * var twoDimensional = float[][].class; // base type: float
   * var threeDimensional = ArrayMetaData.of(twoDimensional, 1); // represents float[][][].class
   * var oneDimensional = ArrayMetaData.of(twoDimensional, -1);  // represents float[].class
   * }</pre></blockquote>
   *
   * @param type the type to inspect
   * @param dimensions the number of dimensions
   */
  public static ArrayMetaData of(Class<?> type, int dimensions) {
    Check.notNull(type);
    return new ArrayMetaData(baseType(type), dimensions(type) + dimensions);
  }

  private final Class<?> baseType;
  private final int dimensions;

  private Class<?> arrayClass;

  private ArrayMetaData(Class<?> baseType, int dimensions) {
    Check.that(dimensions, "number of dimensions").is(positive());
    this.baseType = baseType;
    this.dimensions = dimensions;
  }

  private ArrayMetaData(Class<?> arrayClass, Class<?> baseType, int dimensions) {
    Check.that(dimensions, "number of dimensions").is(positive());
    this.arrayClass = arrayClass;
    this.baseType = baseType;
    this.dimensions = dimensions;
  }

  /**
   * Returns the base type of the array represented by this instance.
   *
   * @return the base type of the array represented by this instance
   */
  public Class<?> getBaseType() {
    return baseType;
  }

  /**
   * Returns the number of dimensions of the array represented by this instance.
   *
   * @return the number of dimensions of the array represented by this instance
   */
  public int getDimensions() {
    return dimensions;
  }

  /**
   * Returns the {@code Class} object corresponding to this instance. For example, if the base type of this
   * instance is {@code byte} and the number of dimensions recorded by this instance is three, then this
   * method will return {@code byte[][][].class}.
   *
   * @return a {@code Class} object
   */
  public Class<?> getArrayClass() {
    if (arrayClass == null) {
      arrayClass = arrayClass(baseType, dimensions);
    }
    return arrayClass;
  }

  /**
   * <p>Creates a new array with the specified length, using the base type and number of dimensions of
   * this {@code ArrayMetaData} instance. For example, if the provided length is 10, the base type is
   * {@code short.class}, and the number of dimensions is three, then calling this method amounts to calling
   * {@code new short[10][][]}:
   *
   * <blockquote><pre>{@code
   * var expected = new short[10][][];
   * ArrayMetaData metadata = ArrayMetaData.of(short.class, 3);
   * var actual = metadata.newArray(10);
   * assertArrayEquals(expected, actual);
   * }</pre></blockquote>
   *
   * <p>If this {@code ArrayMetaData} instance represents a multidimensional array, you may optionally
   * specify the lengths of the arrays in the other dimensions (akin to calling {@code new short[10][8][]} or
   * {@code new short[10][8][15]}).
   *
   * @param length the length of the array
   * @param lengths the lengths of the arrays in the other dimensions
   * @param <T> the type of the array
   * @return a new array
   */
  @SuppressWarnings("unchecked")
  public <T> T newArray(int length, int... lengths) {
    Check.that(length, "array length").isNot(negative());
    Check.notNull(lengths, VARARGS);
    Check.that(lengths.length).is(lt(), dimensions, "number of lengths exceeds number of dimensions");
    IntStream.of(lengths).forEach(x -> Check.that(x, "array length").is(gte(), 0));
    return (T) populate(this, prefix(lengths, length));
  }


  /**
   * Creates a "hyper cube" in which all arrays in all dimensions have the same length (as in
   * {@code new int[10][10][10]}). Note that "hyper cube" is in fact a misnomer, because this method can also
   * be used to create a two-dimensional array ({@code new int[10][10}), or even a one-dimensional array.
   *
   * @param length the array length in all dimensions
   * @param <T> the type of the array
   * @return a new array
   */
  @SuppressWarnings("unchecked")
  public <T> T newHyperCube(int length) {
    Check.that(length, "array length").isNot(negative());
    return (T) populate(this, repeat(length, dimensions));
  }

  private static Object populate(ArrayMetaData metadata, int[] lengths) {
    int len = lengths[0];
    Object array = InvokeMethods.newArray(metadata.getArrayClass(), len);
    if (metadata.dimensions > 1 && lengths.length > 1) {
      metadata = metadata.removeDimension();
      lengths = Arrays.copyOfRange(lengths, 1, lengths.length);
      for (int i = 0; i < len; ++i) {
        InvokeMethods.setArrayElement(array, i, populate(metadata, lengths));
      }
    }
    return array;
  }

  /**
   * Returns an {@code ArrayMetaData} instance with the same number of dimensions as this instance, and with
   * the specified base type.
   *
   * @param elementType the base type of the returned {@code ArrayMetaData} instance
   * @return an {@code ArrayMetaData} instance with the same number of dimensions as this instance, and with
   *     the specified base type
   */
  public ArrayMetaData withBaseType(Class<?> elementType) {
    if (elementType != this.baseType) {
      Check.notNull(elementType);
      return new ArrayMetaData(elementType, dimensions);
    }
    return this;
  }

  /**
   * Returns an {@code ArrayMetaData} instance with the same base type as this instance, and with the
   * specified number of dimensions.
   *
   * @param dimensions the number of dimensions
   * @return an {@code ArrayMetaData} instance with the same base type as this instance, and with the
   *     specified number of dimensions
   */
  public ArrayMetaData withDimensions(int dimensions) {
    if (dimensions != this.dimensions) {
      Check.that(dimensions, "number of dimensions").is(positive());
      return new ArrayMetaData(baseType, dimensions);
    }
    return this;
  }

  /**
   * Returns an {@code ArrayMetaData} instance with one more dimension than this instance.
   *
   * @return an {@code ArrayMetaData} instance with one more dimension than this instance
   */
  public ArrayMetaData addDimension() {
    return new ArrayMetaData(baseType, dimensions + 1);
  }

  /**
   * Returns an {@code ArrayMetaData} instance with one dimension less than this instance.
   *
   * @return an {@code ArrayMetaData} instance with one dimension less than this instance
   */
  public ArrayMetaData removeDimension() {
    int x = Check.that(dimensions - 1).is(positive(), "number of dimensions must remain positive").ok();
    return new ArrayMetaData(baseType, x);
  }

  /**
   * Returns an {@code ArrayMetaData} instance for the boxed version of the base type. If the base type of
   * this instance is not a primitive type, the instance itself is returned.
   *
   * @return an {@code ArrayMetaData} for the boxed version of the base type
   */
  public ArrayMetaData box() {
    if (baseType.isPrimitive()) {
      return new ArrayMetaData(ClassMethods.box(baseType), dimensions);
    }
    return this;
  }

  /**
   * Returns the {@code ArrayMetaData} for the unboxed version of the base type. If the base type of this
   * instance is not a primitive wrapper type (like {@code Double}), the instance itself is returned.
   *
   * @return the {@code ArrayMetaData} for the unboxed version of the base type
   */
  public ArrayMetaData unbox() {
    if (ClassMethods.isWrapper(baseType)) {
      return new ArrayMetaData(ClassMethods.unbox(baseType), dimensions);
    }
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof ArrayMetaData that
        && baseType.equals(that.baseType)
        && dimensions == that.dimensions;
  }

  @Override
  public int hashCode() {
    return baseType.hashCode() ^ dimensions;
  }

  /**
   * Returns the simple class name of the array type represented by this instance. The returned string is
   * easier to understand than what you get from {@link Class#getSimpleName()}. For example the return value
   * for {@code int[][].class} would be "int[][]".
   *
   * @return the simple class name of the array type represented by this {@code ArrayMetaData}
   */
  @Override
  public String toString() {
    if (dimensions == 1) { // happy path for 99% of the cases
      return baseType.getSimpleName() + "[]";
    }
    String name = baseType.getSimpleName();
    StringBuilder sb = new StringBuilder(name.length() + dimensions * 2);
    sb.append(name);
    sb.append("[]".repeat(dimensions));
    return sb.toString();
  }

  /**
   * Returns the class name of the array type represented by this instance. The returned string is easier to
   * understand than what you get from {@link Class#getName()}. For example, the return value for
   * {@code String[][].class} would be "String[][]". For types outside the {@code java.lang} package, the
   * fully-qualified class name is used (e.g. "java.io.File[][]").
   *
   * @return the class name of the array type represented by this {@code ArrayMetaData}
   */
  public String getArrayClassName() {
    StringBuilder sb = new StringBuilder(baseTypeName());
    sb.append("[]".repeat(dimensions));
    return sb.toString();
  }

  private String baseTypeName() {
    if (baseType.getPackageName().equals(Object.class.getPackageName())) {
      return baseType.getSimpleName();
    }
    return baseType.getName();
  }

  private static Class<?> arrayClass(Class<?> baseType, int dimensions) {
    Class<?> c = baseType;
    for (int i = 0; i < dimensions; ++i) {
      c = c.arrayType();
    }
    return c;
  }

  private static Class<?> baseType(Class<?> type) {
    while (type.isArray()) {
      type = type.getComponentType();
    }
    return type;
  }

  private static int dimensions(Class<?> clazz) {
    int x = 0;
    for (var c = clazz; c.isArray(); c = c.getComponentType()) {
      ++x;
    }
    return x;
  }

}
