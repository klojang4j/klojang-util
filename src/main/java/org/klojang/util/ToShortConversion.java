package org.klojang.util;

import static org.klojang.util.NumberMethods.isRound;
import static org.klojang.util.NumberMethods.yes;
import static org.klojang.util.TypeConversionException.inputTypeNotSupported;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import org.klojang.util.TypeConversionException;

final class ToShortConversion {

  private ToShortConversion() {
    throw new UnsupportedOperationException();
  }

  private static final Map<Class<?>, Predicate<Number>> fitsIntoShort = Map.of(
      BigDecimal.class,
      n -> isRound((BigDecimal) n) && n.longValue() == n.shortValue(),
      BigInteger.class, n -> n.longValue() == n.shortValue(),
      Double.class, n -> isRound((Double) n) && n.longValue() == n.shortValue(),
      Float.class, n -> isRound((Float) n) && n.longValue() == n.shortValue(),
      Long.class, n -> n.longValue() == n.shortValue(),
      AtomicLong.class, n -> n.longValue() == n.shortValue(),
      Integer.class, n -> n.longValue() == n.shortValue(),
      AtomicInteger.class, n -> n.longValue() == n.shortValue(),
      Short.class, yes(),
      Byte.class, yes()
  );

  static boolean isLossless(Number n) {
    Predicate<Number> tester = fitsIntoShort.get(n.getClass());
    if (tester != null) {
      return tester.test(n);
    }
    throw inputTypeNotSupported(n, Short.class);
  }

  static short exec(Number n) {
    if (isLossless(n)) {
      return n.shortValue();
    }
    throw new TypeConversionException(n, Short.class);
  }

}
