package org.klojang.util.path;

import static org.klojang.util.ClassMethods.isPrimitiveArray;
import static org.klojang.util.path.PathWalkerException.nullValue;

import java.util.List;
import java.util.Map;

import org.klojang.util.path.ArraySegmentWriter;
import org.klojang.util.path.BeanSegmentWriter;
import org.klojang.util.path.KeyDeserializer;
import org.klojang.util.path.ListSegmentWriter;
import org.klojang.util.path.MapSegmentWriter;
import org.klojang.util.path.Path;
import org.klojang.util.path.PathWalker;
import org.klojang.util.path.PathWalkerException;
import org.klojang.util.path.PrimitiveArraySegmentWriter;

final class ObjectWriter {

  private final boolean se;
  private final KeyDeserializer kd;

  ObjectWriter(boolean suppressExceptions, KeyDeserializer keyDeserializer) {
    this.se = suppressExceptions;
    this.kd = keyDeserializer;
  }

  boolean write(Object host, Path path, Object value) {
    Object writeTo;
    int segment;
    if (path.size() == 1) {
      writeTo = host;
      segment = 0;
    } else {
      Path parent = path.parent();
      PathWalker pw = new PathWalker(path.parent(), false, kd);
      try {
        writeTo = pw.read(host);
      } catch (PathWalkerException e) {
        if (se) {
          return false;
        }
        throw e;
      }
      segment = parent.size() - 1;
    }
    if (writeTo == null) {
      return deadEnd(nullValue(path, segment));
    }
    if (writeTo instanceof List l) {
      return new ListSegmentWriter(se, kd).write(l, path, value);
    } else if (writeTo instanceof Map m) {
      return new MapSegmentWriter(se, kd).write(m, path, value);
    } else if (writeTo instanceof Object[] o) {
      return new ArraySegmentWriter(se, kd).write(o, path, value);
    } else if (isPrimitiveArray(writeTo)) {
      return new PrimitiveArraySegmentWriter(se, kd).write(writeTo, path, value);
    }
    return new BeanSegmentWriter(se, kd).write(writeTo, path, value);
  }

  boolean deadEnd(PathWalkerException.Factory excFactory) {
    if (se) {
      return false;
    }
    throw excFactory.get();
  }

}
