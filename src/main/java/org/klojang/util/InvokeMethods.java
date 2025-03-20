package org.klojang.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * Dynamic invocation utility methods. These methods are not meant to be used in application-level code. They
 * very thinly wrap methods from the {@code java.lang.invoke} package and <b>they do not perform any null
 * checks, type checks, range checks, etc.</b> These checks are still necessary but are left to higher-level
 * code.
 */
public final class InvokeMethods {

  //@formatter:off
  private static final class Cache extends LinkedHashMap<Class<?>, MethodHandle> {
    public Cache() {
      super(16, 0.75f, true);
    }
    public boolean removeEldestEntry(Map.Entry<Class<?>, MethodHandle> eldest) {
      return size() > 10;
    }
  }
  //@formatter:on

  private static final Cache noArgConstructors = new Cache();
  private static final Cache intArgConstructors = new Cache();
  private static final Cache arrayConstructors = new Cache();
  private static final Cache arrayLengthGetters = new Cache();
  private static final Cache elementGetters = new Cache();
  private static final Cache elementSetters = new Cache();

  /**
   * Returns a new instance of the specified class using its no-arg constructor. The
   * {@link NoSuchMethodException}, thrown if there is no such constructor, is converted to an
   * {@link InvokeException}.
   *
   * @param clazz the class to instantiate
   * @param <T> the type of the returned instance
   * @return the instance
   * @throws InvokeException if the class does not have a no-arg constructor
   */
  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Class<T> clazz) throws InvokeException {
    try {
      return (T) getNoArgConstructor(clazz).invoke();
    } catch (NoSuchMethodException e) {
      throw InvokeException.missingNoArgConstructor(clazz);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  /**
   * Returns a new instance of the specified class using the constructor that takes a single {@code int}
   * argument. The {@link NoSuchMethodException}, thrown if there is no such constructor, is converted to an
   * {@link InvokeException}.
   *
   * @param clazz the class
   * @param arg0 the constructor argument
   * @param <T> the type of the returned instance
   * @return the instance
   * @throws InvokeException if the class does not have such a constructor
   */
  @SuppressWarnings({"unchecked"})
  public static <T> T newInstance(Class<T> clazz, int arg0) throws InvokeException {
    try {
      return (T) getIntArgConstructor(clazz).invoke(arg0);
    } catch (NoSuchMethodException e) {
      throw InvokeException.noSuchConstructor(clazz, int.class);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  /**
   * Returns a new array with the specified length. Higher-level code must check that the provided class does
   * in fact represent an array type and that the specified length is not negative
   *
   * @param arrayType the array class
   * @param length the length of the outermost array
   * @return the array
   */
  public static Object newArray(Class<?> arrayType, int length) {
    MethodHandle mh = getArrayConstructor(arrayType);
    try {
      return mh.invoke(length);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  /**
   * Returns a new array with the specified type of elements and with the specified length. The provided type
   * may be an array type, but the returned array will then be an array of that array type. For example, if
   * the provided type is {@code String[][].class}, and the provided length is 100, then this method will
   * return {@code new String[100][][]}.
   *
   * @param elementType the type of the elements in the array.
   * @param length the length of the array
   * @return the array
   */
  public static Object arrayOf(Class<?> elementType, int length) {
    ArrayMetaData metadata = ArrayMetaData.of(elementType, 1);
    return newArray(metadata.getArrayClass(), length);
  }

  /**
   * Returns the length of the provided array. Higher-level code must check that the provided object is in
   * fact represent an array
   *
   * @param array the array
   * @return its length
   */
  public static int getArrayLength(Object array) {
    try {
      return (int) getArrayLengthGetter(array.getClass()).invoke(array);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  /**
   * Returns the array element at the specified index.
   *
   * @param array the array
   * @param index the array index
   * @param <T> the type of the array elements
   * @return the array element
   */
  @SuppressWarnings("unchecked")
  public static <T> T getArrayElement(Object array, int index) {
    try {
      return (T) getElementGetter(array.getClass()).invoke(array, index);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  /**
   * Sets the element at the specified index.
   *
   * @param array the array
   * @param idx the array index
   * @param value the value
   */
  public static void setArrayElement(Object array, int idx, Object value) {
    try {
      getElementSetter(array.getClass()).invoke(array, idx, value);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  private static <T> MethodHandle getNoArgConstructor(Class<T> clazz)
      throws NoSuchMethodException, IllegalAccessException {
    MethodHandle mh = noArgConstructors.get(clazz);
    if (mh == null) {
      mh = publicLookup().findConstructor(clazz, methodType(void.class));
      noArgConstructors.put(clazz, mh);
    }
    return mh;
  }

  private static <T> MethodHandle getIntArgConstructor(Class<T> clazz)
      throws NoSuchMethodException, IllegalAccessException {
    MethodHandle mh = intArgConstructors.get(clazz);
    if (mh == null) {
      mh = publicLookup().findConstructor(clazz, methodType(void.class, int.class));
      intArgConstructors.put(clazz, mh);
    }
    return mh;
  }

  private static MethodHandle getArrayConstructor(Class<?> clazz) {
    return arrayConstructors.computeIfAbsent(clazz, MethodHandles::arrayConstructor);
  }

  private static MethodHandle getArrayLengthGetter(Class<?> clazz) {
    return arrayLengthGetters.computeIfAbsent(clazz, MethodHandles::arrayLength);
  }

  private static MethodHandle getElementGetter(Class<?> clazz) {
    return elementGetters.computeIfAbsent(clazz, MethodHandles::arrayElementGetter);
  }

  private static MethodHandle getElementSetter(Class<?> clazz) {
    return elementSetters.computeIfAbsent(clazz, MethodHandles::arrayElementSetter);
  }

}
