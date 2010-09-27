package fitnesse.wiki;

import fitnesse.wikitext.parser.Symbol;
import util.StringUtil;

import java.util.Arrays;
import java.util.List;

public class WikiWordReference {
    private WikiPage currentPage;
    private String wikiWord;

    public WikiWordReference(WikiPage currentPage, String wikiWord) {
        this.currentPage = currentPage;
        this.wikiWord = wikiWord;
    }

    public WikiPage getReferencedPage() throws Exception {
        String theWord = expandPrefix(wikiWord);
        WikiPage parentPage = currentPage.getParent();
        return parentPage.getPageCrawler().getPage(parentPage, PathParser.parse(theWord));
    }
    
    private String expandPrefix(String theWord) throws Exception {
      PageCrawler crawler = currentPage.getPageCrawler();
      if (theWord.charAt(0) == '^' || theWord.charAt(0) == '>') {
        String prefix = currentPage.getName();
        return String.format("%s.%s", prefix, theWord.substring(1));
      } else if (theWord.charAt(0) == '<') {
        String undecoratedPath = theWord.substring(1);
        String[] pathElements = undecoratedPath.split("\\.");
        String target = pathElements[0];
        //todo rcm, this loop is duplicated in PageCrawlerImpl.getSiblingPage
        for (WikiPage current = currentPage.getParent(); !crawler.isRoot(current); current = current.getParent()) {
          if (current.getName().equals(target)) {
            pathElements[0] = PathParser.render(crawler.getFullPath(current));
            return "." + StringUtil.join(Arrays.asList(pathElements), ".");
          }
        }
        return "." + undecoratedPath;
      }
      return theWord;
    }

    public void wikiWordRenameMovedPageIfReferenced(Symbol wikiWord, WikiPage pageToBeMoved, String newParentName) throws Exception {
      WikiPagePath pathOfPageToBeMoved = pageToBeMoved.getPageCrawler().getFullPath(pageToBeMoved);
      pathOfPageToBeMoved.makeAbsolute();
      String QualifiedNameOfPageToBeMoved = PathParser.render(pathOfPageToBeMoved);
      String reference = getQualifiedWikiWord(wikiWord.getContent());
      if (refersTo(reference, QualifiedNameOfPageToBeMoved)) {
        String referenceTail = reference.substring(QualifiedNameOfPageToBeMoved.length());
        String childPortionOfReference = pageToBeMoved.getName();
        if (referenceTail.length() > 0)
          childPortionOfReference += referenceTail;
        String newQualifiedName;
        if ("".equals(newParentName))
          newQualifiedName = "." + childPortionOfReference;
        else
          newQualifiedName = "." + newParentName + "." + childPortionOfReference;

        wikiWord.setContent(newQualifiedName);
      }
    }

    private String getQualifiedWikiWord(String wikiWordText) throws Exception {
      String pathName = expandPrefix(wikiWordText);
      WikiPagePath expandedPath = PathParser.parse(pathName);
      if (expandedPath == null)
        return wikiWordText;
      WikiPagePath fullPath = currentPage.getParent().getPageCrawler().getFullPathOfChild(currentPage.getParent(), expandedPath);
      return "." + PathParser.render(fullPath); //todo rcm 2/6/05 put that '.' into pathParser.  Perhaps WikiPagePath.setAbsolute()
    }

    private boolean refersTo(String qualifiedReference, String qualifiedTarget) {
        return qualifiedReference.equals(qualifiedTarget) || qualifiedReference.startsWith(qualifiedTarget + ".");
    }

    public void wikiWordRenamePageIfReferenced(Symbol wikiWord, WikiPage pageToRename, String newName) throws Exception {
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

    private String rename(String oldQualifiedName, String newPageName) {
      String newQualifiedName;

      int lastDotIndex = oldQualifiedName.lastIndexOf(".");
      if (lastDotIndex < 1)
        newQualifiedName = newPageName;
      else
        newQualifiedName = oldQualifiedName.substring(0, lastDotIndex + 1) + newPageName;
      return newQualifiedName;
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
}
