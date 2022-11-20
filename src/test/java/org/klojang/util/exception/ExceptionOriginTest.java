package org.klojang.util.exception;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.klojang.util.ArrayMethods;
import org.klojang.util.StringMethods;
import org.klojang.util.exception.ExceptionOrigin;
import org.klojang.util.path.Path;

import java.io.IOException;

public class ExceptionOriginTest {

  private static Exception exception1;
  private static Exception exception2 = new Exception("Something is really wrong");

  @BeforeClass
  public static void beforeClass() {
    StackTraceElement ste0 =
        new StackTraceElement(ExceptionOriginTest.class.getName(),
            "fooMethod1",
            "Foo.java",
            8);
    StackTraceElement ste1 =
        new StackTraceElement(ExceptionOriginTest.class.getName(),
            "fooMethod2",
            "Foo.java",
            17);
    StackTraceElement ste2 =
        new StackTraceElement(StringMethods.class.getName(),
            "barMethod1",
            "Bar.java",
            211);
    StackTraceElement ste3 =
        new StackTraceElement(Path.class.getName(), "bazMethod1", "Baz.java", 180);
    exception1 = new Exception("Something is wrong");
    exception1.setStackTrace(ArrayMethods.pack(ste0, ste1, ste2, ste3));

    exception2.setStackTrace(new StackTraceElement[0]);
  }

  @Test
  public void test01() {
    ExceptionOrigin eo = new ExceptionOrigin(exception1, "org.klojang.util");
    System.out.println(eo.getDetailedMessage());
    assertEquals(
        "java.lang.Exception ***  at org.klojang.util.exception.ExceptionOriginTest.fooMethod1 (line 8) *** Something is wrong *** absolute origin of exception: yes!",
        eo.getDetailedMessage());
  }

  @Test
  public void test02() {
    ExceptionOrigin eo = new ExceptionOrigin(exception1, "klojang");
    System.out.println(eo.getDetailedMessage());
    assertEquals(
        "java.lang.Exception ***  at org.klojang.util.exception.ExceptionOriginTest.fooMethod1 (line 8) *** Something is wrong *** absolute origin of exception: yes!",
        eo.getDetailedMessage());
  }

  @Test
  public void test03() {
    ExceptionOrigin eo = new ExceptionOrigin(exception1, "org.klojang.util.path");
    System.out.println(eo.getDetailedMessage());
    assertEquals(
        "java.lang.Exception ***  at org.klojang.util.path.Path.bazMethod1 (line 180) *** Something is wrong *** absolute origin of exception: org.klojang.util.exception.ExceptionOriginTest.fooMethod1 (line 8)",
        eo.getDetailedMessage());
  }

  @Test
  public void test04() {
    ExceptionOrigin eo = new ExceptionOrigin(exception1, "com.example");
    System.out.println(eo.getDetailedMessage());
    assertEquals(
        "java.lang.Exception ***  (not originating from \"com.example\") *** Something is wrong *** absolute origin of exception: org.klojang.util.exception.ExceptionOriginTest.fooMethod1 (line 8)",
        eo.getDetailedMessage());
  }

  @Test
  public void test05() throws IOException {
    ExceptionOrigin eo = new ExceptionOrigin(exception2, "com.example");
    System.out.println(eo.getDetailedMessage());
    assertEquals(
        "java.lang.Exception ***  (no stack trace available) *** Something is really wrong *** absolute origin of exception: yes!",
        eo.getDetailedMessage());
  }

}
