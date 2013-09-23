// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

public class WikiImporterFactory {
  public WikiImporter newImporter(WikiImporterClient client) {
    return new WikiImporter(client);
  }
}

