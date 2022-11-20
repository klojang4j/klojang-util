package org.klojang.util.path;

import static org.klojang.util.NumberMethods.toInt;
import static org.klojang.util.path.PathWalkerException.*;
import static org.klojang.util.x.invoke.InvokeUtils.*;

import java.util.OptionalInt;

import org.klojang.util.invoke.InvokeException;
import org.klojang.util.path.KeyDeserializer;
import org.klojang.util.path.ObjectReader;
import org.klojang.util.path.Path;
import org.klojang.util.path.SegmentReader;

final class PrimitiveArraySegmentReader extends SegmentReader<Object> {

  PrimitiveArraySegmentReader(boolean suppressExceptions,
      KeyDeserializer keyDeserializer) {
    super(suppressExceptions, keyDeserializer);
  }

  @Override
  Object read(Object array, Path path, int segment) {
    OptionalInt opt = toInt(path.segment(segment));
    if (opt.isEmpty()) {
      return deadEnd(indexExpected(path, segment));
    }
    int idx = opt.getAsInt();
    int len = getArrayLength(array);
    if (idx >= 0 && idx < len) {
      Object val = getArrayElement(array, idx);
      return new ObjectReader(se, kd).read(val, path, ++segment);
    }
    return deadEnd(indexOutOfBounds(path, segment));
  }

}


