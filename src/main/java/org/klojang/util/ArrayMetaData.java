package org.klojang.util;

import org.klojang.check.Check;

import static org.klojang.check.CommonChecks.array;
import static org.klojang.check.CommonChecks.positive;
import static org.klojang.util.InvokeMethods.getArrayLength;

/**
 * Provides metadata about an array type and allows you to generate new arrays with a different element type
 * or dimensionality from it. The element type is the {@linkplain Class#getComponentType() component type} of
 * the innermost array of an n-dimensional array. So for {@code int[][][]} that would be {@code int}.
 * {@code int[][]} is the component type of {@code int[][][]}, but {@code int} is its element type of
 * {@code int[][][]}. In other words: <i>the element type of an array always is a non-array type</i>.
 *
 * @author Ayco Holleman
 */
public final class ArrayMetaData {

  /**
   * {@code ArrayMetaData} instance representing a one-dimensional {@code byte} array.
   */
  public static final ArrayMetaData BYTE_ARRAY = new ArrayMetaData(byte.class, 1);

  /**
   * {@code ArrayMetaData} instance representing a one-dimensional {@code int} array.
   */
  public static final ArrayMetaData INT_ARRAY = new ArrayMetaData(int.class, 1);

  /**
   * Returns a description of the provided array. It contains the element type's simple class name and the
   * length of the outermost array. For example, for an array defined as {@code new Double[4][12]}, this
   * method would return "Double[4][]".
   *
   * @param array the array to describe
   * @return a description of the array
   */
  public static String describe(Object array) {
    ArrayMetaData metadata = forArray(array);
    int len = getArrayLength(array);
    StringBuilder sb = new StringBuilder(metadata.elementType.getSimpleName())
        .append('[')
        .append(len)
        .append(']');
    sb.append("[]".repeat(Math.max(0, metadata.dimensions - 1)));
    return sb.toString();
  }

  /**
   * Returns the element type of the specified array type. An {@link IllegalArgumentException} is thrown if
   * the provided class is not an array type.
   *
   * @param arrayType the array type to inspect
   * @return the element type of the specified array type
   */
  public static Class<?> getElementType(Class<?> arrayType) {
    Check.notNull(arrayType).is(array());
    return elementType(arrayType);
  }

  /**
   * Returns the element type of the specified array.
   *
   * @param array the array to inspect
   * @return the element type of the specified array
   */
  public static Class<?> getElementType(Object array) {
    Check.notNull(array);
    if (array.getClass() == Class.class) {
      // JVM might end up here with dynamically invoked code (using method handles or reflection)
      return elementType((Class<?>) array);
    }
    return elementType(array.getClass());
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
   * Returns a {@code ArrayMetaData} instance corresponding to the specified array type. An
   * {@link IllegalArgumentException} is thrown if the provided class object does not represent an array
   * type.
   *
   * @param arrayType the array class
   * @return the {@code ArrayMetaData} instance
   */
  public static ArrayMetaData forType(Class<?> arrayType) {
    Check.notNull(arrayType).is(array());
    return new ArrayMetaData(elementType(arrayType), dimensions(arrayType));
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
    return new ArrayMetaData(elementType(c), dimensions(c));
  }

  /**
   * Creates a new {@code ArrayMetaData} instance. The provided type may or may not be an array type. If it is
   * an array type, its element type will be the element type of the {@code ArrayMetaData} instance, otherwise
   * the type itself will be the element type. The specified number of dimensions may be zero or negative, as
   * long as the sum of the dimensions remains positive:
   *
   * <blockquote><pre>{@code
   * var twoDimensional = float[][].class; // element type: float
   * var threeDimensional = ArrayMetaData.of(twoDimensional, 1); // represents float[][][].class
   * var oneDimensional = ArrayMetaData.of(twoDimensional, -1);  // represents float[].class
   * }</pre></blockquote>
   *
   * @param type the element type of the array
   * @param dimensions the number of dimensions
   */
  public static ArrayMetaData of(Class<?> type, int dimensions) {
    Check.notNull(type);
    return new ArrayMetaData(elementType(type), dimensions(type) + dimensions);
  }

  private final Class<?> elementType;
  private final int dimensions;

  /**
   * Creates a new {@code ArrayMetaData} instance. The {@code baseType} argument is, in fact, allowed to be an
   * array class. However, the recorded element type will still be the absolute element type of the array
   * class, and its dimension count will be added to the specified number of dimensions. The specified number
   * of dimensions may be zero or negative, as long as the sum of the dimensions remains positive:
   *
   * <blockquote><pre>{@code
   * Class<float[][]> twoDimensional = float[][].class;
   * ArrayType threeDimensional = new ArrayType(twoDimensional, 1); // float[][][].class
   * ArrayType oneDimensional = new ArrayType(twoDimensional, -1); // float[].class
   * }</pre></blockquote>
   *
   * @param elementType the element type of the array
   * @param dimensions the number of dimensions
   */
  private ArrayMetaData(Class<?> elementType, int dimensions) {
    Check.that(dimensions, "number of dimensions").is(positive());
    this.elementType = elementType;
    this.dimensions = dimensions;
  }

  /**
   * Returns the element type of the array represented by this instance.
   *
   * @return the element type of the array represented by this instance
   */
  public Class<?> getElementType() {
    return elementType;
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
   * Returns the {@code Class} object corresponding to this instance. For example, if the element type of this
   * instance is {@code byte} and the number of dimensions recorded by this instance is three, then this
   * method will return {@code byte[][][].class}.
   *
   * @return a {@code Class} object
   */
  public Class<?> toClass() {
    return toClass(elementType, dimensions);
  }

  /**
   * Constructs a new array with the specified length, using the element type and number of dimensions of this
   * {@code ArrayMetaData} instance. For example, if the provided length is 10, the element type is
   * {@code short.class}, and the number of dimensions is two, then calling this method amounts to calling
   * {@code new short[10][][]}:
   *
   * <blockquote><pre>{@code
   * var array0 = new short[10][][];
   * ArrayMetaData metadata = ArrayMetaData.of(short.class, 3);
   * var array1 = metadata.newArray(10);
   * assertArrayEquals(array0, array1);
   * }</pre></blockquote>
   *
   * @param length the length of the array
   * @param <T> the component type the array &#8212; which, for multidimensional arrays, is not the same
   *     as its element type
   * @return a new array
   */
  @SuppressWarnings("unchecked")
  public <T> T[] newArray(int length) {
    return (T[]) InvokeMethods.newArray(toClass(), length);
  }

  /**
   * Returns an {@code ArrayMetaData} instance with the same number of dimensions as this instance, and with
   * the specified element type.
   *
   * @param elementType the element type of the returned {@code ArrayMetaData} instance
   * @return an {@code ArrayMetaData} instance with the same number of dimensions as this instance, and with
   *     the specified element type
   */
  public ArrayMetaData withElementType(Class<?> elementType) {
    if (elementType != this.elementType) {
      Check.notNull(elementType);
      return new ArrayMetaData(elementType, dimensions);
    }
    return this;
  }

  /**
   * Returns an {@code ArrayMetaData} instance with the same element type as this instance, and with the
   * specified number of dimensions.
   *
   * @param dimensions the number of dimensions
   * @return an {@code ArrayMetaData} instance with the same element type as this instance, and with the
   *     specified number of dimensions
   */
  public ArrayMetaData withDimensions(int dimensions) {
    if (dimensions != this.dimensions) {
      Check.that(dimensions, "number of dimensions").is(positive());
      return new ArrayMetaData(elementType, dimensions);
    }
    return this;
  }

  /**
   * Returns an {@code ArrayMetaData} instance for the boxed version of the element type. If the element type
   * of this instance is not a primitive type, the instance itself is returned.
   *
   * @return an {@code ArrayMetaData} for the boxed version of the element type
   */
  public ArrayMetaData box() {
    if (elementType.isPrimitive()) {
      return new ArrayMetaData(ClassMethods.box(elementType), dimensions);
    }
    return this;
  }

  /**
   * Returns the {@code ArrayMetaData} for the unboxed version of the element type. If the element type of
   * this instance is not a primitive wrapper type (like {@code Double}), the instance itself is returned.
   *
   * @return the {@code ArrayMetaData} for the unboxed version of the element type
   */
  public ArrayMetaData unbox() {
    if (ClassMethods.isWrapper(elementType)) {
      return new ArrayMetaData(ClassMethods.unbox(elementType), dimensions);
    }
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof ArrayMetaData that
        && elementType.equals(that.elementType)
        && dimensions == that.dimensions;
  }

  @Override
  public int hashCode() {
    return elementType.hashCode() ^ dimensions;
  }

  /**
   * Returns the simple class name of the array type encoded by this instance. The returned string is somewhat
   * easier to read than what you get from {@link Class#getSimpleName()}. For example the returned value for
   * {@code int[][].class} would be "int[][]".
   *
   * @return the simple class name of the array type encoded by this {@code ArrayMetaData}
   */
  @Override
  public String toString() {
    if (dimensions == 1) { // happy path for 99% of the cases
      return elementTypeName() + "[]";
    }
    String name = elementTypeName();
    StringBuilder sb = new StringBuilder(name.length() + dimensions * 2);
    sb.append(name);
    sb.append("[]".repeat(dimensions));
    return sb.toString();
  }

  /**
   * Returns the class name of the array type encoded by this instance. The returned string is easier to
   * understand than what you get from {@link Class#getName()}. For example, the return value for
   * {@code String[][].class} would be "String[][]". For types outside the {@code java.lang} package, the
   * fully-qualified class name is used (e.g. "java.io.File[][]").
   *
   * @return the class name of the array type encoded by this {@code ArrayMetaData}
   */
  public String getArrayClassName() {
    StringBuilder sb = new StringBuilder(elementTypeName());
    sb.append("[]".repeat(dimensions));
    return sb.toString();
  }

  private String elementTypeName() {
    if (elementType.getPackageName().equals(Object.class.getPackageName())) {
      return elementType.getSimpleName();
    }
    return elementType.getName();
  }

  private static Class<?> toClass(Class<?> baseType, int dimensions) {
    Class<?> c = baseType;
    for (int i = 0; i < dimensions; ++i) {
      c = c.arrayType();
    }
    return c;
  }

  private static Class<?> elementType(Class<?> type) {
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
