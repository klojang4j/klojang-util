package org.klojang.util;

import org.junit.Test;
import org.klojang.util.LaxTuple2;

import static org.junit.Assert.assertEquals;

public class LaxTuple2Test {

  @Test
  public void toEntry00() {
    var tuple = LaxTuple2.of("John", 42);
    var entry = tuple.toEntry();
    assertEquals("John", entry.getKey());
    assertEquals(42, (int) entry.getValue());
  }

}
