package org.klojang.util;

import org.junit.Test;
import org.klojang.util.Tuple2;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class Tuple2Test {

  @Test
  public void toEntry00() {
    var tuple = Tuple2.of("John", 42);
    var entry = tuple.toEntry();
    assertEquals("John", entry.getKey());
    assertEquals(42, (int) entry.getValue());
  }

}
