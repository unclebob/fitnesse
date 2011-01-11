// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

public class FitNesseVersion {
  private final String version;
	
  public FitNesseVersion() {
<<<<<<< HEAD
    this("v20101228");
=======
    this("v20110106");
>>>>>>> 43c85b5ed8d19670bc67b52ffedeb6444bb41fc3
  }

  public FitNesseVersion(String version) {
    this.version = version;
  }

  public String toString() {
    return version;
  }

  public boolean isAtLeast(String requiredVersion) {
    long thisVersion = Long.parseLong(version.substring(1));
    long otherVersion = Long.parseLong(requiredVersion.substring(1));
    return thisVersion >= otherVersion;
  }
}
