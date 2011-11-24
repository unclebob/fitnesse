// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import org.apache.velocity.VelocityContext;

import fitnesse.VelocityFactory;

public class HtmlPage {
  public static final String DTD = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"; // TR/html4/strict.DTD
  public static final String BreakPoint = "<!--BREAKPOINT-->";

  private String templateFileName;
  private String pageTitle = "FitNesse";
  private String bodyClass;
  
  public HtmlTag header;
  public HtmlTag actions;
  public HtmlTag main;

  public String preDivision;
  public String postDivision;

  protected HtmlPage(String templateFileName) {
    super();
    
    this.templateFileName = templateFileName;
    
    main = HtmlUtil.makeDivTag("main");
    header = HtmlUtil.makeDivTag("header");
    actions = HtmlUtil.makeDivTag("actions");
  }

  protected VelocityContext makeVelocityContext() {
    VelocityContext velocityContext = new VelocityContext();
    
    velocityContext.put("pageTitle", pageTitle);
    velocityContext.put("bodyClass", bodyClass);
    velocityContext.put("headerSection", header.html());
    velocityContext.put("mainSection", main.html());
    velocityContext.put("actionsSection", actions.html());
    return velocityContext;
  }

  public String html() {
    VelocityContext velocityContext = makeVelocityContext();
    return VelocityFactory.translateTemplate(velocityContext, templateFileName);
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
}
