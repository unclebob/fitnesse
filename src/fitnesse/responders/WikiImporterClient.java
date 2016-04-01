// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.io.IOException;

import fitnesse.wiki.WikiPage;

public interface WikiImporterClient {
  void pageImported(WikiPage localPage) throws IOException;

  void pageImportError(WikiPage localPage, Exception e) throws IOException;
}
