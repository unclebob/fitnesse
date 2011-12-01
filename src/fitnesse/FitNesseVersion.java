// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

/**
 * Represents a version of a fitnesse release. Versions have the following format: v20100103[suffix]
 * I.e. the suffix is optional.
 **/
public class FitNesseVersion {
  private final String version;
	
  public FitNesseVersion() {
    this("v20111201");
  }

  public FitNesseVersion(String version) {
    this.version = version;
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
