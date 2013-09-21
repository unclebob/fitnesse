// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

public class MockWikiImporterFactory extends WikiImporterFactory {
  public MockWikiImporter mockWikiImporter = new MockWikiImporter();

  public WikiImporter newImporter(WikiImporterClient client) {
    mockWikiImporter.setWikiImporterClient(client);
    return mockWikiImporter;
  }
}
