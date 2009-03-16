// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ReplacingFileUpdate extends FileUpdate {
  public ReplacingFileUpdate(UpdaterImplementation updater, String source, String destination) throws Exception {
    super(updater, source, destination);
  }

  public void doUpdate() throws Exception {
    if (destinationFile().exists())
      destinationFile().delete();
    super.doUpdate();
  }

  public boolean shouldBeApplied() throws Exception {
    if (super.shouldBeApplied())
      return true;
    else {
      URL resource = getResource(source);
      if (resource != null) {
        long sourceSum = checkSum(resource.openStream());
        long destinationSum = checkSum(new FileInputStream(destinationFile()));

        return sourceSum != destinationSum;
      } else
        return false;
    }
  }

  private long checkSum(InputStream input) throws IOException {
    try {
      long sum = 0;
      int b;
      while ((b = input.read()) != -1)
        sum += b;
      return sum;
    } finally {
      input.close();
    }

  }
}
