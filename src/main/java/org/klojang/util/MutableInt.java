package org.klojang.util;

import org.klojang.check.Check;

import java.util.function.IntUnaryOperator;

/**
 * The mutable-integer class.
 */
public final class MutableInt {

  public static MutableInt of(int value) {
    return new MutableInt(value);
  }

  private int i;

  /**
   * Instantiates a {@code MutableInt} with an initial value of 0 (zero).
   */
  public MutableInt() {
  }

  /**
   * Instantiates a {@code MutableInt} with the specified initial value.
   *
   * @param value the initial value of this instance
   */
  public MutableInt(int value) {
    i = value;
  }

  /**
   * Instantiates a {@code MutableInt} initialized to the current value of another instance.
   *
   * @param other the instance used to initialize this instance.
   */
  public MutableInt(MutableInt other) {
    Check.notNull(other);
    i = other.i;
  }

  /**
   * Returns the current value of this {@code MutableInt}.
   *
   * @return the current value of this {@code MutableInt}
   */
  public int get() {
    return i;
  }

  /**
   * Returns a copy of this {@code MutableInt}.
   *
   * @return a copy of this {@code MutableInt}
   */
  public MutableInt copy() {
    return new MutableInt(i);
  }

  /**
   * Increments the value by one and returns the original value (before incrementing). Corresponds to the ++
   * postfix operator.
   *
   * @return the original value
   */
  public int increment() {
    return i++;
  }

  /**
   * Increments the value by one and returns the new value (after incrementing). Corresponds to the ++ prefix
   * operator.
   *
   * @return the new value
   */
  public int oneUp() {
    return add(1);
  }


  /**
   * Decrements the value by one and returns the original value (before decrementing). Corresponds to the --
   * postfix operator.
   *
   * @return the original value
   */
  public int decrement() {
    return i--;
  }

  /**
   * Decrements the value by one and returns the new value (after decrementing). Corresponds to the -- prefix
   * operator.
   *
   * @return the new value
   */
  public int oneDown() {
    return subtract(1);
  }

  /**
   * Adds the specified value to the current value and returns the new value.
   *
   * @param j the value to add
   * @return the new value
   */
  public int add(int j) {
    return i += j;
  }

  /**
   * Adds the specified value to the current value and returns the new value.
   *
   * @param other the value the add
   * @return the new value
   */
  public MutableInt add(MutableInt other) {
    Check.notNull(other);
    i += other.i;
    return this;
  }

  /**
   * Subtracts the specified value to the current value and returns the new value.
   *
   * @param j the value to subtract
   * @return the new value
   */
  public int subtract(int j) {
    return i -= j;
  }

  /**
   * Subtracts the specified value to the current value and returns the new value.
   *
   * @param other the value to subtract
   * @return the new value
   */
  public MutableInt subtract(MutableInt other) {
    Check.notNull(other);
    i -= other.i;
    return this;
  }

  /**
   * Subtracts the specified value to the current value and returns the new value.
   *
   * @param j the value to subtract
   * @return the new value
   */
  public int multiply(int j) {
    return i *= j;
  }

  /**
   * Subtracts the specified value to the current value and returns the new value.
   *
   * @param other the value to subtract
   * @return the new value
   */
  public MutableInt multiply(MutableInt other) {
    Check.notNull(other);
    i *= other.i;
    return this;
  }

  /**
   * Computes a new value for this instance using the current value as input
   *
   * @param operator a function that computes a new value based on the current value
   * @return the new value
   */
  public int computeInt(IntUnaryOperator operator) {
    return i = operator.applyAsInt(i);
  }

  /**
   * Computes a new value for this instance using the current value as input
   *
   * @param operator a function that computes a new value based on the current value
   * @return the new value
   */
  public MutableInt compute(IntUnaryOperator operator) {
    Check.notNull(operator);
    i = operator.applyAsInt(i);
    return this;
  }

  /**
   * Overwrites the current value with the specified value and returns the new value.
   *
   * @param j the new value
   * @return the new value
   */
  public int set(int j) {
    return i = j;
  }

  /**
   * Overwrites the current value with the specified value and returns the new value.
   *
   * @param other the new value
   * @return the new value
   */
  public MutableInt set(MutableInt other) {
    Check.notNull(other);
    i = other.i;
    return this;
  }

  /**
   * Corresponds to the {@code ==} (equals) operation.
   *
   * @param j the value to compare this instance with
   * @return {@code true} if equal, {@code false} otherwise
   */
  public boolean equalTo(int j) {
    return i == j;
  }

  /**
   * Corresponds to the {@code ==} (equals) operation.
   *
   * @param other the value to compare this instance with
   * @return {@code true} if equal, {@code false} otherwise
   */
  public boolean equalTo(MutableInt other) {
    Check.notNull(other);
    return i == other.i;
  }

  /**
   * Corresponds to the {@code !=} (not equals) operation.
   *
   * @param j the value to compare this instance with
   * @return {@code true} if not equal, {@code false} otherwise
   */
  public boolean notEquals(int j) {
    return i != j;
  }

  /**
   * Corresponds to the {@code !=} (not equals) operation.
   *
   * @param other the value to compare this instance with
   * @return {@code true} if equal, {@code false} otherwise
   */
  public boolean notEquals(MutableInt other) {
    Check.notNull(other);
    return i != other.i;
  }

  /**
   * Corresponds to the {@code >} (greater than) operation.
   *
   * @param j the value to compare this instance with
   * @return {@code true} if this instance has a value greater than the specified value, {@code false}
   *     otherwise
   */
  public boolean greaterThan(int j) {
    return i > j;
  }

  /**
   * Corresponds to the {@code >} (greater than) operation.
   *
   * @param other the value to compare this instance with
   * @return {@code true} if this instance has a value greater than the specified value, {@code false}
   *     otherwise
   */
  public boolean greaterThan(MutableInt other) {
    Check.notNull(other);
    return i > other.i;
  }

  /**
   * Corresponds to the {@code <} (less than) operation.
   *
   * @param j the value to compare this instance with
   * @return {@code true} if this instance has a value less than the specified value, {@code false} otherwise
   */
  public boolean lessThan(int j) {
    return i < j;
  }

  /**
   * Corresponds to the {@code <} (less than) operation.
   *
   * @param other the value to compare this instance with
   * @return {@code true} if this instance has a value less than the specified value, {@code false} otherwise
   */
  public boolean lessThan(MutableInt other) {
    Check.notNull(other);
    return i < other.i;
  }

  /**
   * Corresponds to the {@code >=} (greater or equal) operation.
   *
   * @param j the value to compare this instance with
   * @return {@code true} if this instance has a value greater than, or equal to the specified value,
   *     {@code false} otherwise
   */
  public boolean gte(int j) {
    return i >= j;
  }

  /**
   * Corresponds to the {@code >=} (greater or equal) operation.
   *
   * @param other the value to compare this instance with
   * @return {@code true} if this instance has a value greater than, or equal to the specified value,
   *     {@code false} otherwise
   */
  public boolean gte(MutableInt other) {
    Check.notNull(other);
    return i >= other.i;
  }

  /**
   * Corresponds to the {@code <=} (less or equal) operation.
   *
   * @param j the value to compare this instance with
   * @return {@code true} if this instance has a value less than, or equal to the specified value,
   *     {@code false} otherwise
   */
  public boolean lte(int j) {
    return i <= j;
  }

  /**
   * Corresponds to the {@code <=} (less or equal) operation.
   *
   * @param other the value to compare this instance with
   * @return {@code true} if this instance has a value less than, or equal to the specified value,
   *     {@code false} otherwise
   */
  public boolean lte(MutableInt other) {
    Check.notNull(other);
    return i <= other.i;
  }

  /**
   * Sets the value to 0 (zero).
   *
   * @return 0 (zero)
   */
  public int reset() {
    return i = 0;
  }

  /**
   * Returns the current value of this instance
   *
   * @return the current value of this instance
   */
  @Override
  public int hashCode() {
    return i;
  }

  /**
   * Returns true if {@code obj} is a {@code MutableInt} containing the same {@code int} value as this
   * {@code MutableInt}, {@code false otherwise}.
   *
   * @param obj the value to compare this instance with
   * @return Whether this {@code MutableInt} is equal to the specified value
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof MutableInt mi) {
      return i == mi.i;
    }
    return false;
  }

  /**
   * Returns the string representation of the current value.
   *
   * @return the string representation of the current value
   */
  @Override
  public String toString() {
    return String.valueOf(i);
  }

}
