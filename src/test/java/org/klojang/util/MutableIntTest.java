package org.klojang.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class MutableIntTest {

  public void copy00() {
    MutableInt mi0 = MutableInt.of(100);
    MutableInt mi1 = mi0.copy();
    assertEquals(mi0, mi1);
  }

  @Test
  public void increment00() {
    MutableInt mi0 = new MutableInt();
    int j = mi0.increment();
    assertEquals(0, j);
    assertEquals(1, mi0.get());
  }

  @Test
  public void oneUp00() {
    MutableInt mi0 = new MutableInt();
    int j = mi0.oneUp();
    assertEquals(1, j);
    assertEquals(1, mi0.get());
  }

  @Test
  public void decrement00() {
    MutableInt mi = new MutableInt(7);
    int j = mi.decrement();
    assertEquals(7, j);
    assertEquals(6, mi.get());
  }

  @Test
  public void oneDown00() {
    MutableInt mi = new MutableInt(7);
    int j = mi.oneDown();
    assertEquals(6, j);
    assertEquals(6, mi.get());
  }

  @Test
  public void add00() {
    MutableInt mi0 = new MutableInt(7);
    int j = mi0.add(2);
    assertEquals(9, j);
    assertEquals(9, mi0.get());
    MutableInt mi1 = mi0.add(mi0);
    assertEquals(18, mi1.get());
    assertEquals(18, mi0.get());
  }

  @Test
  public void subtract00() {
    MutableInt mi0 = new MutableInt(7);
    int j = mi0.subtract(2);
    assertEquals(5, j);
    assertEquals(5, mi0.get());
    MutableInt mi1 = mi0.subtract(mi0.copy());
    assertEquals(MutableInt.of(0), mi1);
    assertEquals(0, mi0.get());
  }

  @Test
  public void multiply00() {
    MutableInt mi0 = new MutableInt(7);
    int j = mi0.multiply(2);
    assertEquals(14, j);
    assertEquals(14, mi0.get());
    MutableInt mi1 = mi0.multiply(mi0);
    assertEquals(MutableInt.of(196), mi1);
  }


  @Test
  public void computeInt00() {
    MutableInt mi0 = new MutableInt(9);
    int j = mi0.computeInt(i -> i / 3);
    assertEquals(3, j);
  }

  @Test
  public void compute00() {
    MutableInt mi0 = new MutableInt(9);
    MutableInt mi1 = mi0.compute(i -> i / 3);
    assertSame(mi0, mi1);
    assertEquals(MutableInt.of(3), mi1);
  }

  @Test
  public void set00() {
    MutableInt mi0 = new MutableInt(7);
    int j = mi0.set(-2);
    assertEquals(-2, j);
    assertEquals(-2, mi0.get());
    MutableInt mi1 = mi0.set(MutableInt.of(7));
    assertSame(mi0, mi1);
    assertEquals(7, mi1.get());
  }

  @Test
  public void equalTo00() {
    MutableInt i = new MutableInt(7);
    assertTrue(i.equalTo(7));
    assertTrue(i.equalTo(new MutableInt(7)));
    assertFalse(i.equalTo(12));
    assertFalse(i.equalTo(new MutableInt(12)));
  }

  @Test
  public void notEquals00() {
    MutableInt i = new MutableInt(7);
    assertTrue(i.notEquals(9));
    assertTrue(i.notEquals(new MutableInt(9)));
    assertFalse(i.notEquals(7));
    assertFalse(i.notEquals(new MutableInt(7)));
  }

  @Test
  public void greaterThan00() {
    MutableInt i = new MutableInt(7);
    assertTrue(i.greaterThan(-7));
    assertTrue(i.greaterThan(new MutableInt(-7)));
    assertFalse(i.greaterThan(9));
    assertFalse(i.greaterThan(new MutableInt(9)));
  }

  @Test
  public void lessThan00() {
    MutableInt i = new MutableInt(7);
    assertTrue(i.lessThan(10));
    assertTrue(i.lessThan(new MutableInt(10)));
    assertFalse(i.lessThan(7));
    assertFalse(i.lessThan(new MutableInt(7)));
  }

  @Test
  public void gte00() {
    MutableInt i = new MutableInt(7);
    assertTrue(i.gte(-7));
    assertTrue(i.gte(new MutableInt(-7)));
    assertFalse(i.gte(9));
    assertFalse(i.gte(new MutableInt(9)));
  }

  @Test
  public void lte00() {
    MutableInt i = new MutableInt(7);
    assertTrue(i.lte(7));
    assertTrue(i.lte(new MutableInt(7)));
    assertFalse(i.lte(4));
    assertFalse(i.lte(new MutableInt(4)));
  }

  @Test
  public void reset00() {
    MutableInt i = new MutableInt(7);
    int j = i.reset();
    assertEquals(0, j);
    assertEquals(0, i.get());
  }

  @Test
  public void hashCode00() {
    MutableInt i = new MutableInt(7);
    assertEquals(7, i.hashCode());
  }

  @Test
  public void equals00() {
    MutableInt mi = new MutableInt(7);
    assertEquals(mi, mi);
    assertEquals(mi, mi.copy());
    assertEquals(mi, MutableInt.of(7));
    assertEquals(mi, new MutableInt(mi));
    assertNotEquals(mi, new MutableInt(8));
    assertNotEquals(mi, "7");
    assertNotEquals(mi, 7);
  }

  @Test
  public void toString00() {
    MutableInt i = new MutableInt(7);
    assertEquals("7", i.toString());
  }

}
