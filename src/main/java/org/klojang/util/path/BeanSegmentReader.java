package org.klojang.util.path;

import static org.klojang.util.ObjectMethods.isEmpty;
import static org.klojang.util.path.PathWalkerException.*;

import org.klojang.util.invoke.BeanReader;
import org.klojang.util.invoke.NoPublicGettersException;
import org.klojang.util.invoke.NoSuchPropertyException;
import org.klojang.util.path.KeyDeserializer;
import org.klojang.util.path.ObjectReader;
import org.klojang.util.path.Path;
import org.klojang.util.path.SegmentReader;

@SuppressWarnings({"rawtypes", "unchecked"})
final class BeanSegmentReader extends SegmentReader<Object> {

  BeanSegmentReader(boolean suppressExceptions, KeyDeserializer keyDeserializer) {
    super(suppressExceptions, keyDeserializer);
  }

  @Override
  Object read(Object bean, Path path, int segment) {
    String property = path.segment(segment);
    if (isEmpty(property)) {
      return deadEnd(emptySegment(path, segment));
    }
    BeanReader reader;
    try {
      reader = new BeanReader(bean.getClass());
    } catch (NoPublicGettersException e) {
      return deadEnd(terminalValue(path, segment, bean.getClass()));
    }
    try {
      Object val = reader.read(bean, path.segment(segment));
      return new ObjectReader(se, kd).read(val, path, ++segment);
    } catch (NoSuchPropertyException e) {
      return deadEnd(noSuchProperty(path, segment, bean.getClass()));
    }
  }

}
