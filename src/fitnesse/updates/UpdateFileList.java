package fitnesse.updates;

import util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class UpdateFileList {
  private ArrayList<String> mainDirectories;
  private String updateListContent;
  private String updateDoNotCopyOverContent;
  private HashSet<String> doNotReplaceFiles = new HashSet<String>();
  private String baseDirectory = "";
  static UpdateFileList testUpdater = null;

  public static void main(String[] args) {
    UpdateFileList updater = testUpdater != null ? testUpdater : new UpdateFileList();

    updater.parseCommandLine(args);
    if (updater.directoriesAreValid()) {
      updater.createUpdateList();
      updater.createDoNotUpdateList();
    } else {
      updater.printMessage("Some directories are invalid.");
      updater.exit();
    }
  }

  void printMessage(String message) {
    System.err.println(message);
  }

  void exit() {
    System.exit(1);
  }

  public UpdateFileList() {
    mainDirectories = new ArrayList<String>();
    updateListContent = "";
    updateDoNotCopyOverContent = "";
  }

  public boolean parseCommandLine(String[] args) {
    if (args.length == 0)
      return false;
    for (String arg : args)
      parseArgument(arg);
    return true;
  }

  private void parseArgument(String arg) {
    if (arg.startsWith("-doNotReplace:"))
      addADoNotReplaceFileName(arg);
    else if (arg.startsWith("-baseDirectory:"))
      parseBaseDirectory(arg);
    else
      mainDirectories.add(baseDirectory + arg);
  }

  private void addADoNotReplaceFileName(String arg) {
    String[] components = arg.split(":");
    doNotReplaceFiles.add(components[1]);
  }

  private void parseBaseDirectory(String arg) {
    String[] components = arg.split(":");
    baseDirectory = components[1];
    if (!baseDirectory.endsWith("/"))
      baseDirectory += "/";
  }

  public ArrayList<String> getDirectories() {
    return mainDirectories;
  }

  public boolean directoriesAreValid() {
    for (String dirName : mainDirectories) {
      File checkFile = new File(dirName);
      if (!checkFile.exists()) {
        return false;
      }
    }
    return true;
  }

  public File createUpdateList() {
    for (String dirName : mainDirectories)
      addFilePathsToList(dirName);

    return FileUtil.createFile(new File("updateList"), updateListContent);

  }

  private void addFilePathsToList(String directoryPath) {
    File directory = new File(directoryPath);
    File[] files = FileUtil.getDirectoryListing(directory);
    for (File childFile : files)
      addFilePathToAppropriateList(directoryPath, childFile);

  }

  private void addFilePathToAppropriateList(String directoryPath, File childFile) {
    String childPath = directoryPath + "/" + childFile.getName();
    if (childFile.isDirectory())
      addFilePathsToList(childPath);
    else if (isDoNotReplaceFile(childFile))
      updateDoNotCopyOverContent += makePathLine(childPath);
    else
      updateListContent += makePathLine(childPath);
  }

  private String makePathLine(String path) {
    if (baseDirectory != null && baseDirectory.length() > 0 && path.startsWith(baseDirectory))
      path = path.replace(baseDirectory, "");
    if (path.startsWith("/"))
      path = path.substring(1);
    return path + "\n";
  }

  private boolean isDoNotReplaceFile(File file) {
    String name = file.getPath();
    name = name.replace(baseDirectory, "");
    name = name.replace(File.separator, "/");
    return doNotReplaceFiles.contains(name);
  }

  public File createDoNotUpdateList() {
    if (updateDoNotCopyOverContent.equals(""))
      for (String dirName : mainDirectories)
        addFilePathsToList(dirName);

    return FileUtil.createFile(new File("updateDoNotCopyOverList"), updateDoNotCopyOverContent);
  }
}
