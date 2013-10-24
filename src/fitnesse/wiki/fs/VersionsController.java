package fitnesse.wiki.fs;

import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface VersionsController {

  void setHistoryDepth(int historyDepth);

  FileVersion[] getRevisionData(String label, File... files);

  Collection<? extends VersionInfo> history(File... files);

  VersionInfo makeVersion(FileVersion... fileVersion) throws IOException;

  VersionInfo addDirectory(final File filePath) throws IOException;

  void rename(File file, File originalFile) throws IOException;

  void delete(File... files);
}