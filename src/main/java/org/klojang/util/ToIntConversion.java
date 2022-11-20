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

final class ToIntConversion {

  private ToIntConversion() {
    throw new UnsupportedOperationException();
  }

  private static final Map<Class<?>, Predicate<Number>> fitsIntoInt = Map.of(
      BigDecimal.class,
      n -> isRound((BigDecimal) n) && n.longValue() == n.intValue(),
      BigInteger.class, n -> n.longValue() == n.intValue(),
      Double.class, n -> isRound((Double) n) && n.longValue() == n.intValue(),
      Float.class, n -> isRound((Float) n) && n.longValue() == n.intValue(),
      Long.class, n -> n.longValue() == n.intValue(),
      AtomicLong.class, n -> n.longValue() == n.intValue(),
      Integer.class, yes(),
      AtomicInteger.class, yes(),
      Short.class, yes(),
      Byte.class, yes()
  );

  static boolean isLossless(Number n) {
    Predicate<Number> tester = fitsIntoInt.get(n.getClass());
    if (tester != null) {
      return tester.test(n);
    }
    throw inputTypeNotSupported(n, Integer.class);
  }

  static int exec(Number n) {
    if (isLossless(n)) {
      return n.intValue();
    }
    throw new TypeConversionException(n, Integer.class);
  }

}
