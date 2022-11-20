package org.klojang.util.path;

import org.klojang.util.path.ArraySegmentReader;
import org.klojang.util.path.BeanSegmentReader;
import org.klojang.util.path.CollectionSegmentReader;
import org.klojang.util.path.KeyDeserializer;
import org.klojang.util.path.MapSegmentReader;
import org.klojang.util.path.Path;
import org.klojang.util.path.PathWalkerException;
import org.klojang.util.path.PrimitiveArraySegmentReader;

abstract sealed class SegmentReader<T> permits ArraySegmentReader, BeanSegmentReader,
    CollectionSegmentReader, MapSegmentReader, PrimitiveArraySegmentReader {

  final boolean se;
  final KeyDeserializer kd;

  SegmentReader(boolean suppressExceptions, KeyDeserializer keyDeserializer) {
    this.se = suppressExceptions;
    this.kd = keyDeserializer;
  }

  abstract Object read(T obj, Path path, int segment);

  Object deadEnd(PathWalkerException.Factory excFactory) {
    if (se) {
      return null;
    }
    throw excFactory.get();
  }

}
