package org.klojang.util.path;

import static org.klojang.util.path.PathWalkerException.*;

import java.util.Map;

import org.klojang.util.path.KeyDeserializationException;
import org.klojang.util.path.KeyDeserializer;
import org.klojang.util.path.ObjectReader;
import org.klojang.util.path.Path;
import org.klojang.util.path.SegmentReader;

final class MapSegmentReader extends SegmentReader<Map<?, ?>> {

  MapSegmentReader(boolean suppressExceptions, KeyDeserializer keyDeserializer) {
    super(suppressExceptions, keyDeserializer);
  }

  @Override
  Object read(Map<?, ?> map, Path path, int segment) {
    Object key;
    if (kd == null) {
      key = path.segment(segment);
    } else {
      try {
        key = kd.deserialize(path, segment);
      } catch (KeyDeserializationException e) {
        return deadEnd(keyDeserializationFailed(path, segment, e));
      }
    }
    Object val = map.get(key);
    if (val == null && !map.containsKey(key)) {
      return deadEnd(noSuchKey(path, segment, key));
    }
    return new ObjectReader(se, kd).read(val, path, ++segment);
  }

}
