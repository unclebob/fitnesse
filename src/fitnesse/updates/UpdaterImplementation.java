// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

public class UpdaterImplementation extends UpdaterBase {
  public static boolean testing = false;

  private ArrayList<String> updateDoNotCopyOver = new ArrayList<String>();
  private ArrayList<String> updateList = new ArrayList<String>();
  private String fitNesseVersion = FitNesse.VERSION.toString();

  public UpdaterImplementation(FitNesseContext context) throws Exception {

    super(context);
    createUpdateAndDoNotCopyOverLists();
    updates = makeAllUpdates();
  }

  private Update[] makeAllUpdates() throws Exception {
    int listSize = updateList.size() + updateDoNotCopyOver.size();
    updates = new Update[listSize];
    int index = 0;
    for (String updateableFile : updateList) {
      String path = getCorrectPathForTheDestination(updateableFile);
      String source = getCorrectPathFromJar(updateableFile);
      updates[index] = new ReplacingFileUpdate(context.rootPath, source, path);
      index++;
    }
    for (String nonCopyableFile : updateDoNotCopyOver) {
      String path = getCorrectPathForTheDestination(nonCopyableFile);
      String source = getCorrectPathFromJar(nonCopyableFile);
      updates[index] = new FileUpdate(context.rootPath, source, path);
      index++;
    }
    return updates;

  }

  public String getCorrectPathFromJar(String updateableFile) {
    return "Resources/"+updateableFile;
  }


  public String getCorrectPathForTheDestination(String updateableFile) {
    if (updateableFile.startsWith("FitNesseRoot"))
      updateableFile = updateableFile.replace("FitNesseRoot", context.rootDirectoryName);
    int lastIndexOfSlash = updateableFile.lastIndexOf("/");
    if (lastIndexOfSlash >= 0)
      return updateableFile.substring(0, lastIndexOfSlash);
    else
      return updateableFile;
  }

  private void createUpdateAndDoNotCopyOverLists() {
    try {
      getUpdateFilesFromJarFile();
    } catch (Exception e) {
      e.printStackTrace();
    }
    File updateFileList = new File(context.rootPagePath,"updateList");
    File updateDoNotCopyOverFileList = new File(context.rootPagePath,"updateDoNotCopyOverList");
    tryToParseTheFileIntoTheList(updateFileList, updateList);
    tryToParseTheFileIntoTheList(updateDoNotCopyOverFileList, updateDoNotCopyOver);
  }

  public void getUpdateFilesFromJarFile() throws Exception {
    Update update = new FileUpdate(context.rootPagePath,"Resources/updateList", ".");
    update.doUpdate();
    update = new FileUpdate(this.context.rootPagePath, "Resources/updateDoNotCopyOverList", ".");
    update.doUpdate();
  }

  public void tryToParseTheFileIntoTheList(File updateFileList, ArrayList<String> list) {
    if (updateFileList.exists() == false)
      System.out.println("Could Not Find UpdateList");
    else {
      try {
        parseTheFileContentToAList(updateFileList, list);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void parseTheFileContentToAList(File updateFileList, ArrayList<String> list) throws Exception {
    String content = FileUtil.getFileContent(updateFileList);
    String[] filePaths = content.split("\n");
    for (String path : filePaths)
      list.add(path);

  }

  public void update() throws Exception {
    Properties properties = getProperties();
    String versionProperty = properties.getProperty("Version");
    if (versionProperty == null || !versionProperty.equals(fitNesseVersion)) {
      System.err.println("Unpacking new version of FitNesse resources.  Please be patient.");
      super.update();
      properties.put("Version", fitNesseVersion);
      saveProperties();
      System.err.println("You must now reload FitNesse.  Thank you for your patience........");
      if (!testing)
        System.exit(0);
    }
  }

  public void setFitNesseVersion(String version) {
    fitNesseVersion = version;
  }
}
