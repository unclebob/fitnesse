// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import java.io.IOException;

public interface Update {
  public String getName();

  public String getMessage();

  public boolean shouldBeApplied() throws IOException;

  public void doUpdate() throws IOException;
}
