import org.gradle.api.tasks.*

class LessCompiler extends JavaExec {
  @InputDirectory
  def inputDir

  @Input
  def mainLessFile

  @OutputFile
  def cssFile

  @TaskAction
  public void exec() {
    inputDir.mkdirs()
    args "extra/lesscss/less-rhino-1.7.0.js", new File(inputDir, mainLessFile)
    standardOutput = cssFile.newOutputStream()
    super.exec()
  }
}
