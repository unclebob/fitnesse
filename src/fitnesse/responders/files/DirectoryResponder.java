// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import util.FileUtil;
import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
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

public class DirectoryResponder implements SecureResponder {
  private String resource;
  private File requestedDirectory;
  private FitNesseContext context;
  private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");

  public DirectoryResponder(String resource, File requestedFile) {
    this.resource = resource;
    requestedDirectory = requestedFile;
  }

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    this.context = context;

    SimpleResponse simpleResponse = new SimpleResponse();
    if (!resource.endsWith("/"))
      setRedirectForDirectory(simpleResponse);
    else
      simpleResponse.setContent(makeDirectoryListingPage());
    return simpleResponse;
  }

  private void setRedirectForDirectory(Response response) {
    if (!resource.startsWith("/"))
      resource = "/" + resource;
    response.redirect(resource + "/");
  }

  private String makeDirectoryListingPage() throws Exception {
    HtmlPage page = context.htmlPageFactory.newPage();
    page.title.use("Files: " + resource);
    page.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource, "/", "Files Section"));
    page.actions.use(makeFrontPageLink());
    page.main.use(makeRightColumn());

    return page.html();
  }

  private HtmlTag makeFrontPageLink() {
    HtmlUtil.ActionLink link = new HtmlUtil.ActionLink("/FrontPage", "FrontPage");
    link.setQuery(null);
    return link.getHtml();
  }

  private String makeRightColumn() throws Exception {
    TagGroup html = new TagGroup();
    html.add(addFiles(FileUtil.getDirectoryListing(requestedDirectory)));
    html.add(HtmlUtil.HR.html());
    html.add(makeUploadForm());
    html.add(makeDirectoryForm());
    return html.html();
  }

  private HtmlTag addFiles(File[] files) throws Exception {
    HtmlTableListingBuilder table = new HtmlTableListingBuilder();
    makeHeadingRow(table);
    addFileRows(files, table);

    return table.getTable();
  }

  private void addFileRows(File[] files, HtmlTableListingBuilder table) throws Exception {
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      HtmlTag nameItem = makeLinkToFile(file);
      HtmlElement sizeItem = new RawHtml(getSizeString(file));
      HtmlElement dateItem = new RawHtml(dateFormat.format(new Date(file.lastModified())));
      TagGroup actionItem = new TagGroup();
      actionItem.add(makeRenameButton(file.getName()));
      actionItem.add("|");
      actionItem.add(makeDeleteButton(file.getName()));
      table.addRow(new HtmlElement[]{nameItem, sizeItem, dateItem, actionItem});
    }
  }

  private void makeHeadingRow(HtmlTableListingBuilder table) throws Exception {
    HtmlTag nameHeading = HtmlUtil.makeSpanTag("caps", "Name");
    HtmlTag sizeHeading = HtmlUtil.makeSpanTag("caps", "Size");
    HtmlTag dateHeading = HtmlUtil.makeSpanTag("caps", "Date");
    HtmlTag actionHeading = HtmlUtil.makeSpanTag("caps", "Action");
    table.addRow(new HtmlTag[]{nameHeading, sizeHeading, dateHeading, actionHeading});
  }

  private HtmlTag makeDeleteButton(String filename) throws Exception {
    return HtmlUtil.makeLink("?responder=deleteConfirmation&filename=" + filename, "Delete");
  }

  private HtmlTag makeRenameButton(String filename) throws Exception {
    return HtmlUtil.makeLink("?responder=renameConfirmation&filename=" + filename, "Rename");
  }

  private HtmlTag makeLinkToFile(File file) {
    String href = file.getName();
    if (file.isDirectory()) {
      href += "/";
      HtmlTag image = new HtmlTag("img");
      image.addAttribute("src", "/files/images/folder.gif");
      image.addAttribute("class", "left");
      HtmlTag link = HtmlUtil.makeLink(href, image);
      link.add(file.getName());
      return link;
    } else
      return HtmlUtil.makeLink(href, file.getName());
  }

  private HtmlTag makeUploadForm() throws Exception {
    HtmlTag uploadForm = HtmlUtil.makeFormTag("post", "/" + resource);
    uploadForm.addAttribute("enctype", "multipart/form-data");
    uploadForm.addAttribute("class", "left");
    uploadForm.add("<!--upload form-->");
    uploadForm.add(HtmlUtil.makeSpanTag("caps", "Upload a file:"));
    uploadForm.add(HtmlUtil.makeInputTag("hidden", "responder", "upload"));
    uploadForm.add(HtmlUtil.BR);
    uploadForm.add(HtmlUtil.makeInputTag("file", "file", ""));
    uploadForm.add(HtmlUtil.BR);
    uploadForm.add(HtmlUtil.makeInputTag("submit", "", "Upload"));
    return uploadForm;
  }

  private HtmlTag makeDirectoryForm() throws Exception {
    HtmlTag dirForm = HtmlUtil.makeFormTag("get", "/" + resource);
    dirForm.addAttribute("class", "right");
    dirForm.add(HtmlUtil.makeInputTag("hidden", "responder", "createDir"));
    dirForm.add("<!--create directory form-->");
    dirForm.add(HtmlUtil.makeSpanTag("caps", "Create a directory:"));
    dirForm.add(HtmlUtil.BR);
    dirForm.add(HtmlUtil.makeInputTag("text", "dirname", ""));
    dirForm.add(HtmlUtil.BR);
    dirForm.add(HtmlUtil.makeInputTag("submit", "", "Create"));
    return dirForm;
  }

  public static String getSizeString(File file) {
    if (file.isDirectory())
      return "";
    else
      return file.length() + " bytes";
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
