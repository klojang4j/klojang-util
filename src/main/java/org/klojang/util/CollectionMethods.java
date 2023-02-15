package org.klojang.util;

import org.klojang.check.Check;
import org.klojang.check.aux.DuplicateValueException;
import org.klojang.check.fallible.FallibleFunction;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

import static java.util.Map.Entry;
import static java.util.Map.entry;
import static java.util.stream.Collectors.*;
import static org.klojang.check.CommonChecks.*;
import static org.klojang.check.CommonExceptions.duplicateKey;
import static org.klojang.check.CommonProperties.*;
import static org.klojang.check.Tag.*;
import static org.klojang.util.ArrayMethods.*;

/**
 * Methods extending the Java Collection framework.
 */
public final class CollectionMethods {

  static final String SEPARATOR = "separator";
  static final String CONVERTER = "converter";
  static final String STRINGIFIER = "stringifier";

  private CollectionMethods() {
    throw new UnsupportedOperationException();
  }

  private static final Map<Class<?>, Function<Object, List<?>>> listifiers = Map.of(
      int[].class, o -> ArrayMethods.asList((int[]) o),
      double[].class, o -> ArrayMethods.asList((double[]) o),
      long[].class, o -> ArrayMethods.asList((long[]) o),
      byte[].class, o -> ArrayMethods.asList((byte[]) o),
      char[].class, o -> ArrayMethods.asList((char[]) o),
      float[].class, o -> ArrayMethods.asList((float[]) o),
      short[].class, o -> ArrayMethods.asList((short[]) o),
      boolean[].class, o -> ArrayMethods.asList((boolean[]) o)
  );

  /**
   * <p>Converts the specified value to a {@code List}.
   *
   * <ul>
   *   <li>if the value already is a {@code List}, it is returned as-is.
   *   <li>if the value is a {@code Collection}, it is
   *      converted to an {@link ArrayList} using the standard Collections Framework
   *      conversion mechanism &#8212; by passing it to the constructor of
   *      {@code ArrayList}
   *   <li>if the value is an instance of {@code Object[]}, it is converted using
   *      {@link Arrays#asList(Object[]) Arrays.asList}
   *   <li>if the value is an array of a primitive type, it is converted using
   *      {@link ArrayMethods#asList(int[]) ArrayMethods.asList}
   *   <li>in any other case the value is converted using
   *      {@link Collections#singletonList(Object) Collections.singletonList}
   * </ul>
   *
   * <p>In other words, this method takes the shortest route to turn the value into
   * a {@code List} and there is no guarantee about what type of {@code List}
   * you get.
   *
   * @param value the value to convert
   * @return the value converted to a {@code List}
   * @see ArrayMethods#box(int[])
   * @see ArrayMethods#asList(int[])
   */
  @SuppressWarnings({"unchecked"})
  public static List<?> listify(Object value) {
    if (value != null) {
      if (value instanceof List<?> l) {
        return l;
      } else if (value instanceof Collection<?> c) {
        return new ArrayList<>(c);
      } else if (value instanceof Object[] o) {
        return Arrays.asList(o);
      }
      Function<Object, List<?>> fnc = listifiers.get(value.getClass());
      if (fnc != null) {
        return fnc.apply(value);
      }
    }
    return Collections.singletonList(value);
  }

  /**
   * Returns a fixed-size, mutable {@code List} with all elements initialized to the
   * specified value. The initialization value must not be {@code null}.
   *
   * @param size The desired size of the {@code List}
   * @param initVal The initial value of the list elements (must not be
   *     {@code null})
   * @param <E> the type of the elements
   * @return a fixed-size, mutable, initialized {@code List}
   */
  @SuppressWarnings({"unchecked"})
  public static <E> List<E> initializeList(int size, E initVal) {
    Check.that(size, SIZE).is(gte(), 0);
    Check.notNull(initVal, "initialization value");
    Object[] array = new Object[size];
    for (int i = 0; i < size; ++i) {
      array[i] = initVal;
    }
    return (List<E>) Arrays.asList(array);
  }

  /**
   * Returns a fixed-size, mutable {@code List} with all elements initialized to
   * values provided by a {@code Supplier}.
   *
   * @param size The desired size of the {@code List}
   * @param initValSupplier The supplier of the initial values
   * @param <E> the type of the elements
   * @return a fixed-size, mutable, initialized {@code List}
   */
  @SuppressWarnings({"unchecked"})
  public static <E> List<E> initializeList(int size, Supplier<E> initValSupplier) {
    Check.that(size, SIZE).is(gte(), 0);
    Check.notNull(initValSupplier, "supplier");
    Object[] array = new Object[size];
    for (int i = 0; i < size; ++i) {
      array[i] = initValSupplier.get();
    }
    return (List<E>) Arrays.asList(array);
  }

  /**
   * Returns a new {@link ArrayList} initialized with the specified values. The
   * values are allowed to be {@code null}. The initial capacity will always be at
   * least the length of the {@code initVals} array, whatever the value of the
   * {@code capacity} argument.
   *
   * @param capacity The initial capacity of the list
   * @param initVals The values to add to the list
   * @param <E> the type of the list elements
   * @return a new {@link ArrayList} initialized with the specified values.
   */
  @SafeVarargs
  public static <E> List<E> newArrayList(int capacity, E... initVals) {
    Check.that(capacity, CAPACITY).is(gte(), 0);
    Check.notNull(initVals, "initVals");
    if (initVals.length == 0) {
      return new ArrayList<>(Math.max(1, capacity));
    }
    List<E> l = new ArrayList<>(Math.max(capacity, initVals.length));
    l.addAll(Arrays.asList(initVals));
    return l;
  }

  /**
   * Returns a {@link HashMap} initialized with the specified key-value pairs. Both
   * keys and values are allowed to be {@code null}. Keys will be checked for
   * uniqueness.
   *
   * @param <K> the type of the keys
   * @param <V> the type of the values
   * @param size The expected number of map entries. No rehashing will take place
   *     until that number is reached. If you specify a number less than the number
   *     of key-value pairs (half the length of the varargs array), it will be taken
   *     as a multiplier. For example, 2 would mean that you expect the map to grow
   *     to about twice the specified number of key-value pairs.
   * @param keyClass The class of the keys
   * @param valueClass The class of the values
   * @param kvPairs An array alternating between keys and values
   * @return a {@code HashMap} initialized with the specified key-value pairs
   */
  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> newHashMap(
      int size, Class<K> keyClass, Class<V> valueClass, Object... kvPairs) {
    Check.that(size, CAPACITY).isNot(negative());
    Check.notNull(kvPairs, "kvPairs").has(length(), even());
    if (kvPairs.length == 0) {
      return new HashMap<>(Math.max(1, size));
    }
    int cap;
    if (size < kvPairs.length / 2) {
      cap = Math.max(1, size) * kvPairs.length;
    } else {
      cap = size;
    }
    HashMap<K, V> map = new HashMap<>(1 + cap * 4 / 3);
    for (int i = 0; i < kvPairs.length - 1; i += 2) {
      K key = (K) kvPairs[i];
      V val = (V) kvPairs[i + 1];
      Check.that(key).isNot(keyIn(),
          map,
          "duplicate key at position ${0}: ${arg}",
          i);
      if (key != null) {
        Check.that(key, "kvPairs[" + i + "]").is(instanceOf(), keyClass);
      }
      if (val != null) {
        Check.that(val, "kvPairs[" + (i + 1) + "]").is(instanceOf(), valueClass);
      }
      map.put(key, val);
    }
    return map;
  }

  /**
   * Returns an {@link EnumMap} with all enum constants set to non-null values. The
   * number of values must exactly equal the number of enum constants, and they are
   * assigned according to ordinal number. This method throws an
   * {@link IllegalArgumentException} if the number of values is not exactly equal to
   * the number of constants in the enum class, or if any of the values is null.
   *
   * @param <K> the key type
   * @param <V> the value type
   * @param enumClass The enum class
   * @param values The values to associate with the enum constants
   * @return a fully-occupied {@code EnumMap} with no null-values
   * @throws IllegalArgumentException if {@code enumClass} or {@code Values} is
   *     null, or if any of the provided values is null, or is the number of values
   *     is not exactly equals to the number of enum constants
   */
  @SuppressWarnings("unchecked")
  public static <K extends Enum<K>, V> EnumMap<K, V> saturatedEnumMap(
      Class<K> enumClass, V... values) throws IllegalArgumentException {
    K[] constants = Check.notNull(enumClass, "enumClass").ok().getEnumConstants();
    Check.notNull(values, VALUES).has(length(), eq(), constants.length);
    EnumMap<K, V> map = new EnumMap<>(enumClass);
    for (int i = 0; i < constants.length; ++i) {
      Check.that(values[i]).is(notNull(),
          "Illegal null value for key ${0}",
          constants[i]);
      map.put(constants[i], values[i]);
    }
    return map;
  }

  /**
   * Returns a sublist of the provided list starting with element {@code from} and
   * containing at most {@code length} elements. The returned list is backed by the
   * original list, so changing its elements will affect the original list as well.
   * If {@code offset} is negative, it is taken relative to the end of the list. If
   * {@code length} is negative, the sublist is taken in the opposite direction
   * &#8212; that is, the element at {@code offset} now becomes the <i>last</i>
   * element of the sublist.
   *
   * @param list the {@code List} to extract a sublist from
   * @param offset the start index if the sublist (however, see above)
   * @param length the length of the sublist
   * @param <T> the type of the elements
   * @return a sublist of the provided list
   */
  public static <T> List<T> sublist(List<T> list, int offset, int length) {
    Check.notNull(list, LIST);
    int sz = list.size();
    int from;
    if (offset < 0) {
      from = offset + sz;
      Check.that(from, OFFSET).is(gte(), 0);
    } else {
      from = offset;
      Check.that(from, OFFSET).is(lte(), sz);
    }
    int to;
    if (length >= 0) {
      to = from + length;
    } else {
      to = from + 1;
      from = to + length;
      Check.that(from, "effective from-index").is(gte(), 0);
    }
    Check.that(to, "effective to-index").is(lte(), sz);
    return list.subList(from, to);
  }

  /**
   * Returns a new {@code Map} where keys and values of the input map have traded
   * places. The specified {@code Map} must not contain duplicate values. An
   * {@link IllegalArgumentException} is thrown if it does. The returned map is
   * tightly sized, but modifiable.
   *
   * @param <K> the type of the keys in the original map, and of the values in
   *     the returned map
   * @param <V> the type of the values in the original map, and of the keys in
   *     the returned map
   * @param map The source map
   * @return a new {@code Map} where keys and values are swapped
   */
  public static <K, V> Map<V, K> swap(Map<K, V> map) {
    return swap(map, () -> new HashMap<>(1 + map.size() * 4 / 3));
  }

  /**
   * Returns a new {@code Map} where keys and values of the input map have traded
   * places. The specified {@code Map} must not contain duplicate values. A
   * {@link DuplicateValueException} is thrown if it does. {@code null} keys and
   * values are allowed, however.
   *
   * @param <K> the type of the keys in the original map, and of the values in
   *     the returned map
   * @param <V> the type of the values in the original map, and of the keys in
   *     the returned map
   * @param map The source map
   * @param mapFactory A supplier of a {@code Map} instance
   * @return a new {@code Map} where keys and values are swapped
   * @throws DuplicateValueException if the map contains duplicate values
   */
  public static <K, V> Map<V, K> swap(Map<K, V> map,
      Supplier<? extends Map<V, K>> mapFactory) throws DuplicateValueException {
    Check.notNull(map, MAP);
    Check.notNull(mapFactory, "mapFactory");
    Map<V, K> out = mapFactory.get();
    map.forEach((k, v) -> out.put(v, k));
    return Check.that(out).has(mapSize(), eq(), map.size(), duplicateKey()).ok();
  }

  /**
   * Returns an unmodifiable {@code Map} where keys and values of the input map have
   * traded places. The specified {@code Map} must not contain {@code null} keys,
   * {@code null} values or duplicate values. An {@link IllegalArgumentException} is
   * thrown if it does.
   *
   * @param <K> the type of the keys in the original map, and of the values in
   *     the returned map
   * @param <V> the type of the values in the original map, and of the keys in
   *     the returned map
   * @param map The source map
   * @return a new {@code Map} where keys and values are swapped
   */
  public static <K, V> Map<V, K> swapAndFreeze(Map<K, V> map) {
    Map<V, K> out = deepFreeze(map, e -> entry(e.getValue(), e.getKey()));
    return Check.that(out).has(mapSize(), eq(), map.size(), duplicateKey()).ok();
  }

  /**
   * Returns an unmodifiable {@code Map} where the values of the input {@code Map}
   * have been converted using the specified {@code Function}. The specified
   * {@code Map} must not contain {@code null} keys, {@code null} values or duplicate
   * values. An {@link IllegalArgumentException} is thrown if it does.
   *
   * @param <K> the type of the keys of the input and output {@code Map}
   * @param <V0> the type of the values of the input {@code Map}
   * @param <V1> the type of the values of the output {@code Map}
   * @param src the input {@code Map}
   * @param valueConverter A {@code Function} that converts the values of the
   *     input {@code Map}
   * @return an unmodifiable {@code Map} where the values of the input {@code Map}
   *     have been converted using the specified {@code Function}
   */
  public static <K, V0, V1> Map<K, V1> freeze(
      Map<K, V0> src, Function<? super V0, ? extends V1> valueConverter) {
    Check.notNull(src, MAP);
    Check.notNull(valueConverter, "value converter");
    return src.entrySet().stream()
        .peek(checkEntry())
        .map(toEntryConverter(valueConverter))
        .collect(toUnmodifiableMap(key(), value()));
  }

  /**
   * Returns an unmodifiable {@code Map} where the values of the input {@code Map}
   * have been converted using the specified {@code BiFunction}. This method passes
   * both the key and the value to the converter function so you can make the
   * conversion key-dependent, or so you can mention the key if the conversion
   * fails.
   *
   * @param <K> the type of the keys of the input and output {@code Map}
   * @param <V0> the type of the values of the input {@code Map}
   * @param <V1> the type of the values of the output {@code Map}
   * @param src the input {@code Map}
   * @param valueConverter A {@code Function} that converts the values of the
   *     input {@code Map}
   * @return an unmodifiable {@code Map} where the values of the input {@code Map}
   *     have been converted using the specified {@code Function}
   */
  public static <K, V0, V1> Map<K, V1> freeze(
      Map<K, V0> src,
      BiFunction<? super K, ? super V0, ? extends V1> valueConverter) {
    Check.notNull(src, MAP);
    Check.notNull(valueConverter, "value converter");
    return src.entrySet().stream()
        .peek(checkEntry())
        .map(toEntryConverter(valueConverter))
        .collect(toUnmodifiableMap(key(), value()));
  }

  /**
   * Returns an unmodifiable {@code Map} where both keys and values of the input
   * {@code Map} have been converted using the specified {@code Function}. The output
   * map may be smaller than the input map if the conversion function does not
   * generate unique keys.
   *
   * @param src the input {@code Map}
   * @param entryConverter a {@code Function} that produces a new entry from the
   *     original entry
   * @param <K0> The type of the keys in the input map
   * @param <V0> the type of the values in the input map
   * @param <K1> the type of the keys in the output map
   * @param <V1> the type of the values in the output map
   * @return an unmodifiable {@code Map} where the values of the input {@code Map}
   *     have been converted using the specified {@code Function}
   */
  public static <K0, V0, K1, V1> Map<K1, V1> deepFreeze(
      Map<K0, V0> src, Function<Entry<K0, V0>, Entry<K1, V1>> entryConverter) {
    Check.notNull(src, MAP);
    Check.notNull(entryConverter, "entry converter");
    Map<K1, V1> out = new HashMap<>(1 + src.size() * 4 / 3);
    src.entrySet().stream()
        .peek(checkEntry())
        .map(entryConverter)
        .forEach(e -> out.put(e.getKey(), e.getValue()));
    return Map.copyOf(out);
  }

  private static <K, V0, V1> Function<Entry<K, V0>, Entry<K, V1>> toEntryConverter(
      BiFunction<? super K, ? super V0, ? extends V1> fnc) {
    return e -> entry(e.getKey(), fnc.apply(e.getKey(), e.getValue()));
  }

  private static <K, V0, V1> Function<Entry<K, V0>, Entry<K, V1>> toEntryConverter(
      Function<? super V0, ? extends V1> fnc) {
    return e -> entry(e.getKey(), fnc.apply(e.getValue()));
  }

  private static <K, V> Consumer<Entry<K, V>> checkEntry() {
    return e ->
        Check.that(e)
            .has(key(), notNull(), "Illegal null key for value ${0}", e.getValue())
            .has(value(), notNull(), "Illegal null value for key ${0}", e.getKey());
  }

  /**
   * Returns an unmodifiable {@code List} containing the values that result from
   * applying the specified function to the source list's elements. The conversion
   * function is allowed to throw a checked exception.
   *
   * @param <T> the type of the elements in the source list
   * @param <U> the type of the elements in the returned list
   * @param <E> the type of exception thrown if the conversion fails
   * @param src the source list
   * @param converter the conversion function
   * @return an unmodifiable {@code List} containing the values that result from
   *     applying the specified function to the source list's elements
   * @throws E the exception potentially being thrown from the conversion
   *     function
   */
  @SuppressWarnings("unchecked")
  public static <T, U, E extends Throwable> List<U> freeze(
      List<? extends T> src, FallibleFunction<? super T, ? extends U, E> converter)
      throws E {
    Check.notNull(src, LIST);
    Check.notNull(converter, CONVERTER);
    Object[] objs = new Object[src.size()];
    for (int i = 0; i < src.size(); ++i) {
      objs[i] = converter.apply(src.get(i));
    }
    return (List<U>) List.of(objs);
  }

  /**
   * Returns an unmodifiable {@code Set} containing the values that result from
   * applying the specified function to the source set's elements. The conversion
   * function is allowed to throw a checked exception.
   *
   * @param <T> the type of the elements in the source set
   * @param <U> the type of the elements in the returned set
   * @param <E> the type of exception thrown if the conversion fails
   * @param src the source set
   * @param converter the conversion function
   * @return an unmodifiable {@code Set} containing the values that result from
   *     applying the
   * @throws E the exception potentially being thrown from the conversion
   *     function
   */
  @SuppressWarnings({"unchecked"})
  public static <T, U, E extends Throwable> Set<U> freeze(
      Set<? extends T> src, FallibleFunction<? super T, ? extends U, E> converter)
      throws E {
    Check.notNull(src, SET);
    Check.notNull(converter, CONVERTER);
    Object[] objs = new Object[src.size()];
    Iterator<? extends T> iterator = src.iterator();
    for (int i = 0; i < src.size(); ++i) {
      objs[i] = converter.apply(iterator.next());
    }
    return (Set<U>) Set.of(objs);
  }

  /**
   * Shortcut method. Returns an unmodifiable list using:
   *
   * <blockquote>
   *
   * <pre>{@code
   * src.stream().map(converter).collect(toUnmodifiableList());
   * }</pre>
   *
   * </blockquote>
   *
   * @param <T> the type of the elements in the source set
   * @param <U> the type of the elements in the returned list
   * @param src the source list
   * @param converter the conversion function
   * @return an unmodifiable {@code List} containing the values that result from
   *     applying the specified function to the source collection's elements
   */
  public static <T, U> List<U> collectionToList(
      Collection<? extends T> src, Function<? super T, ? extends U> converter) {
    Check.notNull(src, COLLECTION);
    Check.notNull(converter, CONVERTER);
    return src.stream().map(converter).collect(toUnmodifiableList());
  }

  /**
   * Shortcut method. Returns an unmodifiable set using:
   *
   * <blockquote>
   *
   * <pre>{@code
   * src.stream().map(converter).collect(toUnmodifiableSet());
   * }</pre>
   *
   * </blockquote>
   *
   * @param <T> the type of the elements in the source set
   * @param <U> the type of the elements in the returned list
   * @param src the source list
   * @param converter the conversion function
   * @return an unmodifiable {@code Set} containing the values that result from
   *     applying the specified function to the source collection's elements
   */
  public static <T, U> Set<U> collectionToSet(
      Collection<? extends T> src, Function<? super T, ? extends U> converter) {
    Check.notNull(src, COLLECTION);
    Check.notNull(converter, CONVERTER);
    return src.stream().map(converter).collect(toUnmodifiableSet());
  }

  /**
   * Shortcut method. Returns an unmodifiable map from the specified collection
   * using:
   *
   * <blockquote> <pre>{@code
   * src.stream().collect(toUnmodifiableMap(keyExtractor, Function.identity()))
   * }</pre> </blockquote>
   *
   * @param <K> the type of the keys
   * @param <V> the type of the values and the list elements
   * @param src the {@code List} to convert.
   * @param keyExtractor The key-extraction function
   * @return an unmodifiable {@code Map}
   */
  public static <K, V> Map<K, V> collectionToMap(
      Collection<V> src, Function<? super V, ? extends K> keyExtractor) {
    Check.notNull(src, COLLECTION);
    Check.notNull(keyExtractor, "key extractor");
    return src.stream().collect(toUnmodifiableMap(keyExtractor,
        Function.identity()));
  }

  /**
   * Shortcut method. Returns the first element that passes the specified test, or
   * else {@code null}. Shortcut for
   * {@code collection.stream().filter(test).findFirst().orElse(null)}.
   *
   * @param collection the collection to search
   * @param test the test
   * @param <T> the type of the elements in the collection
   * @return the first element that passes the specified test, or else {@code null}
   */
  public static <T> T findFirst(Collection<T> collection,
      Predicate<? super T> test) {
    return collection.stream().filter(test).findFirst().orElse(null);
  }

  /**
   * PHP-style implode method, concatenating the collection elements using ", "
   * (comma-space) as separator.
   *
   * @param collection the collection to implode
   * @param <T> the type of the elements
   * @return a concatenation of the elements in the collection.
   * @see ArrayMethods#implode(Object[])
   */
  public static <T> String implode(Collection<T> collection) {
    return implode(collection, IMPLODE_SEPARATOR);
  }

  /**
   * PHP-style implode method, concatenating the collection elements with the
   * specified separator.
   *
   * @param collection the collection to implode
   * @param separator the string used to separate the elements
   * @param <T> the type of the elements
   * @return a concatenation of the elements in the collection.
   * @see ArrayMethods#implode(Object[])
   * @see ArrayMethods#implode(Object[], String)
   */
  public static <T> String implode(Collection<T> collection, String separator) {
    Check.notNull(collection);
    return implode(collection, Objects::toString, separator, 0, -1);
  }

  /**
   * PHP-style implode method, concatenating at most {@code limit} collection
   * elements using ", " (comma-space) as separator.
   *
   * @param collection the collection to implode
   * @param limit The maximum number of elements to collect. Specify -1 for no
   *     maximum. Specifying a number greater than the length of the collection is
   *     OK. It will be clamped to the collection length.
   * @param <T> the type of the elements
   * @return a concatenation of the elements in the collection.
   * @see ArrayMethods#implode(Object[])
   * @see ArrayMethods#implode(Object[], int)
   */
  public static <T> String implode(Collection<T> collection, int limit) {
    return implode(collection, IMPLODE_SEPARATOR, limit);
  }

  /**
   * PHP-style implode method, concatenating at most {@code limit} collection
   * elements using ", " (comma-space) as separator.
   *
   * @param collection the collection to implode
   * @param stringifier a {@code Function} that converts the collection elements
   *     to strings
   * @param <T> the type of the elements
   * @return a concatenation of the elements in the collection.
   * @see ArrayMethods#implode(Object[])
   * @see ArrayMethods#implode(Object[], Function)
   */
  public static <T> String implode(Collection<T> collection,
      Function<T, String> stringifier) {
    return implode(collection, stringifier, IMPLODE_SEPARATOR, 0, -1);
  }

  /**
   * PHP-style implode method, concatenating at most {@code limit} collection
   * elements using the specified separator.
   *
   * @param collection the collection to implode
   * @param separator the string used to separate the elements
   * @param limit The maximum number of elements to collect. Specify -1 for no
   *     maximum. Specifying a number greater than the length of the collection is
   *     OK. It will be clamped to the collection length.
   * @param <T> the type of the elements
   * @return a concatenation of the elements in the collection.
   * @see ArrayMethods#implode(Object[])
   * @see ArrayMethods#implode(Object[], String, int)
   */
  public static <T> String implode(Collection<T> collection,
      String separator,
      int limit) {
    return implode(collection, Objects::toString, separator, 0, limit);
  }

  /**
   * PHP-style implode method.
   *
   * @param collection the collection to implode
   * @param stringifier a {@code Function} that converts the collection elements
   *     to strings
   * @param separator the string used to separate the elements
   * @param from the index of the element to begin the concatenation with
   *     (inclusive)
   * @param to the index of the element to end the concatenation with
   *     (exclusive). The specified number will be clamped to
   *     {@code collection.size()} (i.e. it's OK to specify a number greater than
   *     {@code collection.size()}). You can specify -1 as a shorthand for
   *     {@code collection.size()}.
   * @param <T> the type of the elements
   * @return a concatenation of the elements in the collection.
   * @see ArrayMethods#implode(Object[])
   * @see ArrayMethods#implode(Object[], Function, String, int, int)
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static <T> String implode(
      Collection<T> collection,
      Function<T, String> stringifier,
      String separator,
      int from,
      int to) {
    int sz = Check.notNull(collection, COLLECTION).ok(Collection::size);
    Check.notNull(stringifier, STRINGIFIER);
    Check.notNull(separator, SEPARATOR);
    Check.that(from, FROM_INDEX).is(gte(), 0).is(lte(), sz);
    int x = to == -1 ? sz : Math.min(to, sz);
    Check.that(x, "to").is(gte(), from);
    if (from == 0) {
      Stream<T> stream = x == sz
          ? collection.stream()
          : collection.stream().limit(x);
      return stream.map(stringifier).collect(joining(separator));
    } else if (collection instanceof List) {
      List<T> sublist = ((List<T>) collection).subList(from, x);
      return sublist.stream().map(stringifier).collect(joining(separator));
    }
    Stream stream = Arrays.stream(collection.toArray(), from, x);
    return (String) stream.map(stringifier).collect(joining(separator));
  }

  private static final Set<Class> NULL_REPELLERS =
      // Actually, List.of(1) and List.of(1, 2) currently have the same type, but
      // better safe than sorry. They will anyhow be de-duplicated when entering
      // the HashSet
      Set.copyOf(new HashSet<>(
          Arrays.asList(
              Collections.emptyList().getClass(),
              Collections.emptySet().getClass(),
              List.of().getClass(),
              List.of(1).getClass(),
              List.of(1, 2).getClass(),
              List.of(1, 2, 3).getClass(),
              Set.of().getClass(),
              Set.of(1).getClass(),
              Set.of(1, 2).getClass(),
              Set.of(1, 2, 3).getClass())));

  /**
   * Returns {@code true} if the provided collection is a null-repellent collection
   * like those obtained via {@code List.of(...)} and {@code Set.of(...)}. Note that
   * if this method returns {@code false}, it does not mean that the collection is
   * <i>not</i> null-repellent. The only sure thing is that if this method returns
   * {@code true}, the collection is guaranteed not to contain {@code null} values.
   *
   * @param c the collection to inspect
   * @return {@code true} if the provided collection is a null-repellent collection
   */
  public static boolean isNullRepellent(Collection<?> c) {
    return NULL_REPELLERS.contains(c.getClass());
  }

}
