package org.klojang.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class InvokeExceptionTest {

  @Test
  public void missingNoArgConstructor00() {
    Assert.assertEquals("missing no-arg constructor for class java.io.File",
        InvokeException.missingNoArgConstructor(File.class).getMessage());
  }

  @Test
  public void noSuchConstructor00() {
    assertEquals("no such constructor: File(int, Float)",
        InvokeException.noSuchConstructor(File.class, int.class, Float.class)
            .getMessage());
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void getArrayElement00() {
    int[] ints = {1, 2, 3, 4, 5};
    int i = InvokeMethods.getArrayElement(ints, 10);
  }

  @Test
  public void getArrayElement01() {
    int[] ints = {1, 2, 3, 4, 5};
    int elem = InvokeMethods.getArrayElement(ints, 2);
    assertEquals(3, elem);
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void setArrayElement00() {
    int[] ints = {1, 2, 3, 4, 5};
    InvokeMethods.setArrayElement(ints, 10, 7);
  }

  @Test
  public void setArrayElement01() {
    int[] ints = {1, 2, 3, 4, 5};
    InvokeMethods.setArrayElement(ints, 2, 7);
    int[] expected = {1, 2, 7, 4, 5};
    assertArrayEquals(expected, ints);
  }

  @Test(expected = ClassCastException.class)
  public void setArrayElement02() {
    int[] ints = {1, 2, 3, 4, 5};
    InvokeMethods.setArrayElement(ints, 2, "Hello world");
  }

}
