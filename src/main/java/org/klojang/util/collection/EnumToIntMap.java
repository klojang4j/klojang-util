package org.klojang.util.collection;

import org.klojang.check.Check;
import org.klojang.check.extra.Emptyable;
import org.klojang.util.ClassMethods;
import org.klojang.util.CollectionMethods;
import org.klojang.util.x.collection.ArraySet;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.AbstractMap.SimpleImmutableEntry;
import static org.klojang.check.CommonChecks.gt;
import static org.klojang.util.ObjectMethods.ifNull;

/**
 * An enum-to-int map. The map is backed by an int array with the same length as the number of constants in
 * the {@code enum} class. Although {@code EnumToIntMap} loosely mimics the behaviour and interface of the
 * {@link Map} interface, it obviously does not, and cannot implement the {@code Map} interface. Some methods
 * <i>need</i> to behave differently due to the fact that the type of the values in the map is a primitive
 * type ({@code int}).
 *
 * @param <K> the type of the enum class
 * @author Ayco Holleman
 * @see SaturatedEnumToIntMap
 */
public final class EnumToIntMap<K extends Enum<K>> implements Emptyable {

  private final Class<K> enumClass;
  private final int[] data;
  private final boolean[] isNull;

  /**
   * Creates an empty {@code EnumToIntMap} for the specified {@code enum} class.
   *
   * @param enumClass the type of the enum class
   */
  public EnumToIntMap(Class<K> enumClass) {
    Check.notNull(enumClass, "enum class");
    int len = enumClass.getEnumConstants().length;
    Check.that(len).is(gt(), 0, "empty enums not supported");
    this.enumClass = enumClass;
    this.data = new int[len];
    this.isNull = new boolean[len];
    Arrays.fill(isNull, true);
  }

  /**
   * Creates an {@code EnumToIntMap} saturated with all enum constants of the specified enum class. The map's
   * values are initialized to the specified initial value.
   *
   * @param enumClass the type of the enum class
   * @param initialValue the initial value for all enum constants
   */
  public EnumToIntMap(Class<K> enumClass, int initialValue) {
    this(enumClass, k -> initialValue);
  }

  /**
   * Creates an {@code EnumToIntMap} saturated with all enum constants of the specified enum class. The map's
   * values are initialized using the provided function, which takes an enum constant and returns the initial
   * value for the enum constant.
   *
   * @param enumClass the type of the enum class
   * @param initializer a function called to initialize the map's values
   */
  public EnumToIntMap(Class<K> enumClass, ToIntFunction<K> initializer) {
    Check.notNull(enumClass, "enum class");
    Check.notNull(initializer, "initializer");
    K[] keys = enumClass.getEnumConstants();
    int len = keys.length;
    Check.that(len).is(gt(), 0, "empty enums not supported");
    this.enumClass = enumClass;
    this.data = new int[len];
    this.isNull = new boolean[len];
    IntStream.range(0, len).forEach(i -> data[i] = initializer.applyAsInt(keys[i]));
    // no need to fill nulls array
  }

  /**
   * Instantiates a new {@code EnumToIntMap} with the same key-value mappings as the specified
   * {@code EnumToIntMap}.
   *
   * @param other the {@code EnumToIntMap} whose key-value mappings to copy
   */
  public EnumToIntMap(EnumToIntMap<K> other) {
    Check.notNull(other);
    this.enumClass = other.enumClass;
    int len = other.data.length;
    this.data = new int[len];
    this.isNull = new boolean[len];
    System.arraycopy(other.data, 0, this.data, 0, len);
    System.arraycopy(other.isNull, 0, this.isNull, 0, len);
  }

  /**
   * Returns the type of the keys in this map.
   *
   * @return the type of the keys in this map
   */
  public Class<K> keyType() {
    return enumClass;
  }

  /**
   * Returns {@code true} if this map contains a mapping for the specified key.
   *
   * @param key the enum constant
   * @return whether the map contains an entry for the enum constant
   * @see Map#containsKey(Object)
   */
  public boolean containsKey(K key) {
    Check.notNull(key, "key");
    return !isNull[key.ordinal()];
  }

  /**
   * Returns {@code true} if this map contains the specified value.
   *
   * @param val the value to search for
   * @return whether the map contains the value
   * @see Map#containsValue(Object)
   */
  public boolean containsValue(int val) {
    return streamValues().anyMatch(v -> v == val);
  }

  /**
   * Returns an {@code OptionalInt} containing the value to which the specified enum constant is mapped, or an
   * empty {@code OptionalInt} if this map contains no mapping for the key. (A regular {@code Map} would
   * return {@code null} in the latter case.)
   *
   * @param key the key whose associated value is to be returned
   * @return the value to which the specified key is mapped
   * @see Map#get(Object)
   */
  public OptionalInt get(K key) {
    Check.notNull(key, "key");
    return isNull[key.ordinal()]
        ? OptionalInt.empty()
        : OptionalInt.of(data[key.ordinal()]);
  }

  /**
   * Returns the value associated with the specified enum constant or {@code dfault} if the map did not
   * contain an entry for the specified enum constant.
   *
   * @param key the key to retrieve the value of.
   * @param dfault the integer to return if the map did not contain the key
   * @return the value associated with the key or {@code dfault}
   * @see Map#getOrDefault(Object, Object)
   */
  public int getOrDefault(K key, int dfault) {
    return get(key).orElse(dfault);
  }

  /**
   * Computes a new value for the specified key if present, else associates the key with the specified initial
   * value.
   *
   * @param key the key
   * @param operator the function to apply to the value currently associated with the key
   * @param initialValue the value to associate the key with if the key was not present in the map yet
   * @return the new value
   */
  public int computeIfPresent(K key, IntUnaryOperator operator, int initialValue) {
    Check.notNull(key, "key");
    Check.notNull(operator, "compute");
    int i = key.ordinal();
    if (isNull[i]) {
      isNull[i] = false;
      return data[i] = initialValue;
    }
    return data[i] = operator.applyAsInt(data[i]);
  }

  /**
   * Associates the specified value with the specified key in this map.
   *
   * @param key the key
   * @param val the value enum constant yet.
   * @see Map#put(Object, Object)
   */
  public OptionalInt put(K key, int val) {
    OptionalInt old = get(key);
    assign(key, val);
    return old;
  }

  /**
   * Like {@code put()}, but provides a fluent API for adding entries to the map.
   *
   * @param key the key
   * @param val the value
   * @return this instance
   */
  public EnumToIntMap<K> set(K key, int val) {
    Check.notNull(key, "key");
    assign(key, val);
    return this;
  }

  /**
   * Adds all entries of the specified map to this map, overwriting any previous values. The source map must
   * not contain the <i>key-absent-value</i> of this map. An {@link IllegalArgumentException} is thrown if it
   * does.
   *
   * @param other the {@code EnumToIntMap} whose key-value mappings to copy
   */
  public void putAll(EnumToIntMap<K> other) {
    Check.notNull(other);
    other.forEach(this::assign);
  }

  /**
   * Adds all entries of the specified map to this map. This method acts as a bridge to fully-generic map
   * implementations.
   *
   * @param other the {@code Map} whose key-value mappings to copy
   * @param nullValue the {@code int} value to use for {@code null} values in the source map
   */
  public void putAll(Map<K, Integer> other, int nullValue) {
    Check.notNull(other);
    other.forEach((k, v) -> assign(k, ifNull(v, nullValue)));
  }

  /**
   * Converts this map to a fully-generic version of this map.
   *
   * @return a fully-generic version of this map
   */
  public EnumMap<K, Integer> toGenericMap() {
    EnumMap<K, Integer> map = new EnumMap<>(enumClass);
    streamKeys().forEach(k -> map.put(k, data[k.ordinal()]));
    return map;
  }

  /**
   * Removes the mapping for a key from this map if it is present.
   *
   * @param key the key
   * @return an {@code OptionalInt} containing the previous value associated with key, or an empty
   *     {@code OptionalInt} if there was no mapping for key.
   * @see Map#remove(Object)
   */
  public OptionalInt remove(K key) {
    Check.notNull(key, "key");
    if (!isNull[key.ordinal()]) {
      isNull[key.ordinal()] = true;
      return OptionalInt.of(data[key.ordinal()]);
    }
    return OptionalInt.empty();
  }

  /**
   * Returns a {@link Set} view of the keys contained in this map. The {@code Set} maintains the order of the
   * enum constants, and can be iterated over quickly, but is inefficient for typical set operations such as
   * {@link Set#contains(Object) constains()}.
   *
   * @return a Set view of the keys contained in this map
   * @see Map#keySet()
   */
  public Set<K> keySet() {
    return ClassMethods.cast(ArraySet.of(streamKeys().toArray(), true));
  }

  /**
   * Returns a {@code Collection} view of the values contained in this map.
   *
   * @return a {@code Collection} view of the values contained in this map
   * @see Map#values()
   */
  public Collection<Integer> values() {
    return streamValues().boxed().toList();
  }

  /**
   * Returns an {@code IntList} containing the values of this map.
   *
   * @return an {@code IntList} containing the values of this map
   */
  public IntList intValues() {
    return IntList.of(streamValues().toArray());
  }

  /**
   * Returns a Set view of the mappings contained in this map.
   *
   * @return a set view of the mappings contained in this map
   * @see Map#entrySet()
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Set<Map.Entry<K, Integer>> entrySet() {
    SimpleImmutableEntry[] entries = streamKeys()
        .map(k -> new SimpleImmutableEntry(k, data[k.ordinal()]))
        .toArray(SimpleImmutableEntry[]::new);
    return ArraySet.of(entries, true);
  }

  /**
   * Returns {@code true} if this map contains no key-value mappings, {@code false} otherwise.
   *
   * @return {@code true} if this map contains no key-value mappings, {@code false} otherwise
   * @see Map#isEmpty()
   */
  public boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Performs the given action for each entry in this map until all entries have been processed or the action
   * throws an exception.
   *
   * @param action the action to be performed for each entry
   * @see Map#forEach(BiConsumer)
   */
  public void forEach(ObjIntConsumer<K> action) {
    streamKeys().forEach(k -> action.accept(k, data[k.ordinal()]));
  }

  /**
   * Removes all mappings from this map.
   *
   * @see Map#clear()
   */
  public void clear() {
    Arrays.fill(isNull, true);
  }

  /**
   * Returns the number of key-value mappings in this map.
   *
   * @return the number of key-value mappings in this map
   * @see Map#size()
   */
  public int size() {
    return (int) streamKeys().count();
  }

  /**
   * Returns {@code true} if the argument is an {@code EnumToIntMap} for the same enum class and if it
   * contains the same key-value mappings. The two maps need
   * <i>not</i> have the same <i>key-absent-value</i> value.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof EnumToIntMap<?> that && enumClass == that.enumClass) {
      return Arrays.equals(this.isNull, that.isNull) && Arrays.equals(this.data, that.data);
    }
    return false;
  }

  public int hashCode() {
    return Objects.hash(Arrays.hashCode(isNull), Arrays.hashCode(data));
  }

  @Override
  public String toString() {
    return '[' + CollectionMethods.implode(entrySet()) + ']';
  }

  private IntStream streamValues() {
    return IntStream.range(0, data.length)
        .filter(i -> !isNull[i])
        .map(i -> data[i]);
  }

  private Stream<K> streamKeys() {
    return Arrays.stream(enumClass.getEnumConstants()).filter(k -> !isNull[k.ordinal()]);
  }

  private void assign(K key, int val) {
    data[key.ordinal()] = val;
    isNull[key.ordinal()] = false;
  }

}
