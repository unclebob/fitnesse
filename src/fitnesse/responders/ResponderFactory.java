// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.responders.editing.*;
import fitnesse.responders.files.*;
import fitnesse.responders.refactoring.*;
import fitnesse.responders.run.StopTestResponder;
import fitnesse.responders.run.SuiteResponder;
import fitnesse.responders.run.TestResponder;
import fitnesse.responders.search.SearchPropertiesResponder;
import fitnesse.responders.search.SearchResponder;
import fitnesse.responders.search.WhereUsedResponder;
import fitnesse.responders.testHistory.*;
import fitnesse.responders.versions.RollbackResponder;
import fitnesse.responders.versions.VersionComparerResponder;
import fitnesse.responders.versions.VersionResponder;
import fitnesse.responders.versions.VersionSelectionResponder;
import fitnesse.wiki.PathParser;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResponderFactory {
  private static final Logger LOG = Logger.getLogger(ResponderFactory.class.getName());

  private final String rootPath;
  private final Map<String, Class<? extends Responder>> responderMap;
  private final Map<String, List<Responder>> filterMap;

  public ResponderFactory(String rootPath) {
    this.rootPath = rootPath;
    responderMap = new HashMap<>();
    addResponder("new", NewPageResponder.class);
    addResponder("edit", EditResponder.class);
    addResponder("saveData", SaveResponder.class);
    addResponder("search", SearchResponder.class);
    addResponder("searchForm", SearchResponder.class);
    addResponder("stoptest", StopTestResponder.class);
    addResponder("test", TestResponder.class);
    addResponder("suite", SuiteResponder.class);
    addResponder("proxy", SerializedPageResponder.class);
    addResponder("versions", VersionSelectionResponder.class);
    addResponder("viewVersion", VersionResponder.class);
    addResponder("rollback", RollbackResponder.class);
    addResponder("names", NameWikiPageResponder.class);
    addResponder("properties", PropertiesResponder.class);
    addResponder("saveProperties", SavePropertiesResponder.class);
    addResponder("searchProperties", SearchPropertiesResponder.class);
    // Deprecated:
    addResponder("executeSearchProperties", SearchPropertiesResponder.class);
    addResponder("whereUsed", WhereUsedResponder.class);
    addResponder("refactor", RefactorPageResponder.class);
    addResponder("deletePage", DeletePageResponder.class);
    addResponder("renamePage", RenamePageResponder.class);
    addResponder("movePage", MovePageResponder.class);
    addResponder("pageData", PageDataWikiPageResponder.class);
    addResponder("createDir", CreateDirectoryResponder.class);
    addResponder("upload", UploadResponder.class);
    addResponder("deleteFile", DeleteFileResponder.class);
    addResponder("renameFile", RenameFileResponder.class);
    addResponder("deleteConfirmation", DeleteConfirmationResponder.class);
    addResponder("renameConfirmation", RenameFileConfirmationResponder.class);
    addResponder("raw", RawContentResponder.class);
    addResponder("rss", RssResponder.class);
    addResponder("import", WikiImportingResponder.class);
    addResponder("files", FileResponder.class);
    addResponder("shutdown", ShutdownResponder.class);
    addResponder("symlink", SymbolicLinkResponder.class);
    addResponder("importAndView", ImportAndViewResponder.class);
    addResponder("getPage", WikiPageResponder.class);
    addResponder("packet", PacketResponder.class);
    addResponder("testHistory", TestHistoryResponder.class);
    addResponder("pageHistory", PageHistoryResponder.class);
    addResponder("executionLog", ExecutionLogResponder.class);
    addResponder("addChild", AddChildPageResponder.class);
    addResponder("purgeHistory", PurgeHistoryResponder.class);
    addResponder("compareHistory", HistoryComparerResponder.class);
    addResponder("replace", SearchReplaceResponder.class);
    addResponder("overview", SuiteOverviewResponder.class);
    addResponder("compareVersions", VersionComparerResponder.class);
    filterMap = new HashMap<>();
  }

  public final void addResponder(String key, Class<? extends Responder> responderClass) {
    responderMap.put(key, responderClass);
  }

  public void addFilter(String key, Responder filterClass) {
    List<Responder> filters = filterMap.get(key);
    if (filters == null) {
      filters = new LinkedList<>();
      filterMap.put(key, filters);
    }
    filters.add(filterClass);
  }

  public String getResponderKey(Request request) {
    String fullQuery;
    if (request.hasInput("responder"))
      fullQuery = request.getInput("responder");
    else
      fullQuery = request.getQueryString();

    if (fullQuery == null)
      return null;

    int argStart = fullQuery.indexOf('&');
    return (argStart <= 0) ? fullQuery : fullQuery.substring(0, argStart);
  }

  public Responder makeResponder(Request request) throws InstantiationException, IOException {
    String resource = request.getResource();
    String responderKey = getResponderKey(request);

    final Responder responder;

    if (usingResponderKey(responderKey)) {
      responder = wrapWithFilters(responderKey, lookupResponder(responderKey));
    } else if (resource.startsWith("files/") || resource.equals("files")) {
      responder = wrapWithFilters("files", new FileResponder());
    } else if (StringUtils.isBlank(resource) || PathParser.parse(resource) != null) {
      responder = wrapWithFilters("wiki", new WikiPageResponder());
    } else  {
      String urlResponderKey=findMatchKeyByUrl(resource);
      if (urlResponderKey != null) {
        responder = wrapWithFilters("url", lookupResponder(urlResponderKey));
      } else {
        responder = new NotFoundResponder();
      }
    }

    return responder;
  }

  private Responder lookupResponder(String responderKey)
    throws InstantiationException {
    Class<?> responderClass = getResponderClass(responderKey);
    if (responderClass != null) {
      try {
        return newResponderInstance(responderClass);
      } catch (Exception e) {
        LOG.log(Level.WARNING, "Unable to instantiate responder " + responderKey, e);
        throw new InstantiationException("Unable to instantiate responder " + responderKey);
      }
    }
    throw new InstantiationException("No responder for " + responderKey);
  }

  private Responder newResponderInstance(Class<?> responderClass)
      throws InstantiationException, IllegalAccessException, InvocationTargetException,
      NoSuchMethodException {
    try {
      Constructor<?> constructor = responderClass.getConstructor(String.class);
      return (Responder) constructor.newInstance(rootPath);
    } catch (NoSuchMethodException e) {
      Constructor<?> constructor = responderClass.getConstructor();
      return (Responder) constructor.newInstance();
    }
  }

  private Responder wrapWithFilters(String key, Responder responder) throws InstantiationException {
    List<Responder> filters = filterMap.get(key);
    if (filters == null || filters.isEmpty()) {
      return responder;
    }

    return new FilteringResponder(filters, responder);
  }

  public Class<?> getResponderClass(String responderKey) {
    return responderMap.get(responderKey);
  }

  /**
   * find responder matcher from url
   *
   * @param url request url
   * @return Responder
   */
  public String findMatchKeyByUrl(String url) {
    String[] sepUrls = url.split("/");
    for (String key : responderMap.keySet()) {
      if (key.startsWith("/")) {
        String pureKey = key.replaceAll("/", "");
        if (Arrays.asList(sepUrls).contains(pureKey)) {
          return key;
        }
      }
    }
    return null;
  }

  private boolean usingResponderKey(String responderKey) {
    return !("".equals(responderKey) || responderKey == null);
  }
}
