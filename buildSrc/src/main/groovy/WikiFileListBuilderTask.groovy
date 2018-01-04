import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

public class WikiFileListBuilderTask extends DefaultTask {
  List<String> mainDirectories
  private List<String> doNotReplaceFiles
  private List<String> skipFiles

  @OutputFile
  File updateListFile
  @OutputFile
  File updateDoNotCopyOverListFile

  @TaskAction
  public void taskAction() {
    def updater = new WikiFileListBuilder(mainDirectories, doNotReplaceFiles, skipFiles, updateListFile, updateDoNotCopyOverListFile)
    updater.createUpdateLists()
  }

  public void setDoNotReplaceFiles(final List<String> doNotReplaceFiles) {
    this.doNotReplaceFiles = doNotReplaceFiles
  }

  public void setSkipFiles(final List<String> skipFiles) {
    this.skipFiles = skipFiles
  }

  public void setMainDirectories(final List<String> mainDirectories) {
    this.mainDirectories = mainDirectories
  }

  public void setOutputDirectory(final String outputDirectory) {
    this.updateListFile = new File(outputDirectory, "updateList")
    this.updateDoNotCopyOverListFile = new File(outputDirectory, "updateDoNotCopyOverList")
  }

}
