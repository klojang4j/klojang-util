package org.klojang.util.path;

import static org.klojang.util.NumberMethods.toInt;
import static org.klojang.util.ObjectMethods.isEmpty;
import static org.klojang.util.path.PathWalkerException.*;

import java.util.OptionalInt;

import org.klojang.util.path.KeyDeserializer;
import org.klojang.util.path.ObjectReader;
import org.klojang.util.path.Path;
import org.klojang.util.path.SegmentReader;

final class ArraySegmentReader extends SegmentReader<Object[]> {

  ArraySegmentReader(boolean suppressExceptions, KeyDeserializer keyDeserializer) {
    super(suppressExceptions, keyDeserializer);
  }

  @Override
  Object read(Object[] array, Path path, int segment) {
    OptionalInt opt = toInt(path.segment(segment));
    if (opt.isEmpty()) {
      return deadEnd(indexExpected(path, segment));
    }
    int idx = opt.getAsInt();
    if (idx < array.length) {
      return new ObjectReader(se, kd).read(array[idx], path, ++segment);
    }
    return deadEnd(indexOutOfBounds(path, segment));
  }

}
