// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.refactoring;

import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiWordReference;

import java.util.Optional;

public class PageReferenceRenamer implements ChangeReference {
  private final WikiPage subjectPage;
  private final String newName;

  public PageReferenceRenamer(WikiPage subjectPage, String newName) {
    this.subjectPage = subjectPage;
    this.newName = newName;
  }

  @Override
  public Optional<String> changeReference(WikiPage currentPage, String reference) {
    return new WikiWordReference(currentPage, reference).getRenamedContent(reference, subjectPage, newName);
  }
}
