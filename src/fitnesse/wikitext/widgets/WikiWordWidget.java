// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;
import fitnesse.components.PageReferencer;
import fitnesse.wikitext.WidgetVisitor;
import java.util.regex.Pattern;

public class WikiWordWidget extends TextWidget implements PageReferencer
{
	public static final String SINGLE_WIKIWORD_REGEXP = "\\b[A-Z](?:[a-z0-9]+[A-Z][a-z0-9]*)+";
	public static final String REGEXP = "(?:\\^|\\./|(?:\\.\\./)+)?(?:[./]?" + SINGLE_WIKIWORD_REGEXP + ")+\\b";

	public WikiPage parentPage;

	public WikiWordWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent, text);
		WikiPage wikiPage = getWikiPage();
		parentPage = wikiPage.getParent();
	}

	public String render() throws Exception
	{
		WikiPagePath pathOfWikiWord = PathParser.parse(getWikiWord());
		WikiPagePath fullPathOfWikiWord = parentPage.getPageCrawler().getFullPathOfChild(parentPage, pathOfWikiWord);
		String qualifiedName = PathParser.render(fullPathOfWikiWord);
		if(parentPage.getPageCrawler().pageExists(parentPage, PathParser.parse(getWikiWord())))
			return makeLinkToExistingWikiPage(qualifiedName);
		else
			return makeLinkToNonExistentWikiPage(qualifiedName);
	}

	private String makeLinkToNonExistentWikiPage(String qualifiedName)
	{
		StringBuffer html = new StringBuffer();
		html.append(getText());
		html.append("<a href=\"").append(qualifiedName);
		html.append("?edit");
		html.append("\">?</a>");
		return html.toString();
	}

	private String makeLinkToExistingWikiPage(String qualifiedName)
	{
		StringBuffer html = new StringBuffer();
		html.append("<a href=\"");
		html.append(qualifiedName).append("\">");
		html.append(getText()).append("</a>");
		return html.toString();
	}

	// If pageToRename is referenced somewhere in this wiki word (could be a parent, etc.),
	// rename it to newName.
	public void renamePageIfReferenced(WikiPage pageToRename, String newName) throws Exception
	{
		String qualifiedReference = getQualifiedWikiWord();
		WikiPagePath targetPath = pageToRename.getPageCrawler().getFullPath(pageToRename);
		targetPath.makeAbsolute();
		String qualifiedTarget = PathParser.render(targetPath);

		if(refersTo(qualifiedReference, qualifiedTarget))
		{
			int oldNameLength = qualifiedTarget.length();
			String newQualifiedTarget = "." + rename(qualifiedTarget.substring(1), newName);
			String newQualifiedReference = newQualifiedTarget + qualifiedReference.substring(oldNameLength);
			String renamedReference = makeRenamedRelativeReference(newQualifiedReference);
			setText(renamedReference);
		}
	}

	public void renameMovedPageIfReferenced(WikiPage pageToBeMoved, String newParentName) throws Exception
	{
		WikiPagePath pathOfPageToBeMoved = pageToBeMoved.getPageCrawler().getFullPath(pageToBeMoved);
		pathOfPageToBeMoved.makeAbsolute();
		String QualifiedNameOfPageToBeMoved = PathParser.render(pathOfPageToBeMoved);
		String reference = getQualifiedWikiWord();
		if(refersTo(reference, QualifiedNameOfPageToBeMoved))
		{
			String referenceTail = reference.substring(QualifiedNameOfPageToBeMoved.length());
			String childPortionOfReference = pageToBeMoved.getName();
			if(referenceTail.length() > 0)
				childPortionOfReference += referenceTail;
			String newQualifiedName;
			if("".equals(newParentName))
				newQualifiedName = "." + childPortionOfReference;
			else
				newQualifiedName = "." + newParentName + "." + childPortionOfReference;

			setText(newQualifiedName);
		}
	}

	public String makeRenamedRelativeReference(String newQualifiedReference) throws Exception
	{
		String rawReference = getText();
		String renamedRawReference = rawReference;
		try
		{
			if(rawReference.startsWith(".")) // absolute reference
				renamedRawReference = newQualifiedReference;
			else // relative reference
			{
				WikiPagePath parentPath = parentPage.getPageCrawler().getFullPath(parentPage);
				parentPath.makeAbsolute();
				String qualifiedReferenceToParent = PathParser.render(parentPath);

				boolean parentPrefixHasTrailingDot = !parentPage.getPageCrawler().isRoot(parentPage);
				String parentPrefix = qualifiedReferenceToParent + (parentPrefixHasTrailingDot ? "." : "");
				String referenceRemainder = newQualifiedReference.substring(parentPrefix.length());
				if(newQualifiedReference.startsWith(parentPrefix)) // It's not a component of the parent that is being renamed.
				{
					if(rawReference.startsWith("^"))
						renamedRawReference = "^" + referenceRemainder.substring(referenceRemainder.indexOf(".") + 1);
					else
						renamedRawReference = referenceRemainder;
				}
			}
		}
		catch(StringIndexOutOfBoundsException e)
		{
			// MDM Not quite sure why this happens but it causes trouble....
			// I suppose it can't hurt too much to use the qualified name for the time being.
			return newQualifiedReference;
		}
		return renamedRawReference;
	}

	static boolean refersTo(String qualifiedReference, String qualifiedTarget)
	{
		if(qualifiedReference.equals(qualifiedTarget))
			return true;
		if(qualifiedReference.startsWith(qualifiedTarget + "."))
			return true;
		return false;
	}

	private String getQualifiedWikiWord() throws Exception
	{
		String pathName = expandUparrow(getText());
		WikiPagePath expandedPath = PathParser.parse(pathName);
		if(expandedPath == null)
			return getText();
		WikiPagePath fullPath = parentPage.getPageCrawler().getFullPathOfChild(parentPage, expandedPath);
		return "." + PathParser.render(fullPath); //todo rcm 2/6/05 put that '.' into pathParser.  Perhaps WikiPagePath.setAbsolute()
	}

	private String rename(String oldQualifiedName, String newPageName)
	{
		String newQualifiedName = oldQualifiedName;

		int lastDotIndex = oldQualifiedName.lastIndexOf(".");
		if(lastDotIndex < 1)
			newQualifiedName = newPageName;
		else
			newQualifiedName = oldQualifiedName.substring(0, lastDotIndex + 1) + newPageName;
		return newQualifiedName;
	}

	String getWikiWord() throws Exception
	{
		String theWord = getText();
		theWord = expandUparrow(theWord);
		return theWord;
	}

	public static boolean isWikiWord(String word)
	{
		return Pattern.matches(REGEXP, word);
	}

	protected String expandUparrow(String theWord) throws Exception
	{
		if(theWord.charAt(0) == '^')
		{
			theWord = getWikiPage().getName() + "." + theWord.substring(1);
		}
		return theWord;
	}

	public WikiPage getReferencedPage() throws Exception
	{
		String theWord = getWikiWord();
		return parentPage.getPageCrawler().getPage(parentPage, PathParser.parse(theWord));
	}

	public void acceptVisitor(WidgetVisitor visitor) throws Exception
	{
		visitor.visit(this);
	}

	public static boolean isSingleWikiWord(String s)
	{
		return Pattern.matches(SINGLE_WIKIWORD_REGEXP, s);
	}
}
