import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException

import java.util.logging.Logger;

/**
 * Little utility to assemble the updateLists, used from build script.
 */
public class WikiFileListBuilder extends DefaultTask {

  private static final Logger LOG = Logger.getLogger(WikiFileListBuilder.class.getName());

  private static final List<String> VALID_FILE_NAMES = Arrays.asList("content.txt", "properties.xml", ".gitignore");

  @InputDirectory
  private List<String> mainDirectories
  private Set<String> doNotReplaceFiles
  private String baseDirectory
  private String outputDirectory

  @OutputFile
  File updateListFile
  @OutputFile
  File updateDoNotCopyOverListFile

  private String updateListContent = "";
  private String updateDoNotCopyOverContent = "";

  @TaskAction
  public void createUpdateLists() {
    if (directoriesAreValid()) {
      new File(outputDirectory).mkdirs()
      createUpdateList();
      createDoNotUpdateList();
    } else {
      throw new RuntimeException("Some directories are invalid. Aborting.");
    }
  }

  public void setBaseDirectory(final String baseDirectory) {
    this.baseDirectory = baseDirectory
  }

  public void setDoNotReplaceFiles(final Set<String> doNotReplaceFiles) {
    this.doNotReplaceFiles = doNotReplaceFiles
  }

  public void setMainDirectories(final List<String> mainDirectories) {
    this.mainDirectories = mainDirectories
  }

  public void setOutputDirectory(final String outputDirectory) {
    this.outputDirectory = outputDirectory
    this.updateListFile = new File(outputDirectory, "updateList")
    this.updateDoNotCopyOverListFile = new File(outputDirectory, "updateDoNotCopyOverList")
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

    def f = updateListFile
    f.text = updateListContent
    f
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
    return childFile.isDirectory() || VALID_FILE_NAMES.contains(name);
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
    if (baseDirectory != null && !baseDirectory.isEmpty() && path.startsWith(baseDirectory))
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

    def f = updateDoNotCopyOverListFile
    f.text = updateDoNotCopyOverContent;
    f
  }


}
