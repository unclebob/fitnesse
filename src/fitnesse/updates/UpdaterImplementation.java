// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import fitnesse.FitNesseContext;
import fitnesse.Updater;
import fitnesse.wiki.WikiPage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

public class UpdaterImplementation implements Updater {
  public static boolean testing = false;

  public FitNesseContext context;
  public Properties rootProperties;

  public Update[] updates;

  public UpdaterImplementation(FitNesseContext context) throws Exception {
    this.context = context;
    rootProperties = loadProperties();

    updates = new Update[]{
        new ReplacingFileUpdate(this, "files/images/FitNesseLogo.gif", "files/images"),
        new ReplacingFileUpdate(this, "files/images/FitNesseLogoMedium.jpg", "files/images"),
        new ReplacingFileUpdate(this, "files/images/virtualPage.jpg", "files/images"),
        new ReplacingFileUpdate(this, "files/images/importedPage.jpg", "files/images"),
        new ReplacingFileUpdate(this, "files/images/collapsableOpen.gif", "files/images"),
        new ReplacingFileUpdate(this, "files/images/collapsableClosed.gif", "files/images"),
        new ReplacingFileUpdate(this, "files/images/folder.gif", "files/images"),
        new ReplacingFileUpdate(this, "files/images/executionStatus/ok.gif", "files/images/executionStatus"),
        new ReplacingFileUpdate(this, "files/images/executionStatus/output.gif", "files/images/executionStatus"),
        new ReplacingFileUpdate(this, "files/images/executionStatus/error.gif", "files/images/executionStatus"),
        new ReplacingFileUpdate(this, "files/images/stop.gif", "files/images/"),
        new ReplacingFileUpdate(this, "files/css/fitnesse_base.css", "files/css"),
        new FileUpdate(this, "files/css/fitnesse.css", "files/css"),
        new FileUpdate(this, "files/css/fitnesse_print.css", "files/css"),
        new ReplacingFileUpdate(this, "files/javascript/fitnesse.js", "files/javascript"),
        new ReplacingFileUpdate(this, "files/javascript/clientSideSort.js", "files/javascript"),
        new ReplacingFileUpdate(this, "files/javascript/SpreadsheetTranslator.js", "files/javascript"),
        new ReplacingFileUpdate(this, "files/javascript/spreadsheetSupport.js", "files/javascript"),
        new ReplacingFileUpdate(this, "files/templates/pageFooter.vm", "files/templates"),
        new ReplacingFileUpdate(this, "files/templates/pageHead.vm", "files/templates"),
        new ReplacingFileUpdate(this, "files/templates/pageHistory.vm", "files/templates"),
        new ReplacingFileUpdate(this, "files/templates/pageTitle.vm", "files/templates"),
        new ReplacingFileUpdate(this, "files/templates/testExecutionReport.vm", "files/templates"),
        new ReplacingFileUpdate(this, "files/templates/testHistory.vm", "files/templates"),
        new ReplacingFileUpdate(this, "files/templates/testResults.vm", "files/templates"),
        new ReplacingFileUpdate(this, "files/templates/searchForm.vm", "files/templates"),
        new ReplacingFileUpdate(this, "files/templates/searchResults.vm", "files/templates"),
        new ReplacingFileUpdate(this, "files/html/index.html", "files/html"),
        new ReplacingFileUpdate(this, "files/html/treeControl.html", "files/html"),
        new PropertiesToXmlUpdate(this),
        new AttributeAdderUpdate(this, "RecentChanges"),
        new AttributeAdderUpdate(this, "WhereUsed"),
        new AttributeAdderUpdate(this, "Files"),
        new SymLinkPropertyFormatUpdate(this),
        new WikiImportPropertyFormatUpdate(this),
        new VirtualWikiDeprecationUpdate(this),
        new FrontPageUpdate(this)
    };
  }

  public void update() throws Exception {
    Update[] updates = getUpdates();
    for (int i = 0; i < updates.length; i++) {
      Update update = updates[i];
      if (update.shouldBeApplied())
        performUpdate(update);
    }
    saveProperties();
  }

  private void performUpdate(Update update) throws Exception {
    try {
      print(update.getMessage());
      update.doUpdate();
      print("...done\n");
    }
    catch (Exception e) {
      print("\n\t" + e + "\n");
    }
  }

  private Update[] getUpdates() throws Exception {
    return updates;
  }

  public WikiPage getRoot() {
    return context.root;
  }

  public Properties getProperties() {
    return rootProperties;
  }

  public Properties loadProperties() throws Exception {
    Properties properties = new Properties();
    File propFile = getPropertiesFile();
    if (propFile.exists()) {
      InputStream is = null;
      try {
        is = new FileInputStream(propFile);
        properties.load(is);
      } finally {
        if (is != null)
          is.close();
      }
    }
    return properties;
  }

  private File getPropertiesFile() throws Exception {
    String filename = context.rootPagePath + "/properties";
    return new File(filename);
  }

  public void saveProperties() throws Exception {
    OutputStream os = null;
    File propFile = null;
    try {
      propFile = getPropertiesFile();
      os = new FileOutputStream(propFile);
      writeProperties(os);
    } catch (Exception e) {
      String fileName = (propFile != null) ? propFile.getAbsolutePath() : "<unknown>";
      System.err.println("Filed to save properties file: \"" + fileName + "\". (exception: " + e + ")");
      throw e;
    } finally {
      if (os != null)
        os.close();
    }
  }

  private void writeProperties(final OutputStream OutputStream)
  throws IOException {
    BufferedWriter awriter;
    awriter = new BufferedWriter(new OutputStreamWriter(OutputStream, "8859_1"));
    awriter.write("#FitNesse properties");
    awriter.newLine();
    Object[] keys = rootProperties.keySet().toArray(new Object[0]);
    Arrays.sort(keys);
    for (Enumeration<Object> enumeration = rootProperties.keys(); enumeration
    .hasMoreElements();) {
      String key = (String) enumeration.nextElement();
      String val = (String) rootProperties.get(key);
      awriter.write(key + "=" + val);
      awriter.newLine();
    }
    awriter.flush();
  }

  private void print(String message) {
    if (!testing)
      System.out.print(message);
  }
}
