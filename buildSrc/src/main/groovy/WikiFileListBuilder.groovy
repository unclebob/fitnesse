import java.util.logging.Logger

/**
 * Little utility to assemble the updateLists, used from build script.
 */
public class WikiFileListBuilder {

  private static final Logger LOG = Logger.getLogger(WikiFileListBuilder.class.getName());

  private static final List<String> VALID_FILE_NAMES = Arrays.asList("content.txt", "properties.xml", ".gitignore");

  private Collection<String> files = []
  private Collection<String> doNotReplaceFiles = []
  private Collection<String> mainDirectories = [];

  private File updateListFile
  private File updateDoNotCopyOverListFile

  private String updateListContent = "";
  private String updateDoNotCopyOverContent = "";

  WikiFileListBuilder(Collection<String> files, Collection<String> doNotReplaceFiles, File updateListFile, File updateDoNotCopyOverListFile) {
    this.files = files
    this.doNotReplaceFiles = doNotReplaceFiles
    this.updateListFile = updateListFile
    this.updateDoNotCopyOverListFile = updateDoNotCopyOverListFile
    for (String file : files)
      addFilePathsToList(file);
  }

  public void createUpdateLists() {
    createUpdateList();
    createDoNotUpdateList();
  }

  public File createUpdateList() {
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
    return childFile.isDirectory() || VALID_FILE_NAMES.contains(name) || name.endsWith(".wiki");
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
