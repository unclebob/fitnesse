package fitnesse.wiki.fs;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import fitnesse.wiki.VersionInfo;

public interface VersionsController {

  /**
   * Set the number of elements (either in days or number) that will be returned by the history() method.
   * @param historyDepth
   */
  void setHistoryDepth(int historyDepth);

  /**
   * Obtain data for the files requested at a specific revision,
   * @param revision The revision to look for
   * @param files Files to obtain data for
   * @return An array of FileVersion elements is returned. The size is equal to the number of files requested,
   *    although there is no grarantee all files will be found at a specific revision.
   */
  FileVersion[] getRevisionData(String revision, File... files);

  /**
   * Get history information for a set of files.
   * @param files Files to look for.
   * @return history
   */
  Collection<? extends VersionInfo> history(File... files);

  /**
   * Store files as one revision.
   * @param fileVersion The files to store
   * @return Version information. VersionInfo.label should refer to this revision, so it can be retrieved later.
   * @throws IOException
   */
  VersionInfo makeVersion(FileVersion... fileVersion) throws IOException;

  /**
   * Add a directory. We only add them one at a time.
   * @param filePath
   * @return
   * @throws IOException
   */
  VersionInfo addDirectory(final FileVersion filePath) throws IOException;

  /**
   * Rename a file. Used for the files/ section. No author information is stored here.
   *
   *
   * @param fileVersion File to rename to.
   * @param originalFile The original file.
   * @throws IOException
   */
  void rename(FileVersion fileVersion, File originalFile) throws IOException;

  /**
   * Delete a bunch of files.
   * @param files
   */
  void delete(FileVersion... files);
}