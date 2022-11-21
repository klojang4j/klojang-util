package org.klojang.util;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class InvokeExceptionTest {

  @Test
  public void missingNoArgConstructor00() {
    assertEquals("missing no-arg constructor for class java.io.File",
        InvokeException.missingNoArgConstructor(File.class).getMessage());
  }

  @Test
  public void noSuchConstructor00() {
    assertEquals("no such constructor: File(int, Float)",
        InvokeException.noSuchConstructor(File.class, int.class, Float.class).getMessage());
  }


}
