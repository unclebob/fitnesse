package fitnesse.wiki.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface FileVersionsController {

  /**
   * Create or update a file
   * @param file The file to create
   * @param contentFile A (temporary) file with content
   */
  void addFile(File file, File contentFile) throws IOException;

  /**
   * Delete a file.
   * @param file
   */
  void deleteFile(File file);

  /**
   * Add a new directory under version control.
   * @param dir
   */
  void addDirectory(File dir);

  /**
   * Delete a directory. If the directory contains files, those will also be removed.
   * @param dir
   */
  void deleteDirectory(File dir);

  /**
   * Rename a file.
   * @param file new file name
   * @param oldFile old file name
   */
  void renameFile(File file, File oldFile);
}
