// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import util.FileUtil;
import util.XmlUtil;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wiki.WikiPageProperty;

public class SymLinkPropertyFormatUpdate extends PageTraversingUpdate {
  public SymLinkPropertyFormatUpdate(UpdaterImplementation updater) {
    super(updater);
  }

  public void processPage(WikiPage currentPage) throws Exception {
    try {
      FileSystemPage page = (FileSystemPage) currentPage;
      String propertiesContent = FileUtil.getFileContent(page.getFileSystemPath() + FileSystemPage.propertiesFilename);
      if (propertiesContent.contains("<symbolicLink>"))
        fixPropertiesFile(page, propertiesContent);
    }
    catch (Exception e) {
      // continue to next page
    }
  }

  private void fixPropertiesFile(FileSystemPage page, String propertiesContent) throws Exception {
    PageData data = page.getData();
    WikiPageProperty symLinkProperty = getSymbolicLinkProperty(data);
    data.getProperties().remove("symbolicLink");

    Document document = XmlUtil.newDocument(propertiesContent);
    NodeList oldLinkElements = document.getElementsByTagName("symbolicLink");
    for (int i = 0; i < oldLinkElements.getLength(); i++) {
      Element oldSymLink = (Element) oldLinkElements.item(i);
      String name = XmlUtil.getLocalTextValue(oldSymLink, "name");
      String path = XmlUtil.getLocalTextValue(oldSymLink, "path");
      symLinkProperty.set(name, path);
    }

    page.commit(data);
  }

  private WikiPageProperty getSymbolicLinkProperty(PageData data) throws Exception {
    WikiPageProperties properties = data.getProperties();
    WikiPageProperty symLinkProperty = properties.getProperty(SymbolicPage.PROPERTY_NAME);
    if (symLinkProperty == null)
      symLinkProperty = properties.set(SymbolicPage.PROPERTY_NAME);
    return symLinkProperty;
  }

  public String getName() {
    return "SymLinkPropertyFormatUpdate";
  }

  public String getMessage() {
    return "Updating the format of SybolicLink properties";
  }
}
