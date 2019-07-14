// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.refactoring;

import fitnesse.wiki.NoPruningStrategy;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.SymbolTreeWalker;

import java.util.Optional;

public abstract class ReferenceRenamer implements SymbolTreeWalker {
    protected WikiPage root;
    protected ReferenceRenamingTraverser traverser;

    ReferenceRenamer(WikiPage root) {
        this.root = root;
        this.traverser = new ReferenceRenamingTraverser(this, this::renameSymbolicLinkIfNeeded);
    }

    public void renameReferences() {
        root.getPageCrawler().traverse(traverser, new NoPruningStrategy());
    }

    WikiPage currentPage(){
        return traverser.currentPage();
    }

    abstract Optional<String> renameSymbolicLinkIfNeeded(String linkTarget);
}

