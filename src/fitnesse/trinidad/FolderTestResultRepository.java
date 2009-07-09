/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */

package fitnesse.trinidad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FolderTestResultRepository implements TestResultRepository {
  private String outputPath;

  public FolderTestResultRepository(String outputPath) {
    this.outputPath = outputPath;
    new File(outputPath).mkdirs();
  }

  public void recordTestResult(TestResult tr) throws IOException {
    String finalPath = new File(outputPath, tr.getName() + ".html")
        .getAbsolutePath();
    System.err.println(tr.getName() + " right:" + tr.getCounts().right
        + " wrong:" + tr.getCounts().wrong + " exeptions: "
        + tr.getCounts().exceptions);
    FileWriter fw = new FileWriter(finalPath);
    fw.write(tr.getContent());
    fw.close();

  }

  public void addFile(File f, String relativeFilePath) throws IOException {
    File dst = new File(outputPath, relativeFilePath);
    dst.getParentFile().mkdirs();
    copy(f, dst);
  }

  private void copy(File src, File dst) throws IOException {
    InputStream in = new FileInputStream(src);
    OutputStream out = new FileOutputStream(dst);
    // Transfer bytes from in to out
    byte[] buf = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }
    in.close();
    out.close();
  }
}
