package org.klojang.util.path;

import static org.klojang.util.NumberMethods.toInt;
import static org.klojang.util.path.PathWalkerException.*;

import java.util.OptionalInt;

import org.klojang.util.path.KeyDeserializer;
import org.klojang.util.path.Path;
import org.klojang.util.path.SegmentWriter;

final class ArraySegmentWriter extends SegmentWriter<Object[]> {

  ArraySegmentWriter(boolean suppressExceptions, KeyDeserializer keyDeserializer) {
    super(suppressExceptions, keyDeserializer);
  }

  @Override
  boolean write(Object[] array, Path path, Object value) {
    int segment = path.size() - 1;
    if (value != null) {
      Class elemClass = array.getClass().getComponentType();
      if (!elemClass.isInstance(value)) {
        return deadEnd(typeMismatch(path, segment, elemClass, value.getClass()));
      }
    }
    OptionalInt opt = toInt(path.segment(segment));
    if (opt.isEmpty()) {
      return deadEnd(indexExpected(path, segment));
    }
    int idx = opt.getAsInt();
    if (idx < array.length) {
      array[idx] = value;
      return true;
    }
    return deadEnd(indexOutOfBounds(path, segment));
  }

}
