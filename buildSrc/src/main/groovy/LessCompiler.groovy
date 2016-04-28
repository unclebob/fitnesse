import org.gradle.api.tasks.*

class LessCompiler extends JavaExec {
  @InputDirectory
  def inputDir

  def mainLessFile

  @OutputFile
  def cssFile

  @TaskAction
  public void exec() {
    inputDir.mkdirs()
    main "org.mozilla.javascript.tools.shell.Main"
    args "extra/lesscss/less-rhino-1.7.0.js", new File(inputDir, mainLessFile)
    standardOutput = cssFile.newOutputStream()
    super.exec()
  }
}
