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

final class ToByteConversion {

  private ToByteConversion() {
    throw new UnsupportedOperationException();
  }

  private static final Map<Class<?>, Predicate<Number>> fitsIntoByte = Map.of(
      BigDecimal.class,
      n -> isRound((BigDecimal) n) && n.longValue() == n.byteValue(),
      Double.class, n -> isRound((Double) n) && n.longValue() == n.byteValue(),
      Float.class, n -> isRound((Float) n) && n.longValue() == n.byteValue(),
      BigInteger.class, n -> n.longValue() == n.byteValue(),
      Long.class, n -> n.longValue() == n.byteValue(),
      AtomicLong.class, n -> n.longValue() == n.byteValue(),
      Integer.class, n -> n.longValue() == n.byteValue(),
      AtomicInteger.class, n -> n.longValue() == n.byteValue(),
      Short.class, n -> n.longValue() == n.byteValue(),
      Byte.class, yes()
  );

  static boolean isLossless(Number n) {
    Predicate<Number> tester = fitsIntoByte.get(n.getClass());
    if (tester != null) {
      return tester.test(n);
    }
    throw inputTypeNotSupported(n, Byte.class);
  }

  static byte exec(Number n) {
    if (isLossless(n)) {
      return n.byteValue();
    }
    throw new TypeConversionException(n, Byte.class);
  }

}
