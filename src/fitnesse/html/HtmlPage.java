// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import org.apache.velocity.VelocityContext;

import fitnesse.VelocityFactory;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.ProxyPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageActions;
import fitnesse.wiki.WikiPagePath;

public class HtmlPage {
  public static final String BreakPoint = "<!--BREAKPOINT-->";

  private VelocityContext velocityContext;
  
  private String templateFileName;
  private String pageTitle = "FitNesse";
  private String bodyClass;
  
  public HtmlTag header;
  public WikiPageActions actions;
  public HtmlTag main;

  public String preDivision;
  public String postDivision;

  protected HtmlPage(String templateFileName) {
    super();
    
    velocityContext =  new VelocityContext();
    this.templateFileName = templateFileName;
    
    main = HtmlUtil.makeDivTag("main");
    header = HtmlUtil.makeDivTag("header");
  }

  protected VelocityContext updateVelocityContext() throws Exception {
    velocityContext.put("pageTitle", pageTitle);
    velocityContext.put("bodyClass", bodyClass);
    makeSidebarSection();
    velocityContext.put("headerSection", header.html());
    velocityContext.put("mainSection", main.html());
    return velocityContext;
  }

  public void put(String key, Object value) {
    velocityContext.put(key, value);
  }
  
  public String html() throws Exception {
    VelocityContext context = updateVelocityContext();
    return VelocityFactory.translateTemplate(context, templateFileName);
  }


  public void setTitle(String title) {
    this.pageTitle = title;
  }
  
  public void divide() throws Exception {
    String html = html();
    int breakIndex = html.indexOf(BreakPoint);
    preDivision = html.substring(0, breakIndex);
    postDivision = html.substring(breakIndex + BreakPoint.length());
  }

  public void setBodyClass(String clazz) {
    bodyClass = clazz;
  }
  
  public void makeSidebarSection() throws Exception {
    velocityContext.put("actions", actions);
    if (actions != null) {
      velocityContext.put("localPath", actions.getLocalPageName());
      velocityContext.put("localOrRemotePath", actions.getLocalOrRemotePageName());
      velocityContext.put("openInNewWindow", actions.isNewWindowIfRemote());
    }
  }
  
}
