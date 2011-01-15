// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import static fitnesse.wiki.PageData.*;
import static fitnesse.wiki.PageType.*;

import java.util.Set;

import org.json.JSONObject;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTableListingBuilder;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.RawHtml;
import fitnesse.html.TagGroup;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.NotFoundResponder;
import fitnesse.responders.WikiImportProperty;
import fitnesse.wiki.MockingPageCrawler;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wikitext.Utils;

public class PropertiesResponder implements SecureResponder {
  private WikiPage page;
  public PageData pageData;
  private String resource;
  private SimpleResponse response;

  public Response makeResponse(FitNesseContext context, Request request)
      throws Exception {
    response = new SimpleResponse();
    resource = request.getResource();
    WikiPagePath path = PathParser.parse(resource);
    PageCrawler crawler = context.root.getPageCrawler();
    if (!crawler.pageExists(context.root, path))
      crawler.setDeadEndStrategy(new MockingPageCrawler());
    page = crawler.getPage(context.root, path);
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);

    pageData = page.getData();
    makeContent(context, request);
    response.setMaxAge(0);
    return response;
  }

  private void makeContent(FitNesseContext context, Request request)
      throws Exception {
    if ("json".equals(request.getInput("format"))) {
      JSONObject jsonObject = makeJson();
      response.setContent(jsonObject.toString(1));
    } else {
      String html = makeHtml(context);
      response.setContent(html);
    }
  }

  private JSONObject makeJson() throws Exception {
    response.setContentType("text/json");
    JSONObject jsonObject = new JSONObject();
    String attributes[] = new String[] { TEST.toString(), PropertySEARCH,
        PropertyEDIT, PropertyPROPERTIES, PropertyVERSIONS, PropertyREFACTOR,
        PropertyWHERE_USED, PropertyRECENT_CHANGES, SUITE.toString(),
        PropertyPRUNE, PropertySECURE_READ, PropertySECURE_WRITE,
        PropertySECURE_TEST };
    for (String attribute : attributes)
      addJsonAttribute(jsonObject, attribute);

    return jsonObject;
  }

  private void addJsonAttribute(JSONObject jsonObject, String attribute)
      throws Exception {
    jsonObject.put(attribute, pageData.hasAttribute(attribute));
  }

  private String makeHtml(FitNesseContext context) throws Exception {
    HtmlPage page = context.htmlPageFactory.newPage();
    page.title.use("Properties: " + resource);
    page.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource,
        "Page Properties"));
    page.main.use(makeLastModifiedTag());
    page.main.add(makeFormSections());

    return page.html();
  }

  private HtmlTag makeAttributeCheckbox(String attribute, String displayString, PageData pageData)
      throws Exception {
    HtmlTag checkbox = makeCheckbox(attribute, displayString);
    if (pageData.hasAttribute(attribute))
      checkbox.addAttribute("checked", "true");
    return checkbox;
  }

  private HtmlTag makeCheckbox(String attribute, String displayString) {
    HtmlTag checkbox = HtmlUtil.makeInputTag("checkbox", attribute);
    checkbox.tail = " - " + displayString;
    return checkbox;
  }

  private HtmlTag makeLastModifiedTag() throws Exception {
    HtmlTag tag = HtmlUtil.makeDivTag("right");
    String username = pageData.getAttribute(LAST_MODIFYING_USER);
    if (username == null || "".equals(username))
      tag.use("Last modified anonymously");
    else
      tag.use("Last modified by " + username);
    return tag;
  }

  private HtmlTag makeFormSections() throws Exception {
    TagGroup html = new TagGroup();
    html.add(makePropertiesForm());

    WikiImportProperty importProperty = WikiImportProperty.createFrom(pageData
        .getProperties());
    if (importProperty != null)
      html.add(makeImportUpdateForm(importProperty));
    else
      html.add(makeImportForm());

    html.add(makeSymbolicLinkSection());

    return html;
  }

  private HtmlTag makePropertiesForm() throws Exception {
    HtmlTag form = HtmlUtil.makeFormTag("post", resource);
    form.add(HtmlUtil.makeInputTag("hidden", "responder", "saveProperties"));

    HtmlTag trisection = new HtmlTag("div");
    trisection.addAttribute("style", "width:100%");
    HtmlTag checkBoxesSection = new HtmlTag("div");
    checkBoxesSection.addAttribute("class", "properties");
    checkBoxesSection.add(makePageTypeRadiosHtml(pageData));
    checkBoxesSection.add(makeTestActionCheckboxesHtml(pageData));
    checkBoxesSection.add(makeNavigationCheckboxesHtml(pageData));
    checkBoxesSection.add(makeSecurityCheckboxesHtml(pageData));
    HtmlTag virtualWikiSection = new HtmlTag("div");
    virtualWikiSection.addAttribute("class", "virtual-wiki-properties");
    virtualWikiSection.add(makeVirtualWikiHtml());
    virtualWikiSection.add(makeTagsHtml(pageData));
    virtualWikiSection.add(makeHelpTextHtml(pageData));
    trisection.add(checkBoxesSection);
    trisection.add(virtualWikiSection);
    form.add(trisection);

    HtmlTag buttonSection = new HtmlTag("div");
    buttonSection.add(HtmlUtil.BR);
    HtmlTag saveButton = HtmlUtil.makeInputTag("submit", "Save",
        "Save Properties");
    saveButton.addAttribute("accesskey", "s");
    buttonSection.add(saveButton);
    form.add(buttonSection);
    return form;
  }

  public HtmlTag makePageTypeRadiosHtml(PageData pageData) throws Exception {
    return makeAttributeRadiosHtml("Page type: ",
        PAGE_TYPE_ATTRIBUTES, PAGE_TYPE_ATTRIBUTE, pageData);
  }

  private HtmlTag makeAttributeRadiosHtml(String label, String[] attributes,
      String radioGroup, PageData pageData) throws Exception {
    HtmlTag div = new HtmlTag("div");
    div.addAttribute("style", "float: left; width: 150px;");

    div.add(label);
    String checkedAttribute = getCheckedAttribute(pageData, attributes);
    for (String attribute : attributes) {
      div.add(HtmlUtil.BR);
      div.add(makeAttributeRadio(radioGroup, attribute, attribute.equals(checkedAttribute), attribute));
    }
    div.add(HtmlUtil.BR);
    div.add(HtmlUtil.BR);
    
    div.add(makeAttributeCheckbox(PropertyPRUNE, "Skip (Recursive)", pageData));
    
    return div;
  }

  private String getCheckedAttribute(PageData pageData, String[] attributes)
      throws Exception {
    for (int i = attributes.length - 1; i > 0; i--) {
      if (pageData.hasAttribute(attributes[i]))
        return attributes[i];
    }
    return attributes[0];
  }

  private HtmlTag makeAttributeRadio(String group, String attribute,
      boolean checked, String guiName) throws Exception {
    HtmlTag radioButton = makeRadioButton(group, attribute, guiName);
    if (checked)
      radioButton.addAttribute("checked", "checked");
    return radioButton;
  }

  private HtmlTag makeRadioButton(String group, String attribute, String guiName) {
    HtmlTag checkbox = HtmlUtil.makeInputTag("radio", group);
    checkbox.addAttribute("value", attribute);
    checkbox.tail = " - " + guiName;
    return checkbox;
  }

  private HtmlTag makeVirtualWikiHtml() throws Exception {
    HtmlTag virtualWiki = new HtmlTag("div");
    virtualWiki.addAttribute("style", "float: left; width: 450px;");
    virtualWiki.add("VirtualWiki URL: ");
    HtmlTag deprecated = new HtmlTag("span", "(DEPRECATED)");
    deprecated.addAttribute("style", "color: #FF0000;");
    virtualWiki.add(deprecated);
    virtualWiki.add(HtmlUtil.BR);
    HtmlTag vwInput = HtmlUtil.makeInputTag("text", "VirtualWiki",
        getVirtualWikiValue(pageData));
    vwInput.addAttribute("size", "40");
    virtualWiki.add(vwInput);
    virtualWiki.add(HtmlUtil.NBSP);
    virtualWiki.add(HtmlUtil.NBSP);
    return virtualWiki;
  }

  private HtmlTag makeImportForm() {
    HtmlTag form = HtmlUtil.makeFormTag("post", resource + "#end");
    form.add(HtmlUtil.HR);
    form.add("Wiki Import.  Supply the URL for the wiki you'd like to import.");
    form.add(HtmlUtil.BR);
    form.add("Remote Wiki URL:");
    HtmlTag remoteUrlField = HtmlUtil.makeInputTag("text", "remoteUrl");
    remoteUrlField.addAttribute("size", "70");
    form.add(remoteUrlField);
    form.add(HtmlUtil.BR);
    HtmlTag autoUpdateCheckBox = HtmlUtil.makeInputTag("checkbox",
        "autoUpdate", "checked");
    autoUpdateCheckBox.addAttribute("checked", "true");
    form.add(autoUpdateCheckBox);
    form.add("- Automatically update imported content when executing tests");
    form.add(HtmlUtil.BR);
    form.add(HtmlUtil.makeInputTag("hidden", "responder", "import"));
    form.add(HtmlUtil.makeInputTag("submit", "save", "Import"));
    return form;
  }

  private HtmlTag makeImportUpdateForm(WikiImportProperty importProps)
      throws Exception {
    HtmlTag form = HtmlUtil.makeFormTag("post", resource + "#end");

    form.add(HtmlUtil.HR);
    form.add(new HtmlTag("b", "Wiki Import Update"));
    form.add(HtmlUtil.BR);
    String buttonMessage;
    form.add(HtmlUtil.makeLink(page.getName(), page.getName()));
    if (importProps.isRoot()) {
      form.add(" imports its subpages from ");
      buttonMessage = "Update Subpages";
    } else {
      form.add(" imports its content and subpages from ");
      buttonMessage = "Update Content and Subpages";
    }
    form.add(HtmlUtil.makeLink(importProps.getSourceUrl(), importProps
        .getSourceUrl()));
    form.add(".");
    form.add(HtmlUtil.BR);
    HtmlTag autoUpdateCheckBox = HtmlUtil
        .makeInputTag("checkbox", "autoUpdate");
    if (importProps.isAutoUpdate())
      autoUpdateCheckBox.addAttribute("checked", "true");
    form.add(autoUpdateCheckBox);

    form.add("- Automatically update imported content when executing tests");
    form.add(HtmlUtil.BR);
    form.add(HtmlUtil.makeInputTag("hidden", "responder", "import"));
    form.add(HtmlUtil.makeInputTag("submit", "save", buttonMessage));

    return form;
  }

  private HtmlTag makeSymbolicLinkSection() throws Exception {
    HtmlTag form = HtmlUtil.makeFormTag("get", resource, "symbolics");
    form.add(HtmlUtil.HR);
    form.add(HtmlUtil.makeInputTag("hidden", "responder", "symlink"));
    form.add(new HtmlTag("strong", "Symbolic Links"));

    HtmlTableListingBuilder table = new HtmlTableListingBuilder();
    table.getTable().addAttribute("style", "width:80%");
    table.addRow(new HtmlElement[] { new HtmlTag("strong", "Name"),
        new HtmlTag("strong", "Path to Page"), new HtmlTag("strong", "Actions")
    // , new HtmlTag("strong", "New Name")
        });
    addSymbolicLinkRows(table);
    addFormRow(table);
    form.add(table.getTable());

    return form;
  }

  private void addFormRow(HtmlTableListingBuilder table) throws Exception {
    HtmlTag nameInput = HtmlUtil.makeInputTag("text", "linkName");
    nameInput.addAttribute("size", "16%");
    HtmlTag pathInput = HtmlUtil.makeInputTag("text", "linkPath");
    pathInput.addAttribute("size", "60%");
    HtmlTag submitButton = HtmlUtil.makeInputTag("submit", "submit",
        "Create/Replace");
    submitButton.addAttribute("style", "width:8em");
    table.addRow(new HtmlElement[] { nameInput, pathInput, submitButton });
  }

  private void addSymbolicLinkRows(HtmlTableListingBuilder table)
      throws Exception {
    WikiPageProperty symLinksProperty = pageData.getProperties().getProperty(
        SymbolicPage.PROPERTY_NAME);
    if (symLinksProperty == null)
      return;
    Set<String> symbolicLinkNames = symLinksProperty.keySet();
    for (String linkName : symbolicLinkNames) {
      HtmlElement nameItem = new RawHtml(linkName);
      HtmlElement pathItem = makeHtmlForSymbolicPath(symLinksProperty, linkName);
      // ---Unlink---
      HtmlTag actionItems = HtmlUtil.makeLink(resource
          + "?responder=symlink&removal=" + linkName, "Unlink&nbsp;");
      // ---Rename---
      String callScript = "javascript:symbolicLinkRename('" + linkName + "','"
          + resource + "');";
      actionItems.tail = HtmlUtil.makeLink(callScript, "&nbsp;Rename:").html(); // ..."linked list"

      HtmlTag newNameInput = HtmlUtil.makeInputTag("text", linkName);
      newNameInput.addAttribute("size", "16%");
      table.addRow(new HtmlElement[] { nameItem, pathItem, actionItems,
          newNameInput });
    }
  }

  private HtmlElement makeHtmlForSymbolicPath(
      WikiPageProperty symLinksProperty, String linkName) throws Exception {
    String linkPath = symLinksProperty.get(linkName);
    WikiPagePath wikiPagePath = PathParser.parse(linkPath);

    if (wikiPagePath != null) {
      WikiPage parent = wikiPagePath.isRelativePath() ? page.getParent() : page; // TODO
                                                                                 // -AcD-
                                                                                 // a
                                                                                 // better
                                                                                 // way?
      PageCrawler crawler = parent.getPageCrawler();
      WikiPage target = crawler.getPage(parent, wikiPagePath);
      WikiPagePath fullPath;
      if (target != null) {
        fullPath = crawler.getFullPath(target);
        fullPath.makeAbsolute();
      } else
        fullPath = new WikiPagePath();
      return HtmlUtil.makeLink(fullPath.toString(), Utils.escapeHTML(linkPath));
    } else
      return new RawHtml(linkPath);
  }

  public static String getVirtualWikiValue(PageData data) throws Exception {
    String value = data.getAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE);
    if (value == null)
      return "";
    else
      return value;
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

  public HtmlTag makeTestActionCheckboxesHtml(PageData pageData)
      throws Exception {
    return makeAttributeCheckboxesHtml("Actions:", ACTION_ATTRIBUTES,
        pageData);
  }

  public HtmlElement makeNavigationCheckboxesHtml(PageData pageData)
      throws Exception {
    return makeAttributeCheckboxesHtml("Navigation:",
        NAVIGATION_ATTRIBUTES, pageData);
  }

  public HtmlTag makeSecurityCheckboxesHtml(PageData pageData) throws Exception {
    return makeAttributeCheckboxesHtml("Security:",
        SECURITY_ATTRIBUTES, pageData);
  }

  public HtmlTag makeTagsHtml(PageData pageData) throws Exception {
    HtmlTag div = new HtmlTag("div");
    div.addAttribute("style", "float: left; padding-right: 5px");

    div.add(makeInputField("Tags:", PropertySUITES, PropertySUITES,
        40, pageData));
    return div;
  }

  public HtmlTag makeHelpTextHtml(PageData pageData) throws Exception {
    return makeInputField("Help Text:", PropertyHELP, "HelpText", 90,
        pageData);
  }

  public HtmlTag makeInputField(String label, String propertyName,
      String fieldId, int size, PageData pageData) throws Exception {
    HtmlTag div = new HtmlTag("div");
    div.addAttribute("style", "float: left;");
    div.add(label);

    String textValue = "";
    WikiPageProperty theProp = pageData.getProperties().getProperty(
        propertyName);
    if (theProp != null) {
      String propValue = theProp.getValue();
      if (propValue != null)
        textValue = propValue;
    }

    div.add(HtmlUtil.BR);
    HtmlTag input = HtmlUtil.makeInputTag("text", fieldId, textValue);
    input.addAttribute("size", Integer.toString(size));
    div.add(input);
    return div;
  }

  private HtmlTag makeAttributeCheckboxesHtml(String label,
      String[] attributes, PageData pageData) throws Exception {
    HtmlTag div = new HtmlTag("div");
    div.addAttribute("style", "float: left; width: 180px;");

    div.add(label);
    for (String attribute : attributes) {
      div.add(HtmlUtil.BR);
      div.add(makeAttributeCheckbox(attribute, attribute, pageData));
    }
    div.add(HtmlUtil.BR);
    div.add(HtmlUtil.BR);
    return div;
  }

}
