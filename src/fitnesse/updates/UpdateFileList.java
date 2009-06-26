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

  public static void main(String[] args) {
    UpdateFileList updater = new UpdateFileList();

    updater.parseCommandLine(args);
    if (updater.directoriesAreValid()) {
      updater.createUpdateList();
      updater.createDoNotUpdateList();
    } else {
      System.err.println("Some directories are invalid.");
      System.exit(1);
    }
  }

  public UpdateFileList() {
    mainDirectories = new ArrayList<String>();
    updateListContent = "";
    updateDoNotCopyOverContent = "";
  }

  public boolean parseCommandLine(String[] args) {
    if (args.length == 0)
      return false;
    for (String arg : args) {
      if (arg.startsWith("-doNotReplace:")) {
        String[] components = arg.split(":");
        doNotReplaceFiles.add(components[1]);
      } else if (arg.startsWith("-baseDirectory:")) {
        String[] components = arg.split(":");
        baseDirectory = components[1];
        if (!baseDirectory.endsWith("/"))
          baseDirectory += System.getProperty("file.separator");
      } else {
        mainDirectories.add(baseDirectory +arg);
      }
    }

    return true;
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

    for (File childFile : files) {
      String childPath = directoryPath + System.getProperty("file.separator") + childFile.getName();
      if (childFile.isDirectory())
        addFilePathsToList(childPath);
      else if (isDoNotReplaceFile(childFile)) {
        updateDoNotCopyOverContent += makePathLine(childPath);
      } else
        updateListContent += makePathLine(childPath);
    }
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
    name =  name.replace(baseDirectory, "");
    if(name.startsWith("/"))
      name = name.substring(1);
    return doNotReplaceFiles.contains(name);
  }

  public File createDoNotUpdateList() {
    if (updateDoNotCopyOverContent.equals(""))
      for (String dirName : mainDirectories)
        addFilePathsToList(dirName);

    return FileUtil.createFile(new File("updateDoNotCopyOverList"), updateDoNotCopyOverContent);
  }
}
