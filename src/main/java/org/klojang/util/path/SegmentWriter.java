package org.klojang.util.path;

import org.klojang.util.path.ArraySegmentWriter;
import org.klojang.util.path.BeanSegmentWriter;
import org.klojang.util.path.KeyDeserializer;
import org.klojang.util.path.ListSegmentWriter;
import org.klojang.util.path.MapSegmentWriter;
import org.klojang.util.path.Path;
import org.klojang.util.path.PathWalkerException;
import org.klojang.util.path.PrimitiveArraySegmentWriter;

abstract sealed class SegmentWriter<T> permits ArraySegmentWriter, BeanSegmentWriter,
    ListSegmentWriter, MapSegmentWriter, PrimitiveArraySegmentWriter {

  final boolean se;
  final KeyDeserializer kd;

  SegmentWriter(boolean suppressExceptions, KeyDeserializer keyDeserializer) {
    this.se = suppressExceptions;
    this.kd = keyDeserializer;
  }

  abstract boolean write(T obj, Path path, Object value);

  boolean deadEnd(PathWalkerException.Factory excFactory) {
    if (se) {
      return false;
    }
    throw excFactory.get();
  }

}
