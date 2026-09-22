package io.pillopl.library.commons.aggregates;

public record Version(int version) {

  public int getVersion() {
    return version;
  }

  public static Version zero() {
    return new Version(0);
  }
}
