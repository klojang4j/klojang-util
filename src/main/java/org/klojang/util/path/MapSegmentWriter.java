package org.klojang.util.path;

import static org.klojang.util.path.ErrorCode.*;
import static org.klojang.util.path.PathWalkerException.*;

import java.util.Map;

import org.klojang.util.path.KeyDeserializationException;
import org.klojang.util.path.KeyDeserializer;
import org.klojang.util.path.Path;
import org.klojang.util.path.SegmentWriter;

@SuppressWarnings({"rawtypes", "unchecked"})
final class MapSegmentWriter extends SegmentWriter<Map> {

  MapSegmentWriter(boolean suppressExceptions, KeyDeserializer keyDeserializer) {
    super(suppressExceptions, keyDeserializer);
  }

  @Override
  boolean write(Map map, Path path, Object value) {
    int segment = path.size() - 1;
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
    try {
      map.put(key, value);
    } catch (UnsupportedOperationException e) {
      return deadEnd(notModifiable(path, segment, Map.class));
    }
    return true;
  }

}
