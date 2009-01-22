// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.runner;

import java.io.InputStream;

public interface ResultFormatter extends ResultHandler {
  int getByteCount() throws Exception;

  InputStream getResultStream() throws Exception;
}
