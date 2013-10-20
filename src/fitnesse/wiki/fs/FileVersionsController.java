package fitnesse.wiki.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface FileVersionsController {

  /**
   * Create or update a file
   * @param fileVersions The file to create
   */
  void addFile(FileVersion... fileVersions) throws IOException;

  /**
   * Add a new directory under version control.
   * @param dir
   */
  void addDirectory(File dir);

  /**
   * Delete a file or directory.
   * @param file
   */
  void delete(File file);

  /**
   * Rename a file.
   * @param file new file name
   * @param oldFile old file name
   */
  void renameFile(File file, File oldFile);
}
