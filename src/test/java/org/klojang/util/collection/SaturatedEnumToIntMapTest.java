package org.klojang.util.collection;

import org.junit.Test;
import org.klojang.util.MutableInt;

import java.time.DayOfWeek;
import java.util.*;

import static java.time.DayOfWeek.*;
import static java.util.AbstractMap.Entry;
import static java.util.AbstractMap.SimpleImmutableEntry;
import static org.junit.Assert.*;
import static org.klojang.util.collection.SaturatedEnumToIntMapTest.TestEnum.*;

public class SaturatedEnumToIntMapTest {

  public enum TestEnum {
    RED,
    BLUE,
    ORANGE,
    GREEN,
    BLACK
  }

  @Test
  public void EnumToIntMap01() {
    SaturatedEnumToIntMap<TestEnum> map = new SaturatedEnumToIntMap<>(TestEnum.class);
    assertFalse(map.isEmpty());
  }

  @Test
  public void EnumToIntMap02() {
    SaturatedEnumToIntMap<TestEnum> map0 = new SaturatedEnumToIntMap<>(TestEnum.class);
    map0.put(BLUE, 7);
    map0.put(GREEN, 8);
    SaturatedEnumToIntMap<TestEnum> map1 = new SaturatedEnumToIntMap<>(map0);
    assertEquals(map0, map1);
  }

  @Test
  public void EnumToIntMap03() {
    SaturatedEnumToIntMap<TestEnum> map = new SaturatedEnumToIntMap<>(TestEnum.class,
        k -> k.name().length());
    assertEquals(TestEnum.values().length, map.size());
    assertEquals(3, map.get(RED));
    assertEquals(4, map.get(BLUE));
    assertEquals(6, map.get(ORANGE));
    assertEquals(5, map.get(GREEN));
    assertEquals(5, map.get(BLACK));
  }

  @Test
  public void EnumToIntMap04() {
    SaturatedEnumToIntMap<TestEnum> map = new SaturatedEnumToIntMap<>(TestEnum.class);
    assertFalse(map.isEmpty());
  }

  @Test
  public void put01() {
    SaturatedEnumToIntMap<TestEnum> map = new SaturatedEnumToIntMap<>(TestEnum.class);
    map.put(RED, 7);
    map.put(BLACK, 9);
    assertEquals(7, map.get(RED));
    assertEquals(9, map.get(BLACK));
  }

  @Test
  public void containsKey01() {
    SaturatedEnumToIntMap<TestEnum> map = new SaturatedEnumToIntMap<>(TestEnum.class);
    map.put(RED, 7);
    map.put(BLACK, 9);
    assertTrue(map.containsKey(RED));
    assertTrue(map.containsKey(BLACK));
    assertTrue(map.containsKey(BLUE));
    assertTrue(map.containsKey(ORANGE));
    assertTrue(map.containsKey(GREEN));
  }

  @Test
  public void putAll00() {
    SaturatedEnumToIntMap<DayOfWeek> map0 = new SaturatedEnumToIntMap<>(DayOfWeek.class);
    map0.put(MONDAY, 2);
    map0.put(THURSDAY, 33);
    map0.put(SATURDAY, 37);
    SaturatedEnumToIntMap<DayOfWeek> map1 = new SaturatedEnumToIntMap<>(DayOfWeek.class);
    map1.putAll(map0);
    assertEquals(map0, map1);
  }

  @Test
  public void putAll01() {
    SaturatedEnumToIntMap<TestEnum> map1 = new SaturatedEnumToIntMap<>(TestEnum.class);
    SaturatedEnumToIntMap<TestEnum> map2 = new SaturatedEnumToIntMap<>(TestEnum.class);
    map1.put(RED, 7);
    map1.put(BLACK, 9);
    map1.put(BLACK, 11);
    map1.put(GREEN, 100);
    map2.putAll(map1);
    assertEquals(map1, map2);
  }

  @Test
  public void putAll03() {
    SaturatedEnumToIntMap<TestEnum> map1 = new SaturatedEnumToIntMap<>(TestEnum.class);
    SaturatedEnumToIntMap<TestEnum> map2 = new SaturatedEnumToIntMap<>(TestEnum.class);
    map1.put(RED, 7);
    map2.putAll(map1);
    assertEquals(map1, map2);
  }

  @Test
  public void values00() {
    SaturatedEnumToIntMap<TestEnum> map1 = new SaturatedEnumToIntMap<>(TestEnum.class);
    map1.put(RED, 7);
    map1.put(BLACK, 9);
    map1.put(BLACK, 11);
    map1.put(GREEN, 100);
    map1.put(GREEN, 102);
    map1.put(ORANGE, 102);
    map1.put(BLUE, 102);
    List<Integer> values = new ArrayList<>(map1.values());
    Collections.sort(values);
    assertEquals(7, (int) values.get(0));
    assertEquals(11, (int) values.get(1));
  }

  @Test
  public void intValues00() {
    SaturatedEnumToIntMap<TestEnum> map1 = new SaturatedEnumToIntMap<>(TestEnum.class);
    map1.put(RED, 7);
    map1.put(BLACK, 9);
    map1.put(BLACK, 11);
    map1.put(GREEN, 100);
    map1.put(GREEN, 102);
    map1.put(ORANGE, 102);
    map1.put(BLUE, 102);
    IntList intList = new IntArrayList(map1.intValues());
    intList.sort();
    assertEquals(7, intList.get(0));
    assertEquals(11, intList.get(1));
  }

  @Test
  public void size01() {
    SaturatedEnumToIntMap<TestEnum> map = new SaturatedEnumToIntMap<>(TestEnum.class);
    assertEquals(5, map.size());
  }

  @Test
  public void set01() {
    SaturatedEnumToIntMap<TestEnum> map =
        new SaturatedEnumToIntMap<>(TestEnum.class).set(RED, 7).set(ORANGE, 4).set(BLUE, 28);
    assertTrue(map.containsKey(RED));
    assertTrue(map.containsKey(ORANGE));
    assertTrue(map.containsKey(BLUE));
  }

  @Test
  public void constructor00() {
    SaturatedEnumToIntMap<DayOfWeek> days = new SaturatedEnumToIntMap<>(DayOfWeek.class);
    days.put(MONDAY, 2);
    days.put(THURSDAY, 33);
    days.put(SATURDAY, 37);
    SaturatedEnumToIntMap<DayOfWeek> copy = new SaturatedEnumToIntMap<>(days);
    assertEquals(days, copy);
  }

  @Test
  public void constructor01() {
    SaturatedEnumToIntMap<DayOfWeek> map = new SaturatedEnumToIntMap<>(DayOfWeek.class);
    map.put(MONDAY, 2);
    map.put(THURSDAY, 33);
    map.put(SATURDAY, 37);
    SaturatedEnumToIntMap<DayOfWeek> copy = new SaturatedEnumToIntMap<>(map);
    assertEquals(map, copy);
  }

  @Test
  public void keySet00() {
    SaturatedEnumToIntMap<DayOfWeek> map = new SaturatedEnumToIntMap<>(DayOfWeek.class);
    map.put(MONDAY, 2);
    map.put(THURSDAY, 33);
    map.put(SATURDAY, 37);
    assertEquals(Set.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY), map.keySet());
  }

  @Test
  public void entrySet00() {
    SaturatedEnumToIntMap<DayOfWeek> map = new SaturatedEnumToIntMap<>(DayOfWeek.class);
    map.put(MONDAY, 2);
    map.put(THURSDAY, 33);
    map.put(SATURDAY, 37);
    Set<Entry<DayOfWeek, Integer>> set = map.entrySet();
    assertTrue(set.contains(new SimpleImmutableEntry<>(MONDAY, 2)));
    assertTrue(set.contains(new SimpleImmutableEntry<>(THURSDAY, 33)));
    assertTrue(set.contains(new SimpleImmutableEntry<>(SATURDAY, 37)));
  }

  @Test
  public void forEach00() {
    SaturatedEnumToIntMap<DayOfWeek> map = new SaturatedEnumToIntMap<>(DayOfWeek.class);
    map.put(MONDAY, 100);
    map.put(THURSDAY, 200);
    map.put(SATURDAY, 300);
    MutableInt mi = new MutableInt();
    map.forEach((k, v) -> mi.add(v));
    assertEquals(600, mi.get());
  }

  @Test
  public void toGenericMap00() {
    SaturatedEnumToIntMap<DayOfWeek> map = new SaturatedEnumToIntMap<>(DayOfWeek.class);
    map.put(MONDAY, 100);
    map.put(THURSDAY, 200);
    map.put(SATURDAY, 300);
    assertEquals(Map.of(MONDAY,
            100,
            TUESDAY,
            0,
            WEDNESDAY,
            0,
            THURSDAY,
            200,
            FRIDAY,
            0,
            SATURDAY,
            300,
            SUNDAY,
            0),
        map.toGenericMap());
  }

  @Test
  public void toString00() {
    SaturatedEnumToIntMap<DayOfWeek> map = new SaturatedEnumToIntMap<>(DayOfWeek.class);
    map.put(MONDAY, 1);
    map.put(THURSDAY, 2);
    map.put(SATURDAY, 3);
    assertEquals("[MONDAY=1, TUESDAY=0, WEDNESDAY=0, THURSDAY=2, FRIDAY=0, SATURDAY=3, SUNDAY=0]",
        map.toString());
  }

}
