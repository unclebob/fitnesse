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

  private static final Logger LOG = Logger.getLogger(WikiContentUpdater.class.getName());

  protected final FitNesseContext context;
  private Properties rootProperties;
  private String fitNesseVersion;

  public WikiContentUpdater(FitNesseContext context) throws IOException {
    this.context = context;
    rootProperties = loadProperties();
    fitNesseVersion = context.version.toString();
  }

  @Override
  public boolean update() throws IOException {
    if (shouldUpdate()) {
      LOG.info("Unpacking new version of FitNesse resources. Please be patient...");
      performAllupdates();

      getProperties().put("Version", fitNesseVersion);
      saveProperties();
      return true;
    }
    return false;
  }

  Properties getProperties() {
    return rootProperties;
  }

  Properties loadProperties() throws IOException {
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

  void saveProperties() throws IOException {
    OutputStream os = null;
    File propFile = getPropertiesFile();
    try {
      os = new FileOutputStream(propFile);
      rootProperties.store(os, "#FitNesse properties");
    } catch (IOException e) {
      String fileName = propFile.getAbsolutePath();
      LOG.log(Level.SEVERE, "Failed to save properties file: \"" + fileName + "\". (exception: " + e + ")");
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
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Update failed", e);
    }
  }

  List<Update> makeAllUpdates() throws IOException {
    List<Update> updates = new ArrayList<>();
    updates.addAll(addAllFilesToBeUpdated("updateList", new UpdateFactory() {
      @Override
      public Update create(String source, File destination) {
        return new ReplacingFileUpdate(source, destination);
      }
    }));

    updates.addAll(addAllFilesToBeUpdated("updateDoNotCopyOverList", new UpdateFactory() {
      @Override
      public Update create(String source, File destination) {
        return new FileUpdate(source, destination);
      }
    }));
    return updates;
  }

  private List<Update> addAllFilesToBeUpdated(String updateFile, UpdateFactory updateFactory) throws IOException {
    String updateFileResource = getCorrectPathFromJar(updateFile);
    String[] updateList = parseResource(updateFileResource);
    List<Update> updates = new ArrayList<>();
    for (String updateableFile : updateList) {
      File path = getCorrectPathForTheDestination(updateableFile);
      String source = getCorrectPathFromJar(updateableFile);
      updates.add(updateFactory.create(source, path));
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

  String[] parseResource(String resourceName) throws IOException {
    InputStream in = null;
    try {
      in = ClassLoader.getSystemResourceAsStream(resourceName);
      String content = FileUtil.toString(in);
      return content.split("\n");
    } finally {
      FileUtil.close(in);
    }

  }
  interface UpdateFactory {
    Update create(String source, File destination);

  }

  private boolean shouldUpdate() {
    String versionProperty = getProperties().getProperty("Version");
    return versionProperty == null || !versionProperty.equals(fitNesseVersion);
  }

  public void setFitNesseVersion(String version) {
    fitNesseVersion = version;
  }
}
