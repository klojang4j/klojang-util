package org.klojang.util.collection;

import org.junit.Test;
import org.klojang.util.MutableInt;

import java.time.DayOfWeek;
import java.util.*;

import static java.time.DayOfWeek.*;
import static java.util.AbstractMap.Entry;
import static java.util.AbstractMap.SimpleImmutableEntry;
import static org.junit.Assert.*;
import static org.klojang.util.collection.EnumToIntMapTest.TestEnum.*;

public class EnumToIntMapTest {

  public enum TestEnum {
    RED,
    BLUE,
    ORANGE,
    GREEN,
    BLACK
  }

  @Test
  public void EnumToIntMap01() {
    EnumToIntMap<TestEnum> map = new EnumToIntMap<>(TestEnum.class);
    assertTrue(map.isEmpty());
  }

  @Test
  public void EnumToIntMap02() {
    EnumToIntMap<TestEnum> map0 = new EnumToIntMap<>(TestEnum.class);
    map0.put(BLUE, 7);
    map0.put(GREEN, 8);
    EnumToIntMap<TestEnum> map1 = new EnumToIntMap<>(map0);
    assertEquals(map0, map1);
  }

  @Test
  public void EnumToIntMap03() {
    EnumToIntMap<TestEnum> map = new EnumToIntMap<>(TestEnum.class,
        k -> k.name().length());
    assertEquals(TestEnum.values().length, map.size());
    assertEquals(3, map.get(RED).getAsInt());
    assertEquals(4, map.get(BLUE).getAsInt());
    assertEquals(6, map.get(ORANGE).getAsInt());
    assertEquals(5, map.get(GREEN).getAsInt());
    assertEquals(5, map.get(BLACK).getAsInt());
  }

  @Test
  public void EnumToIntMap04() {
    EnumToIntMap<TestEnum> map = new EnumToIntMap<>(TestEnum.class);
    assertTrue(map.isEmpty());
  }

  @Test
  public void put01() {
    EnumToIntMap<TestEnum> map = new EnumToIntMap<>(TestEnum.class);
    map.put(RED, 7);
    map.put(BLACK, 9);
    assertEquals(2, map.size());
    assertEquals(7, map.get(RED).getAsInt());
    assertEquals(9, map.get(BLACK).getAsInt());
  }

  @Test
  public void containsKey01() {
    EnumToIntMap<TestEnum> map = new EnumToIntMap<>(TestEnum.class);
    map.put(RED, 7);
    map.put(BLACK, 9);
    assertTrue(map.containsKey(RED));
    assertTrue(map.containsKey(BLACK));
    assertFalse(map.containsKey(BLUE));
    assertFalse(map.containsKey(ORANGE));
    assertFalse(map.containsKey(GREEN));
  }

  @Test
  public void putAll00() {
    EnumToIntMap<DayOfWeek> map0 = new EnumToIntMap<>(DayOfWeek.class);
    map0.put(MONDAY, 2);
    map0.put(THURSDAY, 33);
    map0.put(SATURDAY, 37);
    EnumToIntMap<DayOfWeek> map1 = new EnumToIntMap<>(DayOfWeek.class);
    map1.putAll(map0);
    assertEquals(map0, map1);
  }

  @Test
  public void putAll01() {
    EnumToIntMap<TestEnum> map1 = new EnumToIntMap<>(TestEnum.class);
    EnumToIntMap<TestEnum> map2 = new EnumToIntMap<>(TestEnum.class);
    map1.put(RED, 7);
    map1.put(BLACK, 9);
    map1.put(BLACK, 11);
    map1.put(GREEN, 100);
    map2.putAll(map1);
    assertTrue(map1.equals(map2));
  }

   @Test
  public void putAll03() {
    EnumToIntMap<TestEnum> map1 = new EnumToIntMap<>(TestEnum.class);
    EnumToIntMap<TestEnum> map2 = new EnumToIntMap<>(TestEnum.class);
    map1.put(RED, 7);
    map2.putAll(map1);
    assertEquals(map1, map2);
  }

   @Test
  public void values00() {
    EnumToIntMap<TestEnum> map1 = new EnumToIntMap<>(TestEnum.class);
    map1.put(RED, 7);
    map1.put(BLACK, 9);
    map1.put(BLACK, 11);
    map1.put(GREEN, 100);
    map1.put(GREEN, 102);
    map1.put(ORANGE, 102);
    map1.put(BLUE, 102);
    map1.remove(BLACK);
    List<Integer> values = new ArrayList<>(map1.values());
    Collections.sort(values);
    assertEquals(4, values.size());
    assertEquals(7, (int) values.get(0));
    assertEquals(102, (int) values.get(1));
  }

  @Test
  public void intValues00() {
    EnumToIntMap<TestEnum> map1 = new EnumToIntMap<>(TestEnum.class);
    map1.put(RED, 7);
    map1.put(BLACK, 9);
    map1.put(BLACK, 11);
    map1.put(GREEN, 100);
    map1.put(GREEN, 102);
    map1.put(ORANGE, 102);
    map1.put(BLUE, 102);
    map1.remove(BLACK);
    IntList intList = new IntArrayList(map1.intValues());
    intList.sort();
    assertEquals(4, intList.size());
    assertEquals(7, intList.get(0));
    assertEquals(102, intList.get(1));
  }

  @Test
  public void size01() {
    EnumToIntMap<TestEnum> map = new EnumToIntMap<>(TestEnum.class);
    assertEquals(0, map.size());
    map.put(BLACK, 9);
    map.put(BLACK, 11);
    map.put(GREEN, 100);
    assertEquals(2, map.size());
    map.remove(BLUE);
    assertEquals(2, map.size());
    map.remove(GREEN);
    assertEquals(1, map.size());
    map.remove(BLACK);
    assertEquals(0, map.size());
    map.remove(ORANGE);
    assertEquals(0, map.size());
  }

  @Test
  public void set01() {
    EnumToIntMap<TestEnum> map =
        new EnumToIntMap<>(TestEnum.class).set(RED, 7).set(ORANGE, 4).set(BLUE, 28);
    assertEquals(3, map.size());
    assertTrue(map.containsKey(RED));
    assertTrue(map.containsKey(ORANGE));
    assertTrue(map.containsKey(BLUE));
  }

  @Test
  public void constructor00() {
    EnumToIntMap<DayOfWeek> days = new EnumToIntMap<>(DayOfWeek.class);
    days.put(MONDAY, 2);
    days.put(THURSDAY, 33);
    days.put(SATURDAY, 37);
    EnumToIntMap<DayOfWeek> copy = new EnumToIntMap<>(days);
    assertEquals(days, copy);
  }

  @Test
  public void constructor01() {
    EnumToIntMap<DayOfWeek> map = new EnumToIntMap<>(DayOfWeek.class);
    map.put(MONDAY, 2);
    map.put(THURSDAY, 33);
    map.put(SATURDAY, 37);
    EnumToIntMap<DayOfWeek> copy = new EnumToIntMap<>(map);
    assertEquals(map, copy);
  }

  @Test
  public void clear00() {
    EnumToIntMap<DayOfWeek> map = new EnumToIntMap<>(DayOfWeek.class);
    assertEquals(0, map.size());
    assertTrue(map.isEmpty());
    map.put(MONDAY, 2);
    map.put(THURSDAY, 33);
    map.put(SATURDAY, 37);
    assertEquals(3, map.size());
    assertFalse(map.isEmpty());
    map.clear();
    assertEquals(0, map.size());
    assertTrue(map.isEmpty());
  }

  @Test
  public void keySet00() {
    EnumToIntMap<DayOfWeek> map = new EnumToIntMap<>(DayOfWeek.class);
    map.put(MONDAY, 2);
    map.put(THURSDAY, 33);
    map.put(SATURDAY, 37);
    assertEquals(Set.of(MONDAY, THURSDAY, SATURDAY), map.keySet());
  }

  @Test
  public void entrySet00() {
    EnumToIntMap<DayOfWeek> map = new EnumToIntMap<>(DayOfWeek.class);
    map.put(MONDAY, 2);
    map.put(THURSDAY, 33);
    map.put(SATURDAY, 37);
    Set<Entry<DayOfWeek, Integer>> set = map.entrySet();
    assertEquals(3, set.size());
    assertTrue(set.contains(new SimpleImmutableEntry<>(MONDAY, 2)));
    assertTrue(set.contains(new SimpleImmutableEntry<>(THURSDAY, 33)));
    assertTrue(set.contains(new SimpleImmutableEntry<>(SATURDAY, 37)));
  }

  @Test
  public void getOrDefault00() {
    EnumToIntMap<DayOfWeek> map = new EnumToIntMap<>(DayOfWeek.class);
    map.put(MONDAY, 2);
    map.put(THURSDAY, 33);
    map.put(SATURDAY, 37);
    assertEquals(100, map.getOrDefault(SUNDAY, 100));
    assertEquals(33, map.getOrDefault(THURSDAY, 100));
  }

  @Test
  public void forEach00() {
    EnumToIntMap<DayOfWeek> map = new EnumToIntMap<>(DayOfWeek.class);
    map.put(MONDAY, 100);
    map.put(THURSDAY, 200);
    map.put(SATURDAY, 300);
    MutableInt mi = new MutableInt();
    map.forEach((k, v) -> mi.plusIs(v));
    assertEquals(600, mi.get());
  }

  @Test
  public void toGenericMap00() {
    EnumToIntMap<DayOfWeek> map = new EnumToIntMap<>(DayOfWeek.class);
    map.put(MONDAY, 100);
    map.put(THURSDAY, 200);
    map.put(SATURDAY, 300);
    assertEquals(Map.of(MONDAY, 100, THURSDAY, 200, SATURDAY, 300),
        map.toGenericMap());
  }

  @Test
  public void toString00() {
    EnumToIntMap<DayOfWeek> map = new EnumToIntMap<>(DayOfWeek.class);
    map.put(MONDAY, 1);
    map.put(THURSDAY, 2);
    map.put(SATURDAY, 3);
    assertEquals("[MONDAY=1, THURSDAY=2, SATURDAY=3]", map.toString());
  }


}
