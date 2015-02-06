package fitnesse.updates;

import util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

public class UpdateFileList {
  private static final Logger LOG = Logger.getLogger(UpdateFileList.class.getName());

  private List<String> mainDirectories;
  private String updateListContent;
  private String updateDoNotCopyOverContent;
  private HashSet<String> doNotReplaceFiles = new HashSet<String>();
  private String baseDirectory = "";
  private String outputDirectory = "";
  static UpdateFileList testUpdater = null;

  public static void main(String[] args) {
    UpdateFileList updater = testUpdater != null ? testUpdater : new UpdateFileList();

    updater.parseCommandLine(args);
    if (updater.directoriesAreValid()) {
      updater.createUpdateList();
      updater.createDoNotUpdateList();
    } else {
      LOG.severe("Some directories are invalid. Aborting.");
      updater.exit();
    }
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
        baseDirectory = parseDirectoryArgument(arg);
    else if (arg.startsWith("-outputDirectory:"))
        outputDirectory = parseDirectoryArgument(arg);
    else
      mainDirectories.add(baseDirectory + arg);
  }

  private void addADoNotReplaceFileName(String arg) {
    String[] components = arg.split(":");
    doNotReplaceFiles.add(components[1]);
  }

  private String parseDirectoryArgument(String arg) {
    String dir = arg.substring(arg.indexOf(':')+1);
    if (!dir.endsWith("/"))
      dir += "/";
    return dir;
  }

  public List<String> getDirectories() {
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

    return FileUtil.createFile(new File(outputDirectory + "updateList"), updateListContent);

  }

  private void addFilePathsToList(String path) {
    File f = new File(path);
    if (f.isDirectory()) {
      File[] files = FileUtil.getDirectoryListing(f);
      for (File childFile : files)
        if (!isBackupFile(childFile))
          addFilePathToAppropriateList(path, childFile);
    } else if (f.isFile()) {
      String parent = "";
      int index = path.lastIndexOf('/');
      if (index >= 0)
        parent = path.substring(0, index);
      if (!isBackupFile(f))
        addFilePathToAppropriateList(parent, f);
    }
  }

  private boolean isBackupFile(File childFile) {
	return childFile.getName().endsWith(".zip");
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
    String baseDirectoryOnOS = baseDirectory.replace( "/", File.separator);
    name = name.replace(baseDirectoryOnOS, "");
    name = name.replace(File.separator, "/");
    return doNotReplaceFiles.contains(name);
  }

  public File createDoNotUpdateList() {
    if (updateDoNotCopyOverContent.equals(""))
      for (String dirName : mainDirectories)
        addFilePathsToList(dirName);

    return FileUtil.createFile(new File(outputDirectory + "updateDoNotCopyOverList"), updateDoNotCopyOverContent);
  }
}
