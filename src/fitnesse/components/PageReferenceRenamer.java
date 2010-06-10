// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.parser.Alias;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.parser.WikiWordPath;
import util.StringUtil;

import java.util.List;

public class PageReferenceRenamer extends ReferenceRenamer {
  private WikiPage subjectPage;
  private String newName;

  public PageReferenceRenamer(WikiPage root, WikiPage subjectPage, String newName) {
    super(root);
    this.subjectPage = subjectPage;
    this.newName = newName;
  }

    public boolean visit(Symbol node) {
        try {
            if (node.isType(SymbolType.WikiWord)) {
                wikiWordRenamePageIfReferenced(node, subjectPage, newName);
            }
            else if (node.isType(Alias.symbolType)) {
                if (new WikiWordPath().findLength(node.childAt(1).childAt(0).getContent()) > 0) {
                    wikiWordRenamePageIfReferenced(node.childAt(1).childAt(0), subjectPage, newName);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean visitChildren(Symbol node) {
        return !node.isType(Alias.symbolType);
    }

    private void wikiWordRenamePageIfReferenced(Symbol wikiWord, WikiPage pageToRename, String newName) throws Exception {
      String fullPathToReferent = getQualifiedWikiWord(wikiWord.getContent());
      WikiPagePath pathToPageBeingRenamed = pageToRename.getPageCrawler().getFullPath(pageToRename);
      pathToPageBeingRenamed.makeAbsolute();
      String absolutePathToPageBeingRenamed = PathParser.render(pathToPageBeingRenamed);

      if (refersTo(fullPathToReferent, absolutePathToPageBeingRenamed)) {
        int oldNameLength = absolutePathToPageBeingRenamed.length();
        String renamedPath = "." + rename(absolutePathToPageBeingRenamed.substring(1), newName);
        String pathAfterRenamedPage = fullPathToReferent.substring(oldNameLength);
        String fullRenamedPathToReferent = renamedPath + pathAfterRenamedPage;
        String renamedReference = makeRenamedRelativeReference(wikiWord.getContent(), PathParser.parse(fullRenamedPathToReferent));
        wikiWord.setContent(renamedReference);
      }
    }

    private String makeRenamedRelativeReference(String wikiWordText, WikiPagePath renamedPathToReferent) throws Exception {
        WikiPagePath parentPath = currentPage.getPageCrawler().getFullPath(currentPage.getParent());
      parentPath.makeAbsolute();

      if (wikiWordText.startsWith("."))
        return PathParser.render(renamedPathToReferent);
      else if (wikiWordText.startsWith("<")) {
        return buildBackwardSearchReference(parentPath, renamedPathToReferent);
      } else {
        boolean parentPathNotRenamed = renamedPathToReferent.startsWith(parentPath);
        if (parentPathNotRenamed) {
          WikiPagePath relativePath = renamedPathToReferent.subtractFromFront(parentPath);
          if (wikiWordText.startsWith("^") || wikiWordText.startsWith(">"))
            return ">" + PathParser.render(relativePath.getRest());
          else
            return PathParser.render(relativePath);
        }
      }
      return wikiWordText;
    }

    private String buildBackwardSearchReference(WikiPagePath parentPath, WikiPagePath renamedPathToReferent) {
      int branchPoint = findBranchPoint(parentPath.getNames(), renamedPathToReferent.getNames());
      List<String> referentPath = renamedPathToReferent.getNames();
      List<String> referentPathAfterBranchPoint = referentPath.subList(branchPoint, referentPath.size());
        return "<" + StringUtil.join(referentPathAfterBranchPoint, ".");
    }

    private int findBranchPoint(List<String> list1, List<String> list2) {
      int i;
      for (i = 0; i < list1.size(); i++) {
        if (!list1.get(i).equals(list2.get(i))) break;
      }
      return Math.max(0, i - 1);
    }

    private String rename(String oldQualifiedName, String newPageName) {
      String newQualifiedName;

      int lastDotIndex = oldQualifiedName.lastIndexOf(".");
      if (lastDotIndex < 1)
        newQualifiedName = newPageName;
      else
        newQualifiedName = oldQualifiedName.substring(0, lastDotIndex + 1) + newPageName;
      return newQualifiedName;
    }

}
