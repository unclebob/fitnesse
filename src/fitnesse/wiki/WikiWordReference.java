package fitnesse.wiki;

import fit.exception.IncorrectPathException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class WikiWordReference {
  private final WikiPage currentPage;
  private final String wikiWord;

  public WikiWordReference(WikiPage currentPage, String wikiWord) {
    this.currentPage = currentPage;
    this.wikiWord = wikiWord;
  }

  public WikiPage getReferencedPage() {
    String theWord = expandPrefix(currentPage, wikiWord);
    WikiPage parentPage = currentPage.getParent();
    return parentPage.getPageCrawler().getPage(PathParser.parse(theWord));
  }

  public static String expandPrefix(WikiPage wikiPage, String theWord) {
    if (theWord.equals("")) {
      throw new IncorrectPathException("Path to Page must be filled in");
    } else if (theWord.charAt(0) == '^' || theWord.charAt(0) == '>') {
      String prefix = wikiPage.getName();
      return String.format("%s.%s", prefix, theWord.substring(1));
    } else if (theWord.charAt(0) == '<') {
      String undecoratedPath = theWord.substring(1);
      String[] pathElements = undecoratedPath.split("\\.");
      String target = pathElements[0];
      for (WikiPage current = wikiPage.getParent(); !current.isRoot(); current = current.getParent()) {
        if (current.getName().equals(target)) {
          pathElements[0] = PathParser.render(current.getFullPath());
          return "." + StringUtils.join(Arrays.asList(pathElements), ".");
        }
      }
      return "." + undecoratedPath;
    }
    return theWord;
  }

  public Optional<String> getMovedPageRenamedContent(String content, WikiPage pageToBeMoved, String newParentName) {
    WikiPagePath pathOfPageToBeMoved = pageToBeMoved.getFullPath();
    pathOfPageToBeMoved.makeAbsolute();
    String qualifiedNameOfPageToBeMoved = PathParser.render(pathOfPageToBeMoved);
    String reference = getQualifiedWikiWord(content);
    Optional<String> renameMovedContent;
    if (refersTo(reference, qualifiedNameOfPageToBeMoved)) {
      String referenceTail = reference.substring(qualifiedNameOfPageToBeMoved.length());
      String childPortionOfReference = pageToBeMoved.getName();
      if (!referenceTail.isEmpty()) {
        childPortionOfReference += referenceTail;
      }
      String newQualifiedName;
      if ("".equals(newParentName)) {
        newQualifiedName = "." + childPortionOfReference;
      } else {
        newQualifiedName = "." + newParentName + "." + childPortionOfReference;
      }
      renameMovedContent = Optional.of(newQualifiedName);
    } else {
      renameMovedContent = Optional.empty();
    }
    return renameMovedContent;
  }

  private String getQualifiedWikiWord(String wikiWordText) {
    String pathName = expandPrefix(currentPage, wikiWordText);
    WikiPagePath expandedPath = PathParser.parse(pathName);
    if (expandedPath == null)
      return wikiWordText;
    WikiPagePath fullPath = currentPage.getParent().getPageCrawler().getFullPathOfChild(expandedPath);
    return "." + PathParser.render(fullPath); //todo rcm 2/6/05 put that '.' into pathParser.  Perhaps WikiPagePath.setAbsolute()
  }

  private boolean refersTo(String qualifiedReference, String qualifiedTarget) {
    return qualifiedReference.equals(qualifiedTarget) || qualifiedReference.startsWith(qualifiedTarget + ".");
  }

  public Optional<String> getRenamedContent(String content, WikiPage pageToRename, String newName) {
    String fullPathToReferent = getQualifiedWikiWord(content);
    WikiPagePath pathToPageBeingRenamed = pageToRename.getFullPath();
    pathToPageBeingRenamed.makeAbsolute();
    String absolutePathToPageBeingRenamed = PathParser.render(pathToPageBeingRenamed);

    Optional<String> renamedContent;
    if (refersTo(fullPathToReferent, absolutePathToPageBeingRenamed)) {
      int oldNameLength = absolutePathToPageBeingRenamed.length();
      String renamedPath = "." + rename(absolutePathToPageBeingRenamed.substring(1), newName);
      String pathAfterRenamedPage = fullPathToReferent.substring(oldNameLength);
      String fullRenamedPathToReferent = renamedPath + pathAfterRenamedPage;
      String renamedReference = makeRenamedRelativeReference(content, PathParser.parse(fullRenamedPathToReferent));
      renamedContent = Optional.of(renamedReference);
    } else {
      renamedContent = Optional.empty();
    }
    return renamedContent;
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

  private String makeRenamedRelativeReference(String wikiWordText, WikiPagePath renamedPathToReferent) {
    WikiPage parent = currentPage.getParent();
    WikiPagePath parentPath = parent.getFullPath();
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
    return "<" + StringUtils.join(referentPathAfterBranchPoint, ".");
  }

  private int findBranchPoint(List<String> list1, List<String> list2) {
    int i;
    for (i = 0; i < list1.size(); i++) {
      if (!list1.get(i).equals(list2.get(i))) break;
    }
    return Math.max(0, i - 1);
  }
}
