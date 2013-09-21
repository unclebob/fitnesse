// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Represents a version of a fitnesse release. Versions have the following format: v20100103[suffix]
 * I.e. the suffix is optional.
 **/
public class FitNesseVersion {
  private final String version;

  public FitNesseVersion() {
    this(versionFromMetaInf());
  }

  public FitNesseVersion(String version) {
    this.version = version;
  }

  private static String versionFromMetaInf() {
    InputStream is = null;
    try {
      is = FitNesseVersion.class.getResourceAsStream("/META-INF/FitNesseVersion.txt");
      if (is == null) {
        return "unknown";
      }
      byte[] b = new byte[64];
      int len = is.read(b);
      return new String(b, 0, len, Charset.forName("ISO-8859-1"));
    } catch (IOException e) {
      e.printStackTrace();
      return "unknown";
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public String toString() {
    return version;
  }

  static Long dateVersion(String fullTextVersion) {
    try {
      return Long.parseLong(fullTextVersion.substring(1, Math.min(9, fullTextVersion.length())));
    } catch (Exception e) {
      throw new IllegalArgumentException("Unable to extract date version from " + fullTextVersion);
    }
  }

  /**
   * Compare this instance's version against the specified one.
   * Note: the suffixes are ignored when comparing versions
   * @return true if this version is not younger than the specified one, suffix excluded
   **/
  public boolean isAtLeast(String requiredVersion) {
    long thisVersion = dateVersion(version);
    long otherVersion = dateVersion(requiredVersion);
    return thisVersion >= otherVersion;
  }
}
