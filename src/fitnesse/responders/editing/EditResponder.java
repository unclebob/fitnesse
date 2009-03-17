// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.SaveRecorder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.MockingPageCrawler;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.Utils;

public class EditResponder implements SecureResponder {
  public static final String CONTENT_INPUT_NAME = "pageContent";
  public static final String SAVE_ID = "saveId";
  public static final String TICKET_ID = "ticketId";

  protected String content;
  protected WikiPage page;
  protected WikiPage root;
  protected PageData pageData;
  protected Request request;

  public EditResponder() {
  }

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    boolean nonExistent = request.hasInput("nonExistent") ? true : false;
    return doMakeResponse(context, request, nonExistent);
  }

  public Response makeResponseForNonExistentPage(FitNesseContext context, Request request) throws Exception {
    return doMakeResponse(context, request, true);
  }

  protected Response doMakeResponse(FitNesseContext context, Request request, boolean firstTimeForNewPage)
    throws Exception {
    initializeResponder(context.root, request);

    SimpleResponse response = new SimpleResponse();
    String resource = request.getResource();
    WikiPagePath path = PathParser.parse(resource);
    PageCrawler crawler = context.root.getPageCrawler();
    if (!crawler.pageExists(root, path)) {
      crawler.setDeadEndStrategy(new MockingPageCrawler());
      page = crawler.getPage(root, path);
    } else
      page = crawler.getPage(root, path);

    pageData = page.getData();
    content = createPageContent();

    String html = doMakeHtml(resource, context, firstTimeForNewPage);

    response.setContent(html);
    response.setMaxAge(0);

    return response;
  }


  protected void initializeResponder(WikiPage root, Request request) {
    this.root = root;
    this.request = request;
  }

  protected String createPageContent() throws Exception {
    return pageData.getContent();
  }

  public String makeHtml(String resource, FitNesseContext context) throws Exception {
    return doMakeHtml(resource, context, false);
  }

  private String doMakeHtml(String resource, FitNesseContext context, boolean firstTimeForNewPage)
    throws Exception {
    HtmlPage html = context.htmlPageFactory.newPage();
    String title = firstTimeForNewPage ? "Page doesn't exist. Edit " : "Edit ";
    html.title.use(title + resource + ":");
    html.body.addAttribute("onload", "document.f." + CONTENT_INPUT_NAME + ".focus()");
    HtmlTag header = makeHeader(resource, title, firstTimeForNewPage);
    html.header.use(header);
    html.main.use(makeEditForm(resource, firstTimeForNewPage, context.defaultNewPageContent));

    return html.html();
  }

  private HtmlTag makeHeader(String resource, String title, boolean firstTimeForNewPage) throws Exception {
    return HtmlUtil.makeBreadCrumbsWithPageType(resource, title + "Page:");
  }

  private HtmlTag makeEditForm(String resource, boolean firstTimeForNewPage, String defaultNewPageContent) throws Exception {
    HtmlTag form = new HtmlTag("form");
    form.addAttribute("name", "f");
    form.addAttribute("action", resource);
    form.addAttribute("method", "post");
    form.add(HtmlUtil.makeInputTag("hidden", "responder", "saveData"));
    form.add(HtmlUtil.makeInputTag("hidden", SAVE_ID, String.valueOf(SaveRecorder.newIdNumber())));
    form.add(HtmlUtil.makeInputTag("hidden", TICKET_ID, String.valueOf((SaveRecorder.newTicket()))));
    if (request.hasInput("redirectToReferer") && request.hasHeader("Referer")) {
      String redirectUrl = request.getHeader("Referer").toString();
      int questionMarkIndex = redirectUrl.indexOf("?");
      if (questionMarkIndex > 0)
        redirectUrl = redirectUrl.substring(0, questionMarkIndex);
      redirectUrl += "?" + request.getInput("redirectAction").toString();
      form.add(HtmlUtil.makeInputTag("hidden", "redirect", redirectUrl));
    }

    form.add(createTextarea(firstTimeForNewPage, defaultNewPageContent));
    form.add(createButtons());
    form.add(createOptions());
    form.add("<div class=\"hints\"><br />Hints:\n<ul>" +
      "<li>Use alt+s (Windows) or control+s (Mac OS X) to save your changes. Or, tab from the text area to the \"Save\" button!</li>\n" +
      "<li>Grab the lower-right corner of the text area to increase its size (works with some browsers).</li>\n" +
      "</ul></div>");

    TagGroup group = new TagGroup();
    group.add(form);

    return group;
  }
  
  private HtmlTag createOptions() throws Exception {
    HtmlTag options = HtmlUtil.makeDivTag("edit_options");
    options.add(makeScriptOptions());
    return options;
  }

  private HtmlTag makeScriptOptions() {
    TagGroup scripts = new TagGroup();

    includeJavaScriptFile("/files/javascript/textareaWrapSupport.js", scripts);

    return scripts;
  }

  private HtmlTag createButtons() throws Exception {
    HtmlTag buttons = HtmlUtil.makeDivTag("edit_buttons");
    buttons.add(makeSaveButton());
    buttons.add(makeScriptButtons());
    return buttons;
  }

  private HtmlTag makeScriptButtons() {
    TagGroup scripts = new TagGroup();

    includeJavaScriptFile("/files/javascript/SpreadsheetTranslator.js", scripts);
    includeJavaScriptFile("/files/javascript/spreadsheetSupport.js", scripts);
    includeJavaScriptFile("/files/javascript/WikiFormatter.js", scripts);
    includeJavaScriptFile("/files/javascript/wikiFormatterSupport.js", scripts);
    includeJavaScriptFile("/files/javascript/fitnesse.js", scripts);

    return scripts;
  }

  private void includeJavaScriptFile(String jsFile, TagGroup scripts) {
    HtmlTag scriptTag = HtmlUtil.makeJavascriptLink(jsFile);
    scripts.add(scriptTag);
  }

  private HtmlTag makeSaveButton() {
    HtmlTag saveButton = HtmlUtil.makeInputTag("submit", "save", "Save");
    saveButton.addAttribute("tabindex", "2");
    saveButton.addAttribute("accesskey", "s");
    return saveButton;
  }

  private HtmlTag createTextarea(boolean firstTimeForNewPage, String defaultNewPageContent) {
    HtmlTag textarea = new HtmlTag("textarea");
    textarea.addAttribute("class", CONTENT_INPUT_NAME);
    textarea.addAttribute("name", CONTENT_INPUT_NAME);
    textarea.addAttribute("rows", "30");
    textarea.addAttribute("cols", "70");
    textarea.addAttribute("tabindex", "1");
    textarea.add(Utils.escapeHTML(firstTimeForNewPage ? defaultNewPageContent : content));
    return textarea;
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
