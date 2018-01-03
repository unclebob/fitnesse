import java.util.logging.Logger

/**
 * Little utility to assemble the updateLists, used from build script.
 */
public class WikiFileListBuilder {

  private static final Logger LOG = Logger.getLogger(WikiFileListBuilder.class.getName());

  private static final List<String> VALID_FILE_NAMES = Arrays.asList("content.txt", "properties.xml");

  private List<String> mainDirectories = []
  private List<String> doNotReplaceFiles = []
  private List<String> skipFiles = []

  private File updateListFile
  private File updateDoNotCopyOverListFile

  private String updateListContent = "";
  private String updateDoNotCopyOverContent = "";

  WikiFileListBuilder(List<String> mainDirectories, List<String> doNotReplaceFiles, List<String> skipFiles, File updateListFile, File updateDoNotCopyOverListFile) {
    this.mainDirectories = mainDirectories
    this.doNotReplaceFiles = doNotReplaceFiles
    this.skipFiles = skipFiles;
    this.updateListFile = updateListFile
    this.updateDoNotCopyOverListFile = updateDoNotCopyOverListFile
  }

  WikiFileListBuilder(List<String> mainDirectories) {
    // for testing mainly
    this.mainDirectories = mainDirectories
  }

  public void createUpdateLists() {
    if (directoriesAreValid()) {
      createUpdateList();
      createDoNotUpdateList();
    } else {
      throw new RuntimeException("Some directories are invalid. Aborting.");
    }
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

    updateListFile.parentFile.mkdirs()
    updateListFile.text = updateListContent
    updateListFile
  }

  private void addFilePathsToList(String path) {
    File f = new File(path);
    if (f.isDirectory()) {
      File[] files = f.listFiles()
      for (File childFile : files)
        if (isWikiFile(childFile))
          addFilePathToAppropriateList(path, childFile);
    } else if (f.isFile()) {
      String parent = "";
      int index = path.lastIndexOf('/');
      if (index >= 0)
        parent = path.substring(0, index);
      if (isWikiFile(f))
        addFilePathToAppropriateList(parent, f);
    }
  }

  private boolean isWikiFile(File childFile) {
    String name = childFile.getName();
    return !isSkipped(childFile) && (childFile.isDirectory() || VALID_FILE_NAMES.contains(name) || name.endsWith(".wiki"));
  }

  private boolean isSkipped(File childFile) {
    String name = childFile.getName()
    boolean skipped = skipFiles.contains(name)
    if (!skipped)
      for (String skipFile : skipFiles) {
        if (skipFile.endsWith("*")) {
          def skipPrefix = skipFile.substring(0, skipFile.length() - 1)
          skipped = name.startsWith(skipPrefix)
        }
        if (skipped)
          break
      }
    return skipped
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
    if (path.startsWith("/"))
      path = path.substring(1);
    return path + "\n";
  }

  private boolean isDoNotReplaceFile(File file) {
    String name = file.getPath();
    name = name.replace(File.separator, "/");
    return doNotReplaceFiles.contains(name);
  }

  public File createDoNotUpdateList() {
    if (updateDoNotCopyOverContent.equals(""))
      for (String dirName : mainDirectories)
        addFilePathsToList(dirName);

    updateDoNotCopyOverListFile.parentFile.mkdirs()
    updateDoNotCopyOverListFile.text = updateDoNotCopyOverContent;
    updateDoNotCopyOverListFile
  }


}
