// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UpdaterImplementation extends UpdaterBase {
  public static boolean testing = false;

  private ArrayList<String> updateDoNotCopyOver = new ArrayList<String>();
  private ArrayList<String> updateList = new ArrayList<String>();
  private String fitNesseVersion = FitNesse.VERSION.toString();

  public UpdaterImplementation(FitNesseContext context) throws IOException {
    super(context);
    createUpdateAndDoNotCopyOverLists();
    updates = makeAllUpdates();
  }

  private Update[] makeAllUpdates() {
    List<Update> updates = new ArrayList<Update>();
    addAllFilesToBeReplaced(updates);
    addAllFilesThatShouldNotBeCopiedOver(updates);
    return updates.toArray(new Update[updates.size()]);

  }

  private void addAllFilesThatShouldNotBeCopiedOver(List<Update> updates) {
    for (String nonCopyableFile : updateDoNotCopyOver) {
      String path = getCorrectPathForTheDestination(nonCopyableFile);
      String source = getCorrectPathFromJar(nonCopyableFile);
      updates.add(new FileUpdate(context.rootPath, source, path));
    }
  }

  private void addAllFilesToBeReplaced(List<Update> updates) {
    for (String updateableFile : updateList) {
      String path = getCorrectPathForTheDestination(updateableFile);
      String source = getCorrectPathFromJar(updateableFile);
      updates.add(new ReplacingFileUpdate(context.rootPath, source, path));
    }
  }

  public String getCorrectPathFromJar(String updateableFile) {
    return "Resources/" + updateableFile;
  }


  public String getCorrectPathForTheDestination(String updateableFile) {
    if (updateableFile.startsWith("FitNesseRoot"))
      updateableFile = updateableFile.replace("FitNesseRoot", context.rootDirectoryName);
    return FileUtil.getPathOfFile(updateableFile);
  }

  private void createUpdateAndDoNotCopyOverLists() {
    tryToGetUpdateFilesFromJarFile();
    File updateFileList = new File(context.rootPagePath, "updateList");
    File updateDoNotCopyOverFileList = new File(context.rootPagePath, "updateDoNotCopyOverList");
    tryToParseTheFileIntoTheList(updateFileList, updateList);
    tryToParseTheFileIntoTheList(updateDoNotCopyOverFileList, updateDoNotCopyOver);
  }

  private void tryToGetUpdateFilesFromJarFile() {
    try {
      getUpdateFilesFromJarFile();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void getUpdateFilesFromJarFile() throws IOException {
    Update update = new FileUpdate(context.rootPagePath, "Resources/updateList", ".");
    update.doUpdate();
    update = new FileUpdate(this.context.rootPagePath, "Resources/updateDoNotCopyOverList", ".");
    update.doUpdate();
  }

  public void tryToParseTheFileIntoTheList(File updateFileList, ArrayList<String> list) {
    if (updateFileList.exists() == false)
      throw new RuntimeException("Could Not Find UpdateList");

    try {
      parseTheFileContentToAList(updateFileList, list);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  private void parseTheFileContentToAList(File updateFileList, ArrayList<String> list) throws IOException {
    String content = FileUtil.getFileContent(updateFileList);
    String[] filePaths = content.split("\n");
    for (String path : filePaths)
      list.add(path);

  }

  public void update() throws IOException {
    if (shouldUpdate()) {
      System.err.println("Unpacking new version of FitNesse resources. Please be patient.");
      super.update();
      System.err.println("\n\n" +
          "********************************************************************************\n" +
          "    Files have been updated to a new version. Please read the release notes\n" +
          "    on http://localhost:" +
          context.getPort() +
          "/FitNesse.ReleaseNotes to find out about the new\n" +
          "    features and fixes.\n" +
          "********************************************************************************\n\n");
      
      getProperties().put("Version", fitNesseVersion);
      saveProperties();
    }
  }

  private void exit() {
    if (!testing)
      System.exit(0);
  }

  private boolean shouldUpdate() {
    String versionProperty = getProperties().getProperty("Version");
    return versionProperty == null || !versionProperty.equals(fitNesseVersion);
  }

  public void setFitNesseVersion(String version) {
    fitNesseVersion = version;
  }
}
