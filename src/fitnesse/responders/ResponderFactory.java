// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.responders.editing.*;
import fitnesse.responders.files.*;
import fitnesse.responders.refactoring.DeletePageResponder;
import fitnesse.responders.refactoring.MovePageResponder;
import fitnesse.responders.refactoring.RefactorPageResponder;
import fitnesse.responders.refactoring.RenamePageResponder;
import fitnesse.responders.revisioncontrol.*;
import fitnesse.responders.run.*;
import fitnesse.responders.search.SearchFormResponder;
import fitnesse.responders.search.SearchResponder;
import fitnesse.responders.search.WhereUsedResponder;
import fitnesse.responders.versions.RollbackResponder;
import fitnesse.responders.versions.VersionResponder;
import fitnesse.responders.versions.VersionSelectionResponder;
import static fitnesse.revisioncontrol.RevisionControlOperation.*;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.widgets.WikiWordWidget;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ResponderFactory {
  private final String rootPath;
  private final Map<String, Class<?>> responderMap;

  public ResponderFactory(String rootPath) {
    this.rootPath = rootPath;
    responderMap = new HashMap<String, Class<?>>();
    addResponder("dontCreatePage", NotFoundResponder.class);
    addResponder("edit", EditResponder.class);
    addResponder("saveData", SaveResponder.class);
    addResponder("search", SearchResponder.class);
    addResponder("searchForm", SearchFormResponder.class);
    addResponder("test", TestResponder.class);
    addResponder("suite", SuiteResponder.class);
    addResponder("proxy", SerializedPageResponder.class);
    addResponder("versions", VersionSelectionResponder.class);
    addResponder("viewVersion", VersionResponder.class);
    addResponder("rollback", RollbackResponder.class);
    addResponder("names", NameWikiPageResponder.class);
    addResponder("properties", PropertiesResponder.class);
    addResponder("saveProperties", SavePropertiesResponder.class);
    addResponder("whereUsed", WhereUsedResponder.class);
    addResponder("refactor", RefactorPageResponder.class);
    addResponder("deletePage", DeletePageResponder.class);
    addResponder("renamePage", RenamePageResponder.class);
    addResponder("movePage", MovePageResponder.class);
    addResponder("pageData", PageDataWikiPageResponder.class);
    addResponder("createDir", CreateDirectoryResponder.class);
    addResponder("upload", UploadResponder.class);
    addResponder("socketCatcher", SocketCatchingResponder.class);
    addResponder("fitClient", FitClientResponder.class);
    addResponder("deleteFile", DeleteFileResponder.class);
    addResponder("renameFile", RenameFileResponder.class);
    addResponder("deleteConfirmation", DeleteConfirmationResponder.class);
    addResponder("renameConfirmation", RenameFileConfirmationResponder.class);
    addResponder("raw", RawContentResponder.class);
    addResponder("rss", RssResponder.class);
    addResponder("import", WikiImportingResponder.class);
    addResponder("files", FileResponder.class);
    addResponder("shutdown", ShutdownResponder.class);
    addResponder("format", TestResultFormattingResponder.class);
    addResponder("symlink", SymbolicLinkResponder.class);
    addResponder("importAndView", ImportAndViewResponder.class);
    addResponder("getPage", WikiPageResponder.class);
    addRespondersForRevisionControlOperations();
  }

  private void addRespondersForRevisionControlOperations() {
    addResponder(ADD.getQuery(), AddResponder.class);
    addResponder(SYNC.getQuery(), SyncResponder.class);
    addResponder(CHECKOUT.getQuery(), CheckoutResponder.class);
    addResponder(CHECKIN.getQuery(), CheckinResponder.class);
    addResponder(DELETE.getQuery(), DeleteResponder.class);
    addResponder(REVERT.getQuery(), RevertResponder.class);
    addResponder(UPDATE.getQuery(), UpdateResponder.class);
  }

  public void addResponder(String key, Class<?> responderClass) {
    responderMap.put(key, responderClass);
  }

  public String getResponderKey(Request request) {
    String fullQuery;
    if (request.hasInput("responder"))
      fullQuery = (String) request.getInput("responder");
    else
      fullQuery = request.getQueryString();

    int argStart = fullQuery == null ? -1 : fullQuery.indexOf('&');
    return (argStart <= 0) ? fullQuery : fullQuery.substring(0, argStart);
  }

  public Responder makeResponder(Request request, WikiPage root) throws Exception {
    Responder responder = new DefaultResponder();
    String resource = request.getResource();
    if ("".equals(resource))
      resource = "FrontPage";
    String responderKey = getResponderKey(request);
    if (usingResponderKey(responderKey))
      responder = lookupResponder(responderKey, responder);
    else {
      if (resource.startsWith("files/") || resource.equals("files"))
        responder = FileResponder.makeResponder(request, rootPath);
      else if (WikiWordWidget.isWikiWord(resource) || "root".equals(resource))
        responder = new WikiPageResponder();
      else
        responder = new NotFoundResponder();
    }

    return responder;
  }

  private Responder lookupResponder(String responderKey, Responder responder)
    throws NoSuchMethodException, InstantiationException,
    IllegalAccessException, InvocationTargetException {
    Class<?> responderClass = getResponderClass(responderKey);
    if (responderClass != null) {
      try {
        Constructor<?> constructor = responderClass.getConstructor(new Class<?>[]{String.class});
        responder = (Responder) constructor.newInstance(new Object[]{rootPath});
      } catch (NoSuchMethodException e) {
        Constructor<?> constructor = responderClass.getConstructor(new Class<?>[0]);
        responder = (Responder) constructor.newInstance(new Object[0]);
      }
    }
    return responder;
  }

  public Class<?> getResponderClass(String responderKey) {
    return responderMap.get(responderKey);
  }

  private boolean usingResponderKey(String responderKey) {
    return !("".equals(responderKey) || responderKey == null);
  }
}
