import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

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
