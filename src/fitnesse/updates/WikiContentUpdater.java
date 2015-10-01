// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import fitnesse.FitNesseContext;
import fitnesse.Updater;
import util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WikiContentUpdater implements Updater {


  protected static final Logger LOG = Logger.getLogger(WikiContentUpdater.class.getName());

  protected final FitNesseContext context;
  private Properties rootProperties;
  private String fitNesseVersion;

  public WikiContentUpdater(FitNesseContext context) throws IOException {
    this.context = context;
    rootProperties = loadProperties();
    fitNesseVersion = context.version.toString();
  }

  public Properties getProperties() {
    return rootProperties;
  }

  public Properties loadProperties() throws IOException {
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

  private File getPropertiesFile() {
    return new File(context.getRootPagePath(), "properties");
  }

  public void saveProperties() throws IOException {
    OutputStream os = null;
    File propFile = null;
    try {
      propFile = getPropertiesFile();
      os = new FileOutputStream(propFile);
      rootProperties.store(os, "#FitNesse properties");
    } catch (IOException e) {
      String fileName = (propFile != null) ? propFile.getAbsolutePath() : "<unknown>";
      LOG.log(Level.SEVERE, "Filed to save properties file: \"" + fileName + "\". (exception: " + e + ")");
      throw e;
    } finally {
      if (os != null)
        os.close();
    }
  }

  boolean performAllupdates() throws IOException {
    List<Update> updates = makeAllUpdates();
    for (Update update: updates) {
      if (update.shouldBeApplied())
        performUpdate(update);
    }
    return true;
  }

  private void performUpdate(Update update) {
    try {
//      LOG.info(update.getMessage());
      update.doUpdate();
    }
    catch (Exception e) {
      LOG.log(Level.SEVERE, "Update failed", e);
    }
  }

  List<Update> makeAllUpdates() {
    List<Update> updates = new ArrayList<Update>();
    updates.addAll(addAllFilesToBeReplaced());
    updates.addAll(addAllFilesThatShouldNotBeCopiedOver());
    return updates;
  }

  private List<Update> addAllFilesThatShouldNotBeCopiedOver() {
    List<Update> updates = new ArrayList<Update>();
    String[] updateDoNotCopyOver = tryToParseTheFileIntoTheList(new File(context.getRootPagePath(), "updateDoNotCopyOverList"));
    for (String nonCopyableFile : updateDoNotCopyOver) {
      File path = getCorrectPathForTheDestination(nonCopyableFile);
      String source = getCorrectPathFromJar(nonCopyableFile);
      updates.add(new FileUpdate(source, path));
    }
    return updates;
  }

  private List<Update> addAllFilesToBeReplaced() {
    List<Update> updates = new ArrayList<Update>();
    String[] updateList = tryToParseTheFileIntoTheList(new File(context.getRootPagePath(), "updateList"));
    for (String updateableFile : updateList) {
      File path = getCorrectPathForTheDestination(updateableFile);
      String source = getCorrectPathFromJar(updateableFile);
      updates.add(new ReplacingFileUpdate(source, path));
    }
    return updates;
  }

  String getCorrectPathFromJar(String updateableFile) {
    return "Resources/" + updateableFile;
  }


  File getCorrectPathForTheDestination(String updateableFile) {
    if (updateableFile.startsWith("FitNesseRoot"))
      updateableFile = updateableFile.replace("FitNesseRoot", context.getRootPagePath());
    return new File(updateableFile).getParentFile();
  }

  private void getUpdateFilesFromJarFile() throws IOException {
    Update update = new FileUpdate("Resources/updateList", new File(context.getRootPagePath()));
    update.doUpdate();
    update = new FileUpdate("Resources/updateDoNotCopyOverList", new File(context.getRootPagePath()));
    update.doUpdate();
  }

  String[] tryToParseTheFileIntoTheList(File updateFileList) {
    if (!updateFileList.exists())
      throw new RuntimeException("Could Not Find UpdateList");

    try {
      return parseTheFileContentToAList(updateFileList);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  private String[] parseTheFileContentToAList(File updateFileList) throws IOException {
    String content = FileUtil.getFileContent(updateFileList);
    return content.split("\n");
  }

  @Override
  public boolean update() throws IOException {
    if (shouldUpdate()) {
      LOG.info("Unpacking new version of FitNesse resources. Please be patient...");
      getUpdateFilesFromJarFile();
      performAllupdates();
      LOG.info("**********************************************************");
      LOG.info("Files have been updated to a new version.");
      LOG.info("Please read the release notes on ");
      LOG.info("http://localhost:" + (context != null ? context.port : "xxx") +
          "/FitNesse.ReleaseNotes");
      LOG.info("to find out about the new features and fixes.");
      LOG.info("**********************************************************");

      getProperties().put("Version", fitNesseVersion);
      saveProperties();
      return true;
    }
    return false;
  }

  private boolean shouldUpdate() {
    String versionProperty = getProperties().getProperty("Version");
    return versionProperty == null || !versionProperty.equals(fitNesseVersion);
  }

  public void setFitNesseVersion(String version) {
    fitNesseVersion = version;
  }
}
