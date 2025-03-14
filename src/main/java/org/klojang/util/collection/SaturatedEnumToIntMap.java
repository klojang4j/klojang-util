package org.klojang.util.collection;

import org.klojang.check.Check;
import org.klojang.check.extra.Emptyable;
import org.klojang.util.CollectionMethods;
import org.klojang.util.x.collection.ArraySet;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.AbstractMap.SimpleImmutableEntry;
import static org.klojang.check.CommonChecks.gt;
import static org.klojang.check.CommonProperties.length;
import static org.klojang.util.ObjectMethods.ifNull;

/**
 * An enum-to-int map. The map is backed by an int array with the same length as the number of constants in
 * the {@code enum} class. All constructors immediately saturate the map with all enum constants and there is
 * no way to remove keys from the map. These two preconditions make {@code SaturatedEnumToIntMap} just as
 * efficient as a regular {@link EnumMap}, with the added benefit that it obviates the need for unboxing and
 * boxing in the {@code get()} and {@code put()} operations.
 *
 * @param <K> the type of the enum class
 * @author Ayco Holleman
 * @see EnumToIntMap
 */
public final class SaturatedEnumToIntMap<K extends Enum<K>> implements Emptyable {

  private final Class<K> enumClass;
  private final int[] data;

  /**
   * Creates a {@code SaturatedEnumToIntMap}. The map's values are initialized to 0 (zero).
   *
   * @param enumClass the type of the enum class
   */
  public SaturatedEnumToIntMap(Class<K> enumClass) {
    this(enumClass, 0);
  }

  /**
   * Creates a {@code SaturatedEnumToIntMap}. The map's values are initialized to the specified initial
   * value.
   *
   * @param enumClass the type of the enum class
   * @param initialValue the initial value for all enum constants
   */
  public SaturatedEnumToIntMap(Class<K> enumClass, int initialValue) {
    this(enumClass, k -> initialValue);
  }

  /**
   * Creates an {@code EnumToIntMap} saturated with all enum constants of the specified enum class. The map's
   * values are initialized using the provided function, which takes an enum constant and returns the initial
   * value for the enum constant.
   *
   * @param enumClass the type of the enum class
   * @param initializer a function that initializes the map's values
   */
  public SaturatedEnumToIntMap(Class<K> enumClass, ToIntFunction<K> initializer) {
    Check.notNull(enumClass, "enum class");
    Check.notNull(initializer, "initializer");
    K[] keys = enumClass.getEnumConstants();
    Check.that(keys).has(length(), gt(), 0, "empty enums not supported");
    this.enumClass = enumClass;
    this.data = new int[keys.length];
    IntStream.range(0, keys.length).forEach(i -> data[i] = initializer.applyAsInt(keys[i]));
  }

  /**
   * Instantiates a new {@code EnumToIntMap} with the same key-value mappings as the specified
   * {@code EnumToIntMap}.
   *
   * @param other the {@code EnumToIntMap} whose key-value mappings to copy
   */
  public SaturatedEnumToIntMap(SaturatedEnumToIntMap<K> other) {
    Check.notNull(other);
    this.enumClass = other.enumClass;
    int len = other.enumClass.getEnumConstants().length;
    this.data = new int[len];
    System.arraycopy(other.data, 0, this.data, 0, len);
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
   * Always returns {@code true} (all enum constants will always be present in the map).
   *
   * @param key the enum constant
   * @return {@code true}
   * @see Map#containsKey(Object)
   */
  public boolean containsKey(K key) {
    return true;
  }

  /**
   * Returns {@code true} if this map contains the specified value.
   *
   * @param val the value to search for
   * @return whether the map contains the value
   * @see Map#containsValue(Object)
   */
  public boolean containsValue(int val) {
    return Arrays.stream(data).anyMatch(v -> v == val);
  }

  /**
   * Returns the value to which the specified enum constant is mapped.
   *
   * @param key the key whose associated value is to be returned
   * @return the value to which the specified key is mapped
   * @see Map#get(Object)
   */
  public int get(K key) {
    Check.notNull(key, "key");
    return data[key.ordinal()];
  }

  /**
   * Associates the specified value with the specified key in this map.
   *
   * @param key the key
   * @param val the value enum constant yet.
   * @see Map#put(Object, Object)
   */
  public int put(K key, int val) {
    int origVal = get(key);
    data[key.ordinal()] = val;
    return origVal;
  }

  /**
   * Like {@code put()}, but provides a fluent API for adding entries to the map.
   *
   * @param key the key
   * @param val the value
   * @return This instance
   */
  public SaturatedEnumToIntMap<K> set(K key, int val) {
    Check.notNull(key, "key");
    data[key.ordinal()] = val;
    return this;
  }

  /**
   * Increments the value currently associated with the specified key.
   *
   * @param key the key
   * @return the new value associated with the key
   */
  public int increment(K key) {
    Check.notNull(key, "key");
    int x = data[key.ordinal()] + 1;
    return data[key.ordinal()] = x;
  }

  /**
   * Adds the specified value to the value currently associated with the specified key.
   *
   * @param key the key
   * @param val the value to add to the value currently associated with the key
   * @return the new value associated with the key
   */
  public int add(K key, int val) {
    Check.notNull(key, "key");
    int x = data[key.ordinal()] + val;
    return data[key.ordinal()] = x;
  }

  /**
   * Multiplies the value currently associated with the specified key with the specified value.
   *
   * @param key the key
   * @param val the value to multiply with which to multiply the value currently associated with the key
   * @return the new value associated with the key
   */
  public int multiply(K key, int val) {
    Check.notNull(key, "key");
    int x = data[key.ordinal()] * val;
    return data[key.ordinal()] = x;
  }

  /**
   * Adds all entries of the specified map to this map, overwriting any previous values.
   *
   * @param other the {@code EnumToIntMap} whose key-value mappings to copy
   */
  public void putAll(SaturatedEnumToIntMap<K> other) {
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
   * Returns a {@link Set} view of the keys contained in this map. The {@code Set} maintains the order of the
   * enum constants, and can be iterated over quickly, but is inefficient for typical set operations such as
   * {@link Set#contains(Object) constains()}.
   *
   * @return a Set view of the keys contained in this map
   * @see Map#keySet()
   */
  public Set<K> keySet() {
    return ArraySet.of(enumClass.getEnumConstants(), true);
  }

  /**
   * Returns a {@code Collection} view of the values contained in this map.
   *
   * @return a {@code Collection} view of the values contained in this map
   * @see Map#values()
   */
  public Collection<Integer> values() {
    return Arrays.stream(data).boxed().toList();
  }

  /**
   * Returns an {@code IntList} containing the values of this map.
   *
   * @return an {@code IntList} containing the values of this map
   */
  public IntList intValues() {
    return IntList.ofElements(data);
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
   * Always returns {@code false}.
   *
   * @return {@code false}
   */
  @Override
  public boolean isEmpty() {
    return false;
  }

  /**
   * Returns the number of key-value mappings in this map.
   *
   * @return the number of key-value mappings in this map
   * @see Map#size()
   */
  public int size() {
    return data.length;
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
    } else if (obj instanceof SaturatedEnumToIntMap<?> that && enumClass == that.enumClass) {
      return Arrays.equals(this.data, that.data);
    }
    return false;
  }

  public int hashCode() {
    return Arrays.hashCode(data);
  }

  @Override
  public String toString() {
    return '[' + CollectionMethods.implode(entrySet()) + ']';
  }

  private Stream<K> streamKeys() {
    return Arrays.stream(enumClass.getEnumConstants());
  }

  private void assign(K key, int val) {
    data[key.ordinal()] = val;
  }

}
