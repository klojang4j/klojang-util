package org.klojang.util;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;
import static org.klojang.util.ArrayMetaData.describe;
import static org.klojang.util.ArrayMethods.pack;

public class ArrayMetaDataTest {

  @Test
  public void dimensions00() {
    assertEquals(0, ArrayMetaData.countDimensions(int.class));
    assertEquals(1, ArrayMetaData.countDimensions(int[].class));
    assertEquals(2, ArrayMetaData.countDimensions(int[][].class));
  }

  @Test
  public void forType00() {
    ArrayMetaData metadata0 = ArrayMetaData.forType(String[][].class);
    ArrayMetaData metadata1 = ArrayMetaData.forArray(new String[][] {{"hello", "world"}, {"foo"}});
    ArrayMetaData metadata2 = ArrayMetaData.of(String.class, 2);
    ArrayMetaData metadata3 = ArrayMetaData.of(String[].class, 1);
    ArrayMetaData metadata4 = ArrayMetaData.of(String[][][][].class, -2);
    assertEquals(metadata0, metadata1);
    assertEquals(metadata0, metadata2);
    assertEquals(metadata0, metadata3);
    assertEquals(metadata0, metadata4);
  }

  @Test
  public void forType01() {
    ArrayMetaData metadata = ArrayMetaData.forType(String[][].class);
    assertEquals(String.class, metadata.getElementType());
    assertEquals(2, metadata.getDimensions());
  }

  @Test
  public void forType02() {
    ArrayMetaData metadata = ArrayMetaData.forType(int[][][][][].class);
    assertEquals(int.class, metadata.getElementType());
    assertEquals(5, metadata.getDimensions());
  }

  @Test
  public void forArray00() {
    ArrayMetaData metadata0 = ArrayMetaData.forArray(new File[0][][]);
    ArrayMetaData metadata1 = ArrayMetaData.forArray(new File[6][][]);
    ArrayMetaData metadata2 = ArrayMetaData.forArray(new File[0][][]);
    assertSame(metadata0.getElementType(), metadata1.getElementType());
    assertEquals(metadata0, metadata2);
  }

  @Test
  public void constructor00() {
    ArrayMetaData metadata = ArrayMetaData.of(long.class, 1);
    assertEquals(long.class, metadata.getElementType());
    assertEquals(1, metadata.getDimensions());
    assertEquals(long[].class, metadata.toClass());
  }

  @Test
  public void constructor01() {
    ArrayMetaData metadata = ArrayMetaData.of(long[][].class, 1);
    assertEquals(long.class, metadata.getElementType());
    assertEquals(3, metadata.getDimensions());
    assertEquals(long[][][].class, metadata.toClass());
  }

  @Test
  public void constructor02() {
    ArrayMetaData metadata = ArrayMetaData.of(long[][].class, 0);
    assertEquals(long.class, metadata.getElementType());
    assertEquals(2, metadata.getDimensions());
    assertEquals(long[][].class, metadata.toClass());
  }

  @Test
  public void constructor03() {
    ArrayMetaData metadata = ArrayMetaData.of(long[][].class, -1);
    assertEquals(long.class, metadata.getElementType());
    assertEquals(1, metadata.getDimensions());
    assertEquals(long[].class, metadata.toClass());
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructor04() {
    ArrayMetaData.of(void.class, -1);
  }

  @Test
  public void forArray01() {
    ArrayMetaData metadata = ArrayMetaData.forArray(new File[0][][]);
    assertEquals(File.class, metadata.getElementType());
    assertEquals(3, metadata.getDimensions());
  }

  @Test
  public void toClass00() {
    ArrayMetaData metadata = ArrayMetaData.of(short.class, 3);
    assertEquals(short[][][].class, metadata.toClass());
  }

  @Test
  public void newArray00() {
    ArrayMetaData metadata = ArrayMetaData.of(short.class, 3);
    short[][][] array = metadata.newArray(10);
    assertArrayEquals(new short[10][][], array);
  }

  @Test
  public void withElementType00() {
    ArrayMetaData metadata = ArrayMetaData.of(short.class, 3);
    assertEquals(int[][][].class, metadata.withElementType(int.class).toClass());
  }

  @Test
  public void withElementType01() {
    ArrayMetaData metadata = ArrayMetaData.of(short.class, 3);
    assertSame(metadata, metadata.withElementType(short.class));
  }

  @Test
  public void withDimensions00() {
    ArrayMetaData metadata = ArrayMetaData.of(short.class, 3);
    assertEquals(short[][][][].class, metadata.withDimensions(4).toClass());
  }

  @Test
  public void withDimensions01() {
    ArrayMetaData metadata = ArrayMetaData.of(short.class, 3);
    assertSame(metadata, metadata.withDimensions(3));
  }

  @Test
  public void boxed00() {
    ArrayMetaData metadata = ArrayMetaData.of(short.class, 1);
    assertEquals(ArrayMetaData.of(Short.class, 1), metadata.box());
  }

  @Test
  public void unboxed00() {
    ArrayMetaData metadata = ArrayMetaData.of(Double.class, 4);
    assertEquals(ArrayMetaData.of(double.class, 4), metadata.unbox());
  }

  @Test
  public void box00() {
    ArrayMetaData metadata = ArrayMetaData.of(short.class, 1);
    assertEquals(Short[].class, metadata.box().toClass());
  }

  @Test
  public void unbox00() {
    ArrayMetaData metadata = ArrayMetaData.of(Double.class, 4);
    assertEquals(double[][][][].class, metadata.unbox().toClass());
  }

  @Test
  public void toString00() {
    ArrayMetaData metadata = ArrayMetaData.of(double.class, 2);
    assertEquals("double[][]", metadata.toString());
  }

  @Test
  public void toString01() {
    ArrayMetaData metadata = ArrayMetaData.of(double[].class, 2);
    assertEquals("double[][][]", metadata.toString());
  }

  @Test
  public void toString02() {
    ArrayMetaData metadata = ArrayMetaData.of(double[][].class, 2);
    assertEquals("double[][][][]", metadata.toString());
  }

  @Test
  public void getArrayClassName00() {
    ArrayMetaData metadata = ArrayMetaData.of(Double.class, 2);
    assertEquals("Double[][]", metadata.getArrayClassName());
    metadata = ArrayMetaData.of(File.class, 2);
    assertEquals("java.io.File[][]", metadata.getArrayClassName());
  }

  @Test
  public void describe00() {
    assertEquals("Double[7][]", describe(new Double[7][4]));
    assertEquals("String[2]", describe(pack("hello", "world")));
    assertEquals("int[0]", describe(new int[0]));
  }

  @Test(expected = IllegalArgumentException.class)
  public void describe01() {
    describe("hello");
  }

}
