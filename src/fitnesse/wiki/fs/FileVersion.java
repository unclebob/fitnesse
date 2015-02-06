package fitnesse.wiki.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Represents a file under version control.
 */
public interface FileVersion {

  File getFile();

  InputStream getContent() throws IOException;

  String getAuthor();

  Date getLastModificationTime();
}
