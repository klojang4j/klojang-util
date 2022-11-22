package org.klojang.util;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

/**
 * Dynamic invocation utility methods. <i>These methods are not meant to be used in
 * application-level software.</i> They very thinly wrap methods from
 * {@code java.lang.invoke} and don't perform any null checks, type checks, range
 * checks, etc.
 */
public final class InvokeMethods {

  private static final Map<Class<?>, MethodHandle> noArgConstructors = new HashMap<>();
  private static final Map<Class<?>, MethodHandle> intArgConstructors = new HashMap<>();

  /**
   * Returns a new instance of the specified class using its no-arg constructor.
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
   * Returns a new instance of the specified class using the constructor that takes a
   * single {@code int} argument.
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
   * Returns a new array with the specified length.
   *
   * @param arrayType the array class (not the class of its elements!)
   * @param length the desired length of the array
   * @return the array
   */
  public static Object newArray(Class<?> arrayType, int length) {
    MethodHandle mh = arrayConstructor(arrayType);
    try {
      return mh.invoke(length);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

  /**
   * Returns the length of the provided array.
   *
   * @param array the array
   * @return its length
   */
  public static int getArrayLength(Object array) {
    try {
      return (int) arrayLength(array.getClass()).invoke(array);
    } catch (Throwable t) {
      throw InvokeException.arrayOperationFailed(array, t);
    }
  }

  /**
   * Returns the array element at the specified index.
   *
   * @param array the array
   * @param idx the array index
   * @param <T> the type of the array elements
   * @return the array element
   */
  @SuppressWarnings("unchecked")
  public static <T> T getArrayElement(Object array, int idx) {
    try {
      return (T) arrayElementGetter(array.getClass()).invoke(array, idx);
    } catch (Throwable t) {
      throw InvokeException.arrayOperationFailed(array, t);
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
      arrayElementSetter(array.getClass()).invoke(array, idx, value);
    } catch (Throwable t) {
      throw InvokeException.arrayOperationFailed(array, t);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static void copyArrayElements(Object fromArray,
      Collection<?> toCollection) {
    try {
      int len = (int) arrayLength(fromArray.getClass()).invoke(fromArray);
      if (len != 0) {
        MethodHandle mh = arrayElementGetter(fromArray.getClass());
        for (int i = 0; i < len; ++i) {
          ((Collection) toCollection).add(mh.invoke(fromArray, i));
        }
      }
    } catch (Throwable t) {
      throw InvokeException.arrayOperationFailed(fromArray, t);
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

}
