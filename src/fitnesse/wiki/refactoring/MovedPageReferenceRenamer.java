// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.refactoring;

import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiWordReference;

import java.util.Optional;

public class MovedPageReferenceRenamer implements ChangeReference {
  private final WikiPage pageToBeMoved;
  private final String newParentName;

  public MovedPageReferenceRenamer(WikiPage pageToBeMoved, String newParentName) {
    this.pageToBeMoved = pageToBeMoved;
    this.newParentName = newParentName;
  }

  @Override
  public Optional<String> changeReference(WikiPage currentPage, String reference) {
    return new WikiWordReference(currentPage, reference).getMovedPageRenamedContent(reference, pageToBeMoved, newParentName);
  }
}
