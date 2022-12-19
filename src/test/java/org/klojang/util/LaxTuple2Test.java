package org.klojang.util;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class LaxTuple2Test {

  @Test
  public void toEntry00() {
    var tuple = LaxTuple2.of("John", 42);
    var entry = tuple.toEntry();
    assertEquals("John", entry.getKey());
    assertEquals(42, (int) entry.getValue());
  }

  @Test
  public void isEmpty00() {
    var tuple = LaxTuple2.of("John", null);
    assertFalse(tuple.isEmpty());
  }

  @Test
  public void isEmpty01() {
    var tuple = LaxTuple2.of(null, null);
    assertTrue(tuple.isEmpty());
  }


  @Test
  public void isEmpty02() {
    var tuple = LaxTuple2.of("", null);
    assertFalse(tuple.isEmpty());
  }


  @Test
  public void isDeepNotEmpty00() {
    var tuple = LaxTuple2.of("John", Collections.emptyList());
    assertFalse(tuple.isDeepNotEmpty());
  }

  @Test
  public void isDeepNotEmpty01() {
    var tuple = LaxTuple2.of("John", 42);
    assertTrue(tuple.isDeepNotEmpty());
  }

}
