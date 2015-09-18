// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import fitnesse.FitNesseContext;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SimpleResponse extends Response {
  private byte[] content = new byte[0];
  private String pathtagsfile1 = "src/fitnesse/resources/bootstrap/js/pathtags.js";
  private String pathtagsfile2 = "classes/fitnesse/resources/bootstrap/js/pathtags.js";

  public SimpleResponse() {
    super("html");
  }

  public SimpleResponse(int status) {
    super("html", status);
  }

  @Override
  public void sendTo(ResponseSender sender) {
    try {
      sender.send(makeHttpHeaders().getBytes());
      sender.send(content);
    } finally {
      sender.close();
    }
  }

  public void setContent(String value) {
    content = getEncodedBytes(value);
  }

  public void setContent(byte[] value) {
    content = Arrays.copyOf(value, value.length);
  }

  @Override
  public String toString() {
    return String.format("status = %s,  contentType = %s, content = %s",
        getStatus(), getContentType(), getContent());
  }

  public String getContent() {
    return new String(content);
  }

  public byte[] getContentBytes() {
    return Arrays.copyOf(content, content.length);
  }

  @Override
  public int getContentSize() {
    return content.length;
  }

  @Override
  protected void addContentHeaders() {
    super.addContentHeaders();
    addHeader("Content-Length", String.valueOf(getContentSize()));
  }

  // create tree structure with suites or testcases
  public List<String> createTree(List<String> tree, FitNesseContext context, String root, String type) {
    PageCrawler crawler = context.getRootPage().getPageCrawler();
    WikiPagePath RootPath = PathParser.parse(root);
    WikiPage RootPage = crawler.getPage(RootPath);

    if (RootPage != null) {
      // only create tree if there are nodes
      if (RootPage.getChildren().size()>0) {
        // iterate through all childs of the root page
        List<WikiPage> NodeList = RootPage.getChildren();

        WikiPage CurrentNode;
        for (int i = 0; i < NodeList.size(); i++) {
          CurrentNode = NodeList.get(i);
          String currentNodePath = getPathOfCurrentElement(CurrentNode.toString());
          // only get suites or testcases
          if (CurrentNode.getData().hasAttribute("Suite") || CurrentNode.getData().hasAttribute("Test")) {
            // if CurrentNode is suite
            if (CurrentNode.getChildren().size() > 0) {
              // get suites only if requested type is "suite"
              if (CurrentNode.getData().hasAttribute("Suite") && type.equals("Suite")) {
                tree.add(currentNodePath);
                //System.out.println("Final Suite: " + CurrentNode.getParent().getName() + "." + CurrentNode.getName());
              }
              // recursive call for suites that are laying deeper in the tree
              createTree(tree, context, currentNodePath, type);
            }
            else {
              // if CurrentNode has no children and it is a test, then final test node found
              if (CurrentNode.getData().hasAttribute("Test") && type.equals("Test")) {
                tree.add(currentNodePath);
                System.out.println("Final Test: " + CurrentNode.getParent().getName() + "." + CurrentNode.getName());
              }
              // if CurrentNode has no children and it is a suite, then final suite node found
              if (CurrentNode.getData().hasAttribute("Suite") && type.equals("Suite")) {
                tree.add(currentNodePath);
                System.out.println("Final Suite: " + CurrentNode.getParent().getName() + "." + CurrentNode.getName());
              }
            }
          }
        }
      }
    }
    return tree;
  }
  // modify path string to usable form
  public String getPathOfCurrentElement (String currentNodeToString) {
    // needs Node.toString() as input
    String[] parts = currentNodeToString.split("\\.");
    // only take the path part
    String path = parts[(parts.length-1)];
    // cut the first 14 chars
    path = path.substring(14);
    // cut the last 7 chars
    path = path.substring(0,path.length()-7);
    // replace \ by .
    if (path.contains("\\")) {
      path = path.replace("\\", ".");
    }
    return path;
  }

  public void writeTreeListToJsFile (List<String> tests, List<String> suites) {
    BufferedWriter writer1 = null;
    BufferedWriter writer2 = null;
    String temp = "";
    try {
      //create files
      File jsFile1 = new File(pathtagsfile1);
      File jsFile2 = new File(pathtagsfile2);

      // testcases
      writer1 = new BufferedWriter(new FileWriter(jsFile1));
      writer2 = new BufferedWriter(new FileWriter(jsFile2));
      temp += "$(function() {\n";
      temp += "var autocompleteTests = [";
      for (int i = 0; i < tests.size(); i++) {
        temp = temp + '"' + tests.get(i) + "\",";
      }
      // erase ,
      temp = temp.substring(0, temp.length()-1);
      temp += "];\n";
      temp += "$(\"#pathTests\").autocomplete({source: autocompleteTests});\n\n});";

      // suites
      temp += "$(function() {\n";
      temp += "var autocompleteSuites = [";
      for (int i = 0; i < suites.size(); i++) {
        temp = temp + '"' + suites.get(i) + "\",";
      }
      // erase ,
      temp = temp.substring(0, temp.length()-1);
      temp += "];\n";
      temp += "$(\"#pathSuites\").autocomplete({source: autocompleteSuites});\n});";
      writer1.write(temp);
      writer2.write(temp);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      try {
        // close the writer regardless of what happens...
        writer1.close();
        writer2.close();
      }
      catch (Exception e) {
      }
    }
  }

  public void createAutocomplete(FitNesseContext context) {
    List<String> TestList = new ArrayList<String>();
    List<String> SuiteList = new ArrayList<String>();
    System.out.println("<<<<<<<<<<<<<<<<<<<< TEST >>>>>>>>>>>>>>>>>>>>>>");
    TestList = createTree(TestList, context, "/", "Test");
    System.out.println("<<<<<<<<<<<<<<<<<<<< SUITE >>>>>>>>>>>>>>>>>>>>>>");
    SuiteList = createTree(SuiteList, context, "/", "Suite");
    if ((TestList.size() > 0) || (SuiteList.size() > 0)) {
      Collections.sort(TestList);
      Collections.sort(SuiteList);
      writeTreeListToJsFile(TestList, SuiteList);
    }
  }
}
