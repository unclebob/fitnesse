// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.wiki.WikiPage;
import fitnesse.wiki.PageData;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import org.json.JSONArray;
import util.StringUtil;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class NameWikiPageResponder extends BasicWikiPageResponder {
  protected String contentFrom(WikiPage requestedPage) {
    List<String> lines = addLines(requestedPage, "");

    String format = (String) request.getInput("format");
    if ("json".equalsIgnoreCase(format)) {
      JSONArray jsonPages = new JSONArray(lines);
      return jsonPages.toString();
    }
    return StringUtil.join(lines, System.getProperty("line.separator"));
  }

  private List<String> addLines(WikiPage requestedPage, String prefix) {
    List<String> lines = new ArrayList<String>();
	
    for (WikiPage child : requestedPage.getChildren()) {
	  if(!request.hasInput("LeafOnly") || child.getChildren().isEmpty()) {
        lines.add(makeLine(child, prefix));
      }
	  
	  if (request.hasInput("Recursive")) {
	    lines.addAll(addLines(child, prefix + child.getName() + "."));
	  }
    }
	
    return lines;
  }

  private String makeLine(WikiPage child, String prefix) {
    int numberOfChildren = child.getChildren().size();
	
    String line = prefix + child.getName();
	
    if (request.hasInput("ShowChildCount")) {
      line += " " + numberOfChildren;
    }
	
    if (request.hasInput("ShowTags")) {
	  Set<String> tags = getTags(child);
	  if(!tags.isEmpty()) {
	    line += " ";
		for(String tag : tags) {
          line += "[" + tag + "]";
		}
      }
	}
	
    return line;
  }
  
  private Set<String> getTags(WikiPage page) {
    Set<String> result = new TreeSet<String>();
	
	// get the tags of the given page
    String tags = page.getData().getAttribute(PageData.PropertySUITES);
    if((tags != null) && !tags.isEmpty()) {
	  result.addAll(Arrays.asList(tags.split(", ")));
    }
	
	// recusrively collect all tags up to the root
	if(!page.isRoot()) {
	  result.addAll(getTags(page.getParent()));
	}
	
	return result;
  }

  protected String getContentType() {
    return "text/plain";
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
