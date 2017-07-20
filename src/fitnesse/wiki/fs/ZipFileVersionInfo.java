package fitnesse.wiki.fs;


import fitnesse.wiki.VersionInfo;
import fitnesse.util.Clock;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZipFileVersionInfo extends VersionInfo {
  public static final Pattern COMPEX_NAME_PATTERN = Pattern.compile("(?:([a-zA-Z][^\\-]*)-)?(?:\\d+-)?(\\d{14})");

  public static SimpleDateFormat makeVersionTimeFormat() {
    //SimpleDateFormat is not thread safe, so we need to create each instance independently.
    return new SimpleDateFormat("yyyyMMddHHmmss");
  }

  private final File file;

  public ZipFileVersionInfo(String complexName, String author, Date creationTime, File file) {
    super(complexName, author, creationTime);
    this.file = file;
  }

  public static ZipFileVersionInfo makeVersionInfo(File file) {
    String complexName = makeVersionName(file);
    Matcher match = COMPEX_NAME_PATTERN.matcher(complexName);
    String author = "";
    Date creationTime = Clock.currentDate();
    if (match.find()) {
      author = match.group(1);
      if (author == null)
        author = "";
      try {
        creationTime = makeVersionTimeFormat().parse(match.group(2));
      } catch (ParseException e) {
        throw new IllegalStateException(e);
      }
    }
    return new ZipFileVersionInfo(complexName, author, creationTime, file);
  }

  private static String makeVersionName(final File file) {
    final String name = file.getName();
    return name.substring(0, name.length() - 4);
  }

  public File getFile() {
    return file;
  }
}
