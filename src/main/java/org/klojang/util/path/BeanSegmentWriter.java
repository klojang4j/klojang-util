package org.klojang.util.path;

import static org.klojang.util.ObjectMethods.isEmpty;
import static org.klojang.util.path.PathWalkerException.*;

import org.klojang.util.invoke.BeanWriter;
import org.klojang.util.invoke.IllegalAssignmentException;
import org.klojang.util.invoke.NoPublicSettersException;
import org.klojang.util.invoke.NoSuchPropertyException;
import org.klojang.util.path.KeyDeserializer;
import org.klojang.util.path.Path;
import org.klojang.util.path.SegmentWriter;

@SuppressWarnings({"rawtypes", "unchecked"})
final class BeanSegmentWriter extends SegmentWriter<Object> {

  BeanSegmentWriter(boolean suppressExceptions, KeyDeserializer keyDeserializer) {
    super(suppressExceptions, keyDeserializer);
  }

  @Override
  boolean write(Object bean, Path path, Object value) {
    int segment = path.size() - 1;
    String property = path.segment(segment);
    if (isEmpty(property)) {
      return deadEnd(emptySegment(path, segment));
    }
    BeanWriter bw;
    try {
      bw = new BeanWriter(bean.getClass());
    } catch (NoPublicSettersException e) {
      return deadEnd(terminalValue(path, segment, bean.getClass()));
    }
    try {
      bw.write(bean, property, value);
      return true;
    } catch (NoSuchPropertyException e) {
      return deadEnd(noSuchProperty(path, segment, bean.getClass()));
    } catch (IllegalAssignmentException e) {
      return deadEnd(typeMismatch(path, segment, e.getMessage()));
    } catch (Throwable t) {
      return deadEnd(unexpectedError(path, segment, t));
    }
  }

}
