import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*

public class WikiFileListBuilderTask extends DefaultTask {
  @InputFiles
  Closure<Collection<String>> filesClosure

  private Collection<String> doNotReplaceFiles

  @OutputFile
  File updateListFile
  @OutputFile
  File updateDoNotCopyOverListFile

  @Input
  Collection<String> wikiFiles

  @TaskAction
  public void taskAction() {
    def updater = new WikiFileListBuilder(getWikiFiles(), doNotReplaceFiles, updateListFile, updateDoNotCopyOverListFile)
    updater.createUpdateLists()
  }

  public void setDoNotReplaceFiles(final Collection<String> doNotReplaceFiles) {
    this.doNotReplaceFiles = doNotReplaceFiles
  }

  public void setFiles(final Closure<Collection<String>> files) {
    this.filesClosure = files
  }

  public void setFiles(final Collection<String> files) {
    this.filesClosure = { files }
  }

  public void setFiles(final SourceSet files) {
    this.filesClosure = { files.collect { it.name } }
  }

  public void setOutputDirectory(final String outputDirectory) {
    this.updateListFile = new File(outputDirectory, "updateList")
    this.updateDoNotCopyOverListFile = new File(outputDirectory, "updateDoNotCopyOverList")
  }

  public List<String> getWikiFiles() {
    if (wikiFiles == null) wikiFiles = filesClosure()
    wikiFiles
  }
}
