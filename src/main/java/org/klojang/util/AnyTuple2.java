package org.klojang.util;

import org.klojang.check.aux.Emptyable;

import java.util.Map;

/**
 * Generic 2-tuple of arbitrarily typed objects, both of which are allowed to be
 * {@code null}.
 *
 * @param first The first component of the 2-tuple
 * @param second The second component of the 2-tuple
 * @param <T> The type of the first component
 * @param <U> The type of the second component
 * @see Tuple2
 */
public record AnyTuple2<T, U>(T first, U second) implements Emptyable {

  /**
   * Returns a {@code AnyTuple2} consisting of the specified components.
   *
   * @param first The first component of the 2-tuple
   * @param second The second component of the 2-tuple
   * @param <T> The type of the first component
   * @param <U> The type of the second component
   * @return A {@code Tuple2} instance containing the specified values
   */
  public static <T, U> AnyTuple2<T, U> of(T first, U second) {
    return new AnyTuple2<>(first, second);
  }

  /**
   * Converts this instance to an immutable {@code Map} entry.
   *
   * @return an immutable {@code Map} entry
   */
  public Map.Entry<T, U> toEntry() {
    return Map.entry(first, second);
  }

  /**
   * Returns {@code true} if both components are {@code null}, otherwise
   * {@code false}.
   *
   * @return {@code true} if both components are {@code null}
   */
  @Override
  public boolean isEmpty() {
    return first == null && second == null;
  }

  /**
   * Returns {@code true} if both components are recursively non-empty as per
   * {@link ObjectMethods#isDeepNotEmpty(Object)}.
   *
   * @return {@code true} if both components are recursively non-empty
   */
  @Override
  public boolean isDeepNotEmpty() {
    return ObjectMethods.isDeepNotEmpty(first)
        && ObjectMethods.isDeepNotEmpty(second);
  }

}
