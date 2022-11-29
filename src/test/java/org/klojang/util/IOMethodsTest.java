package org.klojang.util;

import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.klojang.util.IOMethods.*;

public class IOMethodsTest {

  @Test
  public void getContents00() throws IOException {
    String s = getContents(getClass(), "/foo.txt");
    assertEquals("This is a test.", s);
    // Bad, but not forbidden:
    s = getContents(getClass(), "/foo.txt", 1);
    assertEquals("This is a test.", s);
    s = getContents(getClass(), "/foo.txt", 2048);
    assertEquals("This is a test.", s);
    try (InputStream is = getClass().getResourceAsStream(
        "/foo.txt")) {
      assertEquals("This is a test.", getContents(is));
    }
    try (InputStream is = getClass().getResourceAsStream(
        "/foo.txt")) {
      assertEquals("This is a test.", getContents(is, 13));
    }
    try (InputStream is = getClass().getResourceAsStream(
        "/foo.txt")) {
      assertEquals("This is a test.", getContents(is, 1));
    }
    try (InputStream is = getClass().getResourceAsStream(
        "/foo.txt")) {
      assertEquals("This is a test.", getContents(is, 2));
    }
    try (InputStream is = getClass().getResourceAsStream(
        "/foo.txt")) {
      assertEquals("This is a test.", getContents(is, "This is a test.".length()));
    }

  }

  @Test
  public void getContents01() throws IOException {
    File f = createTempFile();
    try {
      write(f.getPath(), "Hello, World!");
      String s = getContents(f.getPath());
      assertEquals("Hello, World!", s);
    } finally {
      f.delete();
    }
  }

  @Test
  public void write00() throws IOException {
    File f = createTempFile();
    try {
      write(f.getPath(), "Hello, World!");
      append(f.getPath(), " How are you?");
      String s = getContents(f.getPath());
      assertEquals("Hello, World! How are you?", s);
      append(f.getPath(), " I am fine.");
      s = getContents(f.getPath());
      assertEquals("Hello, World! How are you? I am fine.", s);
      write(f.getPath(), "Hello, World!");
      s = getContents(f.getPath());
      assertEquals("Hello, World!", s);
    } finally {
      rm(f.getPath());
    }
  }

  @Test
  public void read00() throws IOException {
    byte[] expected = "This is a test.".getBytes(StandardCharsets.UTF_8);
    byte[] bytes = readBytes(getClass(), "/foo.txt");
    assertArrayEquals(expected, bytes);
    // Bad, but not forbidden:
    bytes = readBytes(getClass(), "/foo.txt", 1);
    assertArrayEquals(expected, bytes);
    bytes = readBytes(getClass(), "/foo.txt", 2048);
    assertArrayEquals(expected, bytes);
    try (InputStream is = getClass().getResourceAsStream(
        "/foo.txt")) {
      assertArrayEquals(expected, readBytes(is));
    }
    try (InputStream is = getClass().getResourceAsStream(
        "/foo.txt")) {
      assertArrayEquals(expected, readBytes(is, 13));
    }
    try (InputStream is = getClass().getResourceAsStream(
        "/foo.txt")) {
      assertArrayEquals(expected, readBytes(is, 1));
    }
    try (InputStream is = getClass().getResourceAsStream(
        "/foo.txt")) {
      assertArrayEquals(expected, readBytes(is, 2));
    }
    try (InputStream is = getClass().getResourceAsStream(
        "/foo.txt")) {
      assertArrayEquals(expected, readBytes(is, expected.length));
    }

  }

  @Test
  public void pipe00() {
    byte[] bytes = "Hello, World!".getBytes(StandardCharsets.UTF_8);
    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    pipe(in, out);
    assertArrayEquals(bytes, out.toByteArray());
  }

  @Test
  public void pipe01() {
    byte[] bytes = "Hello, World!".getBytes(StandardCharsets.UTF_8);
    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    pipe(in, out, 1);
    assertArrayEquals(bytes, out.toByteArray());
  }

  @Test
  public void rm00() throws IOException {
    File dir0 = createTempDir(getClass(), "/foo/bar", null, true);
    assertTrue(dir0.exists());
    String name = "/foo/bar/" + dir0.getName();
    try {
      File f = createTempDir(getClass(), name, ".d", true);
      assertTrue(f.isDirectory());
      f = createTempDir(getClass(), name, ".dir", false);
      assertFalse(f.isDirectory());
      rm(f.getPath()); // not much happening here
      f.mkdirs();
      f = createTempFile(getClass(), name, ".tmp", true);
      assertTrue(f.isFile());
      f = createTempFile(getClass(), name, ".txt", false);
      assertFalse(f.isFile());
      rm(f); // not much happening here
      f.createNewFile();
      f = createTempFile(getClass(), name + "/zombie", ".tmp", true);
      assertTrue(f.isFile());
    } finally {
      rm(dir0.getPath());
    }
    assertFalse(dir0.exists());
  }

  @Test
  public void rm01() throws IOException {
    File f = createTempDir(Boolean.class, "dir", false);
    assertFalse(f.isDirectory());
    rm(f); // not much happening
    f = createTempDir(Boolean.class, "dir", true);
    assertTrue(f.isDirectory());
    rm(f);
    assertFalse(f.isDirectory());
  }

}
