package org.klojang.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.klojang.util.NumberMethods;
import org.klojang.util.util.EnumParser;

@SuppressWarnings({"rawtypes", "unchecked"})
final class MorphToEnum {

  private MorphToEnum() {
    throw new UnsupportedOperationException();
  }

  private static final Map<Class, EnumParser> parsers = new HashMap<>();

  static <T extends Enum<T>> T morph(Object obj, Class enumClass) {
    EnumParser parser = parsers.computeIfAbsent(enumClass, EnumParser::new);
    if (obj instanceof Character c) {
      if (c >= '0' && c <= '9') {
        return (T) parser.parse(c - 48);
      }
    } else if (obj instanceof String s) {
      OptionalInt opt = NumberMethods.toInt(s);
      if (opt.isPresent()) {
        return (T) parser.parse(opt.getAsInt());
      }
    }
    return (T) parsers.computeIfAbsent(enumClass, EnumParser::new).parse(obj);
  }

}
