package org.klojang.util;

import org.klojang.check.Check;
import org.klojang.check.Tag;
import org.klojang.check.extra.Result;

import java.util.*;

import static org.klojang.check.CommonChecks.*;
import static org.klojang.check.CommonExceptions.illegalState;
import static org.klojang.check.Tag.VALUE;
import static org.klojang.util.ObjectMethods.ifNull;
import static org.klojang.util.StringMethods.EMPTY_STRING;

/**
 * <p>An elaborate reader/writer class for Map&lt;String, Object&gt; (map-in-map) pseudo-objects. A
 * {@code JSONObject} lets you read/write deeply nested values using JSON path strings (e.g.
 * {@code person.address.street}). When writing deeply nested values, intermediate maps are created as and
 * when necessary. Map keys must be non-null strings. Map values can be anything <i>except</i> {@code Map}
 * objects. {@code null} values are allowed, however.
 *
 * <p>Note that, notwithstanding its name, this class does not require that you use it within the context of
 * JSON serialization or deserialization. It is more akin to a {@code MapDecorator}.
 *
 * <p><b>Example 1 (writing):</b>
 *
 * <blockquote><pre>{@code
 * Map<String, Object> map = JSONObject.empty()
 *     .set("person.address.street", "12 Revolutionary Rd.")
 *     .set("person.address.state", "CA")
 *     .set("person.firstName", "John")
 *     .set("person.lastName", "Smith")
 *     .set("person.dateOfBirth", LocalDate.of(1967, 4, 4));
 *     .build();
 * }</pre></blockquote>
 *
 * <p><b>Example 2 (writing):</b>
 *
 * <blockquote><pre>{@code
 * Map<String, Object> map = JSONObject.empty()
 *     .in("person")
 *         .set("firstName", "John")
 *         .set("lastName", "Smith")
 *         .set("dateOfBirth", LocalDate.of(1967, 4, 4))
 *         .in("address")
 *             .set("street", "12 Revolutionary Rd.")
 *             .set("state", "CA")
 *             .backTo("person")
 *         .in("medicalStatus")
 *             .set("allergies", List.of("peanuts"))
 *             .set("smoker", true)
 *             .set("prescriptions", null)
 *     .build();
 * }</pre></blockquote>
 *
 * <p><b>Example 3 (reading):</b>
 *
 * <blockquote><pre>{@code
 * String street = JSONObject.of(someMap).get("person.address.street");
 * }</pre></blockquote>
 *
 * <p>For more flexibility, use the
 * <a href="https://klojang4j.github.io/klojang-invoke/24/api/org.klojang.invoke/org/klojang/path/PathWalker.html">PathWalker</a> class of the klojang-invoke library.
 *
 * @author Ayco Holleman
 */
public final class JSONObject {

  private static final String NO_SUCH_PARENT = "Parent of \"${0}\" is not \"${arg}\". Expected: \"${obj}\".";

  /**
   * Thrown when trying to write to a path that has already been set, or that extends beyond a path segment
   * with a terminal value (anything other than a map).
   *
   * @author Ayco Holleman
   */
  public static final class PathBlockedException extends IllegalArgumentException {

    private PathBlockedException(Path path, Object value) {
      super(createMessage(path, value));
    }

    private PathBlockedException(String msg) {
      super(msg);
    }

    private static String createMessage(Path path, Object value) {
      return "path \"%s\" blocked by terminal value [%s]".formatted(path, value);
    }

  }

  /*
   * When setting a path, or when processing the source map passed to the constructor, we replace null with
   * this value. This way, if Map.get(key) returns null, we know for sure that the map does not contain the
   * key. No need to follow it up with a containsKey call. On its way out, NULL is replaced again with null.
   */
  private static final Object NULL = new Object();


  /**
   * Creates a new {@code JSONObject}.
   *
   * @return a new {@code JSONObject}
   */
  public static JSONObject empty() {
    return new JSONObject();
  }

  /**
   * Creates a {@code JSONObject} that starts out with the entries in the specified map. The provided map is
   * read, but not modified.
   *
   * @param map the initial {@code Map}
   * @return a {@code JSONObject} that starts out with the entries in the specified map
   */
  public static JSONObject of(Map<String, Object> map) {
    Check.notNull(map);
    return new JSONObject(map);
  }

  private final Map<String, Object> map;
  private final Path root;
  private final JSONObject parent;

  private JSONObject() {
    this(new LinkedHashMap<>());
  }

  /**
   * Creates a {@code JSONObject} that starts out with the entries in the specified map. The map is read, but
   * not modified.
   *
   * @param map the initial {@code Map}
   */
  private JSONObject(Map<String, Object> map) {
    this.map = LinkedHashMap.newLinkedHashMap(map.size() + 10);
    this.root = Path.empty();
    this.parent = null;
    init(this, map);
  }

  private JSONObject(Path root, JSONObject parent) {
    this.root = root;
    this.map = new LinkedHashMap<>();
    this.parent = parent;
  }

  /**
   * <p>Sets the specified path to the specified value. It is not allowed to
   * overwrite the value of a path that has already been set, even if set to {@code null}. If necessary, use
   * {@link #unset(String)} to unset the path's value first.
   *
   * <p>The value can be anything except a {@code Map} or {@code JSONObject}. Use the
   * {@link #in(String) in()} method to create a new map at the specified path. It is allowed to set a path's
   * value to {@code null}.
   *
   * @param path the path at which to write the value
   * @param value the value
   * @return this {@code JSONObject}
   */
  public JSONObject set(String path, Object value) {
    return set(Path.from(path), value);
  }


  /**
   * Sets the specified key to the specified value. The provided key will not be interpreted as a JSON path.
   * This is useful if the key contains one or more dot characters.
   *
   * @param key the key
   * @param value the value
   * @return this {@code JSONObject}
   */
  public JSONObject setPlain(String key, Object value) {
    Check.notNull(key, Tag.PATH);
    return set(Path.of(key), value);
  }

  /**
   * <p>Sets the specified path to the specified value. It is not allowed to
   * overwrite the value of a path that has already been set, even if set to {@code null}. If necessary, use
   * {@link #unset(String)} to unset the path's value first.
   *
   * <p>The value can be anything except a {@code Map} or {@code JSONObject}. Use the
   * {@link #in(String) in()} method to create a new map at the specified path. It is allowed to set a path's
   * value to {@code null}.
   *
   * @param path the path at which to write the value
   * @param value the value
   * @return this {@code JSONObject}
   */
  public JSONObject set(Path path, Object value) {
    Check.notNull(path, Tag.PATH);
    set(this, path, value);
    return this;
  }

  /**
   * <p>Appends the specified element to the {@code Collection} found at the
   * specified path. If the path has not been set yet, it will first be set to an {@link ArrayList}, to which
   * the element will then be added. If the path is already set to a non-{@code Collection} type, a
   * {@link PathBlockedException} is thrown.
   *
   * @param path the path
   * @param element the element to add to the collection found or created at the specified path
   * @return this {@code JSONObject}
   */
  public JSONObject addElement(String path, Object element) {
    return addElement(Path.from(path), element);
  }

  /**
   * <p>Appends the specified element to the {@code Collection} found at the
   * specified path. If the path has not been set yet, it will first be set to an {@link ArrayList}, to which
   * the element will then be added. If the path is already set to a non-{@code Collection} type, a
   * {@link PathBlockedException} is thrown.
   *
   * @param path the path
   * @param element the element to add to the collection found or created at the specified path
   * @return this {@code JSONObject}
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public JSONObject addElement(Path path, Object element) {
    Check.notNull(path, Tag.PATH);
    Result<Object> result = get(path);
    if (result.isAvailable()) {
      Object obj = result.get();
      if (obj instanceof Collection c) {
        c.add(element);
      } else {
        String fmt = "\"%s\" must be a collection (was %s)";
        String msg = fmt.formatted(path, obj.getClass());
        throw new PathBlockedException(msg);
      }
    } else {
      List<Object> list = new ArrayList<>();
      list.add(element);
      set(path, list);
    }
    return this;
  }

  /**
   * Returns a {@link Result} object containing the value of the specified path, or
   * {@link Result#notAvailable} if the path is not set.
   *
   * @param path the path
   * @return a {@link Result} object containing the value of the specified path, or
   *     {@link Result#notAvailable} if the path is not set
   * @see #isSet(String)
   */
  public <T> Result<T> get(String path) {
    return get(Path.from(path));
  }

  /**
   * Returns a {@link Result} object containing the value of the specified path, or
   * {@link Result#notAvailable} if the path is not set. The provided key will not be interpreted as a JSON
   * path. This is useful if the key contains one or more dot characters.
   *
   * @param key the path
   * @return a {@link Result} object containing the value of the specified path, or
   *     {@link Result#notAvailable} if the path is not set
   * @see #isSet(String)
   */
  public <T> Result<T> getPlain(String key) {
    Check.notNull(key, Tag.PATH);
    return get(Path.of(key));
  }

  /**
   * Returns a {@link Result} object containing the value of the specified path, or
   * {@link Result#notAvailable} if the path is not set.
   *
   * @param path the path
   * @return a {@link Result} object containing the value of the specified path, or
   *     {@link Result#notAvailable} if the path is not set
   * @see #isSet(String)
   */
  @SuppressWarnings("unchecked")
  public <T> Result<T> get(Path path) {
    Check.notNull(path, Tag.PATH);
    return (Result<T>) poll(this, path);
  }

  /**
   * Returns a {@code JSONObject} for the map at the specified path. Once this method has been called, <i>all
   * subsequently specified paths</i> (including for subsequent calls to {@code in()}) are taken relative to
   * the specified path. If there is no map yet at the specified path, it will be created. Ancestral maps will
   * be created as and when needed. If any of the segments in the path (including the last segment) has
   * already been set, a {@link PathBlockedException} is thrown.
   *
   * @param path the path to be used as the base path. The path will itself be interpreted as relative to
   *     the <i>current</i> base path
   * @return a {@code JSONObject} for the map found or created at the specified path
   */
  public JSONObject in(String path) {
    return in(Path.from(path));
  }

  /**
   * Returns a {@code JSONObject} for the map at the specified path. Once this method has been called, <i>all
   * subsequently specified paths</i> (including for subsequent calls to {@code in()}) are taken relative to
   * the specified path. If there is no map yet at the specified path, it will be created. Ancestral maps will
   * be created as and when needed. If any of the segments in the path (including the last segment) has
   * already been set, a {@link PathBlockedException} is thrown.
   *
   * @param path the path to be used as the base path. The path will itself be interpreted as relative to
   *     the <i>current</i> base path
   * @return a {@code JSONObject} for the map found or created at the specified path
   */
  public JSONObject in(Path path) {
    Check.notNull(path, Tag.PATH);
    return in(this, path);
  }

  /**
   * Jumps to another branch in the tree of nested maps. The difference between {@code jump} and
   * {@link #in(String) in} is that the path passed to {@code jump} is always taken as an absolute path (i.e.
   * relative to the root map), while the path passed to {@code in} is taken relative to the path(s) passed to
   * previous calls to {@code in} and {@code jump}.
   *
   * @param path the absolute path to be used as the base path
   * @return a {@code JSONObject} for the map found or created at the specified path
   * @see #in(String)
   */
  public JSONObject jump(String path) {
    return jump(Path.from(path));
  }

  /**
   * Jumps to another branch in the tree of nested maps. The difference between {@code jump} and
   * {@link #in(String) in} is that the path passed to {@code jump} is always taken as an absolute path (i.e.
   * relative to the root map), while the path passed to {@code in} is taken relative to the path(s) passed to
   * previous calls to {@code in} and {@code jump}.
   *
   * @param path the absolute path to be used as the base path
   * @return a {@code JSONObject} for the map found or created at the specified path
   * @see #in(String)
   */
  public JSONObject jump(Path path) {
    return root().in(path);
  }

  /**
   * <p>Returns a {@code JSONObject} for the parent map of the map currently being edited. All subsequently
   * specified paths will be taken relative to the parent map's path. An {@link IllegalStateException} is
   * thrown when trying to exit out of the root map. You must pass the name of the parent map (the last path
   * segment of the parent map's path). An {@link IllegalArgumentException} is thrown if the argument does not
   * equal the parent map's name. This is to make sure you will not accidentally start writing to the wrong
   * map, and it makes the map-building code more intelligible.
   *
   * <blockquote><pre>{@code
   * Map<String, Object> map = JSONObject.empty()
   *  .in("person")
   *    .set("firstName", "John")
   *    .set("lastName", "Smith")
   *    .in("address")
   *      .set("street", "12 Revolutionary Rd.")
   *      .set("state", "CA")
   *      .backTo("person")
   *    .set("dateOfBirth", LocalDate.of(1967, 4, 4))
   *  .build();
   * }</pre></blockquote>
   *
   * <p>You can chain {@code backTo()} calls. To exit from a map directly under the root map, specify
   * {@code null}:
   *
   * <blockquote><pre>{@code
   * MapBuilder mb = JSONObject.empty();
   *  .in("department.manager.address")
   *    .set("street", "Sunset Blvd")
   *    .backTo("manager")
   *    .backTo("department")
   *    .backTo(null)
   *  .set("foo", "bar");
   * }</pre></blockquote>
   *
   * @param parentName the name of the parent map
   * @return a {@code JSONObject} for the parent map of the map currently being written to
   */
  public JSONObject backTo(String parentName) {
    Check.that(parent).is(notNull(), illegalState("already in root map"));
    if (parentName == null) {
      Check.that(root.size()).is(one(), "null can only be used to go back to root map");
    } else {
      Check.that(parentName).is(equalTo(), parent.name(), NO_SUCH_PARENT, name());
    }
    return parent;
  }

  /**
   * Takes you back to the root map. All paths you specify will be interpreted as absolute paths again.
   *
   * @return a {@code JSONObject} for the root map
   */
  public JSONObject root() {
    if (parent == null) {
      return this;
    }
    JSONObject map = parent;
    while (map.parent != null) {
      map = map.parent;
    }
    return map;
  }

  /**
   * Returns the key used to embed the current map within the parent map. If the current map is the root map,
   * an empty string is returned.
   *
   * @return the key used to embed the current map within the parent map
   */
  public String name() {
    if (parent == null) {
      return EMPTY_STRING;
    }
    return root.lastSegment();
  }

  /**
   * Returns the full path to the current map. That is, the path relative to which all {@code path} arguments
   * (e.g. for the {@link #set(String, Object) set()} method) are taken.
   *
   * @return the full path to the current map
   */
  public String where() {
    return root.toString();
  }

  /**
   * Returns whether the specified path is set to a terminal value (and hence cannot be extended).
   *
   * @param path the path
   * @return whether it is set to a terminal value
   */
  public boolean isSet(String path) {
    return isSet(Path.from(path));
  }

  /**
   * Returns whether the specified path is set to a terminal value (and hence cannot be extended).
   *
   * @param path the path
   * @return whether it is set to a terminal value
   */
  public boolean isSet(Path path) {
    Check.notNull(path);
    return isSet(this, path);
  }

  /**
   * Unsets the value of the specified path. This method returns quietly for non-existent paths.
   *
   * @param path the path to unset.
   * @return this {@code JSONObject}
   */
  public JSONObject unset(String path) {
    return unset(Path.from(path));
  }

  /**
   * Unsets the value of the specified path. This method returns quietly for non-existent paths.
   *
   * @param path the path to unset.
   * @return this {@code JSONObject}
   */
  public JSONObject unset(Path path) {
    Check.notNull(path);
    unset(this, path);
    return this;
  }

  /**
   * Returns the {@code Map} resulting all write actions thus far. The returned map is modifiable and retains
   * the order in which the paths (now keys) were written. You can continue to use the {@code JSONObject}
   * after a call to this method.
   *
   * @return the {@code Map} resulting from the write actions
   */
  public Map<String, Object> build() {
    JSONObject map = this;
    for (; map.parent != null; map = map.parent)
      ;
    return createMap(map);
  }

  /**
   * Returns a string representation of the map created thus far.
   *
   * @return a string representation of the map created thus far
   */
  @Override
  public String toString() {
    return build().toString();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static void init(JSONObject writer, Map map) {
    map.forEach((key, val) -> processEntry(writer, key, val));
  }

  @SuppressWarnings("rawtypes")
  private static void processEntry(JSONObject writer, Object key, Object val) {
    Check.that(key)
        .is(notNull(), "illegal null key in source map")
        .is(instanceOf(), String.class, "illegal key type in source map: ${type}");
    String k = key.toString();
    if (val instanceof Map nested) {
      Path path = writer.root.append(k);
      JSONObject jsonObject = new JSONObject(path, writer);
      writer.map.put(k, jsonObject);
      init(jsonObject, nested);
    } else {
      Check.that(val, VALUE).isNot(instanceOf(), JSONObject.class); // stifle nasty usage
      writer.map.put(k, ifNull(val, NULL));
    }
  }

  private static void set(JSONObject writer, Path path, Object val) {
    String key = firstSegment(path);
    if (path.size() == 1) {
      Check.that(val, VALUE)
          .isNot(instanceOf(), Map.class)
          .isNot(instanceOf(), JSONObject.class); // stifle nasty usage
      writer.map.put(key, ifNull(val, NULL));
    } else {
      set(getNestedWriter(writer, key), path.shift(), val);
    }
  }

  private static Result<Object> poll(JSONObject writer, Path path) {
    String key = path.segment(0);
    Object val = writer.map.get(key);
    if (val instanceof JSONObject nested) {
      if (path.size() == 1) {
        return Result.of(createMap(nested));
      }
      return poll(nested, path.shift());
    } else if (path.size() == 1 && val != null) {
      return Result.of(ObjectMethods.when(val, sameAs(), NULL, null));
    }
    return Result.notAvailable();
  }

  private static JSONObject in(JSONObject writer, Path path) {
    if (path.isEmpty()) {
      return writer;
    }
    String key = firstSegment(path);
    return in(getNestedWriter(writer, key), path.shift());
  }

  private static boolean isSet(JSONObject writer, Path path) {
    String key = firstSegment(path);
    Object val = writer.map.get(key);
    if (val == null) {
      return false;
    } else if (path.size() == 1 || !(val instanceof JSONObject)) {
      return true;
    }
    return isSet((JSONObject) val, path.shift());
  }

  private static void unset(JSONObject writer, Path path) {
    String key = firstSegment(path);
    if (path.size() == 1) {
      writer.map.remove(key);
    } else {
      unset(getNestedWriter(writer, key), path.shift());
    }
  }

  private static Map<String, Object> createMap(JSONObject writer) {
    Map<String, Object> map = LinkedHashMap.newLinkedHashMap(writer.map.size());
    writer.map.forEach((key, val) -> {
      if (val instanceof JSONObject jsonObject) {
        map.put(key, createMap(jsonObject));
      } else if (val == NULL) {
        map.put(key, null);
      } else {
        map.put(key, val);
      }
    });
    return map;
  }

  private static JSONObject getNestedWriter(JSONObject writer, String key) {
    Path root = writer.root.append(key);
    Object val = writer.map.computeIfAbsent(key, _ -> new JSONObject(root, writer));
    if (val instanceof JSONObject mb) {
      return mb;
    }
    throw new PathBlockedException(root, val);
  }

  private static PathBlockedException alreadySet(JSONObject writer, String key) {
    Path absPath = writer.root.append(key);
    Object curVal = writer.map.get(key);
    return new PathBlockedException(absPath, curVal);
  }

  private static String firstSegment(Path path) {
    return Check.that(path.firstSegment()).is(notNull(), "Illegal null segment in path \"${0}\"", path).ok();
  }

}
