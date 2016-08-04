import java.io.File;
import java.util.List;
import java.util.Set;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public class WikiFileListBuilderTask extends DefaultTask {
  List<String> mainDirectories
  private List<String> doNotReplaceFiles

  @OutputFile
  File updateListFile
  @OutputFile
  File updateDoNotCopyOverListFile

  @TaskAction
  public void taskAction() {
    def updater = new WikiFileListBuilder(mainDirectories, doNotReplaceFiles, updateListFile, updateDoNotCopyOverListFile)
    updater.createUpdateLists()
  }

  public void setDoNotReplaceFiles(final List<String> doNotReplaceFiles) {
    this.doNotReplaceFiles = doNotReplaceFiles
  }

  public void setMainDirectories(final List<String> mainDirectories) {
    this.mainDirectories = mainDirectories
  }

  public void setOutputDirectory(final String outputDirectory) {
    this.updateListFile = new File(outputDirectory, "updateList")
    this.updateDoNotCopyOverListFile = new File(outputDirectory, "updateDoNotCopyOverList")
  }

}
