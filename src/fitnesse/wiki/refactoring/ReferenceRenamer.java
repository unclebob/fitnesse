// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.refactoring;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiSourcePage;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolTreeWalker;
import fitnesse.wikitext.parser.WikiTranslator;

public abstract class ReferenceRenamer implements SymbolTreeWalker {
    protected WikiPage root;
    protected ReferenceRenamingTraverser traverser;

    ReferenceRenamer(WikiPage root) {
        this.root = root;
        this.traverser = new ReferenceRenamingTraverser(this);
    }

    public void renameReferences() {
        root.getPageCrawler().traverse(traverser);
    }

    WikiPage currentPage(){
        return traverser.currentPage();
    }
}

class ReferenceRenamingTraverser implements TraversalListener<WikiPage> {
    private final SymbolTreeWalker walker;
    private WikiPage currentPage;

    ReferenceRenamingTraverser(SymbolTreeWalker walker) {
        this.walker = walker;
    }

    @Override
    public void process(WikiPage currentPage) {
        PageData data = currentPage.getData();
        String content = data.getContent();

        Symbol syntaxTree = Parser.make(
                new ParsingPage(new WikiSourcePage(currentPage)),
                content,
                SymbolProvider.refactoringProvider)
                .parse();
        this.currentPage = currentPage;
        syntaxTree.walkPreOrder(walker);
        String newContent = new WikiTranslator(new WikiSourcePage(currentPage)).translateTree(syntaxTree);

        boolean pageHasChanged = !newContent.equals(content);
        if (pageHasChanged) {
            data.setContent(newContent);
            currentPage.commit(data);
        }
    }

    WikiPage currentPage() {
        return currentPage;
    }
}

