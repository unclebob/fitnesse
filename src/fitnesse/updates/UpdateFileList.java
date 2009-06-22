package fitnesse.updates;

import util.FileUtil;

import java.io.File;
import java.util.ArrayList;

public class UpdateFileList {
  private ArrayList<String> mainDirectories;
  private String updateListContent;
  private String updateDoNotCopyOverContent;

  public static void main(String[] args){
    UpdateFileList updater = new UpdateFileList();
    updater.parseCommandLine(args);
    if(updater.directoriesAreValid()){
      updater.createUpdateList();
      updater.createDoNotUpdateList();
    }
  }

  public UpdateFileList() {
    mainDirectories = new ArrayList<String>();
    updateListContent = "";
    updateDoNotCopyOverContent="";
}

  public boolean parseCommandLine(String[] dirs) {
    if (dirs.length == 0)
      return false;
    for (String dirName : dirs)
      mainDirectories.add(dirName);
    return true;
  }

  public ArrayList<String> getDirectories() {
    return mainDirectories;
  }

  public boolean directoriesAreValid() {
    for (String dirName : mainDirectories) {
      File checkFile = new File(dirName);
      if (!checkFile.exists())
        return false;
    }

    return true;
  }

  public File createUpdateList() {
    for (String dirName : mainDirectories)
      addFilePathsToList(new File(dirName), dirName);

    return FileUtil.createFile(new File("updateList"), updateListContent);

  }

  private void addFilePathsToList(File file, String parentPath) {
    File[] files = FileUtil.getDirectoryListing(file);
    for (File childFile : files) {
      String currentPath = parentPath + "/" + childFile.getName();
      if (childFile.isDirectory())
        addFilePathsToList(childFile, currentPath);
      else if (isASpecialFile(childFile))
        updateDoNotCopyOverContent += currentPath + "";
      else
        updateListContent += currentPath + " ";
    }
  }

  private boolean isASpecialFile(File file) {
    if (file.getName().equals("fitnesse.css"))
      return true;
    if (file.getName().equals("fitnesse_print.css"))
      return true;
    return false;
  }

  public File createDoNotUpdateList() {
     if(updateDoNotCopyOverContent.equals(""))
      for (String dirName : mainDirectories)
        addFilePathsToList(new File(dirName), dirName);

    return FileUtil.createFile(new File("updateDoNotCopyOverList"), updateDoNotCopyOverContent);
  }
}
