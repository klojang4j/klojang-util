/**
 * Java module containing basic language extensions and utility classes. The module
 * is light-weight and self-contained, with zero dependencies outside
 * {@code java.base}. It is one of the main utility libraries underneath the Klojang
 * templating API (yet to be published on Maven Central). It centers around the
 * functionality described below.
 *
 * <h2>Naturalis Check</h2>
 *
 * @author Ayco Holleman
 */
module org.klojang.util {
  requires org.klojang.check;
  exports org.klojang.util;
  exports org.klojang.util.collection;
  exports org.klojang.util.exception;
  exports org.klojang.util.invoke;
  exports org.klojang.util.path;
  exports org.klojang.util.util;
}
