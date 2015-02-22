package fitnesse.wiki.fs;

import fitnesse.FitNesseContext;
import fitnesse.wiki.*;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static fitnesse.ConfigurationParameter.VERSIONS_CONTROLLER_DAYS;

/**
 * This class requires jGit to be available.
 */
public class GitFileVersionsController implements VersionsController, RecentChanges {

  private static final int RECENT_CHANGES_DEPTH = 100;

  private final SimpleFileVersionsController persistence;

  private final int historyDepth;

  public GitFileVersionsController(Properties properties) {
    this(getVersionDays(properties));
  }

  public GitFileVersionsController(int historyDepth) {
    this.historyDepth = historyDepth;
    // Fix on Disk file system, since that's what GitFileVersionsController can deal with.
    persistence = new SimpleFileVersionsController(new DiskFileSystem());
  }

  public GitFileVersionsController() {
    this(14);
  }

  private static int getVersionDays(Properties properties) {
    String days = properties.getProperty(VERSIONS_CONTROLLER_DAYS.getKey());
    return days == null ? 14 : Integer.parseInt(days);
  }

  @Override
  public FileVersion[] getRevisionData(String label, File... files) {
    // Workaround for CachingPage
    if (label == null) {
      return persistence.getRevisionData(null, files);
    }
    RevCommit revCommit;
    Repository repository = getRepository(files[0]);
    FileVersion[] versions = new FileVersion[files.length];

    try {
      ObjectId rev = repository.resolve(label);
      RevWalk walk = new RevWalk(repository);
      revCommit = walk.parseCommit(rev);
      PersonIdent author = revCommit.getAuthorIdent();
      int counter = 0;
      for (File file : files) {
        String path = getPath(file, repository);
        byte[] content = getRepositoryContent(repository, revCommit, path);
        versions[counter++] = new GitFileVersion(file, content, author.getName(), author.getWhen());
      }
    } catch (IOException e) {
      throw new RuntimeException("Unable to get data for revision " + label, e);
    }
    return versions;
  }

  private byte[] getRepositoryContent(Repository repository, RevCommit revCommit, String fileName) throws IOException {
    TreeWalk treewalk = TreeWalk.forPath(repository, fileName, revCommit.getTree());

    if(treewalk != null) {
      return repository.open(treewalk.getObjectId(0)).getBytes();
    } else {
      return null;
    }
  }

  @Override
  public Collection<? extends VersionInfo> history(final File... files) {
    try {
      return history(files[0], new LogCommandSpec() {
        public LogCommand specify(LogCommand log, Repository repository) {
          for (File file : files) {
            log.addPath(getPath(file, repository));
          }
          return log.setMaxCount(historyDepth);
        }
      });
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  private Collection<GitVersionInfo> history(File file, LogCommandSpec logCommandSpec) throws GitAPIException{
    Repository repository = getRepository(file);
    Git git = new Git(repository);

    Iterable<RevCommit> log = logCommandSpec.specify(git.log(), repository).call();
    List<GitVersionInfo> versions = new ArrayList<GitVersionInfo>(historyDepth);
    for (RevCommit revCommit : log) {
      versions.add(makeVersionInfo(revCommit));
    }
    return versions;
  }

  @Override
  public VersionInfo makeVersion(FileVersion... fileVersions) throws IOException {
    persistence.makeVersion(fileVersions);
    Repository repository = getRepository(fileVersions[0].getFile());
    Git git = new Git(repository);
    try {
      AddCommand adder = git.add();
      for (FileVersion fileVersion : fileVersions) {
        adder.addFilepattern(getPath(fileVersion.getFile(), repository));
      }
      adder.call();
      commit(git, String.format("[FitNesse] Updated files: %s.", formatFileVersions(fileVersions)), fileVersions[0].getAuthor());
    } catch (GitAPIException e) {
      throw new IOException("Unable to commit changes", e);
    }
    return VersionInfo.makeVersionInfo(fileVersions[0].getAuthor(), fileVersions[0].getLastModificationTime());
  }

  @Override
  public void delete(FileVersion... files) {
    Repository repository = getRepository(files[0].getFile());
    Git git = new Git(repository);
    try {
      RmCommand remover = git.rm();
      for (FileVersion fileVersion : files) {
        remover.addFilepattern(getPath(fileVersion.getFile(), repository));
      }
      remover.call();
      commit(git, String.format("[FitNesse] Deleted files: %s.", formatFileVersions(files)), files[0].getAuthor());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    persistence.delete(files);
  }

  private String formatFileVersions(FileVersion[] fileVersions) {
    File[] files = new File[fileVersions.length];
    int counter = 0;
    for (FileVersion fileVersion : fileVersions) {
      files[counter++] = fileVersion.getFile();
    }
    return formatFiles(files);
  }

  String formatFiles(File[] files) {
    StringBuilder builder = new StringBuilder(128);
    int counter = 0;
    for (File file : files) {
      if (counter > 0) {
        builder.append(counter == files.length - 1 ? " and " : ", ");
      }
      builder.append(file.getPath());
      counter++;
    }
    return builder.toString();
  }

  private void commit(Git git, String message, String author) throws GitAPIException {
    Status status = git.status().call();
    if (!status.getAdded().isEmpty() || !status.getChanged().isEmpty() || !status.getRemoved().isEmpty()) {
      if (author==null)
        author = "";
      // set the commit author (if given) but ignores the email
      git.commit().setAuthor(author, "").setMessage(message).call();
    }
  }

  // Paths we feed to Git should be relative to the git repo. Absolute paths are not appreciated.
  private String getPath(File file, Repository repository) {
    String workTreePath = repository.getWorkTree().getAbsolutePath();
    String pagePath = file.getAbsolutePath();

    assert pagePath.startsWith(workTreePath);

    pagePath = pagePath.substring(workTreePath.length());
    
    // git stores paths unix-style
    pagePath = pagePath.replace(File.separatorChar, '/');
    
    // Skip starting '/'
    if (pagePath.startsWith("/"))
        pagePath = pagePath.substring(1);

    return pagePath;
  }

  private GitVersionInfo makeVersionInfo(RevCommit revCommit) {
    PersonIdent authorIdent = revCommit.getAuthorIdent();
    return new GitVersionInfo(revCommit.name(), authorIdent.getName(), authorIdent.getWhen(), revCommit.getShortMessage());
  }

  public static Repository getRepository(File file) {
    try {
      return new FileRepositoryBuilder()
              .findGitDir(file)
              .readEnvironment()
              .setMustExist(true)
              .build();
    } catch (IOException e) {
      throw new RuntimeException("No Git repository found", e);
    }
  }

  @Override
  public void updateRecentChanges(WikiPage page) {
    // Nothing to do, read history from Git repository
  }

  @Override
  public WikiPage toWikiPage(WikiPage root) {
    FileSystemPage fsPage = (FileSystemPage) root;
    WikiPage recentChangesPage = createInMemoryRecentChangesPage(fsPage);
    PageData pageData = recentChangesPage.getData();
    try {
      pageData.setContent(convertToWikiText(history(fsPage.getFileSystemPath(), new LogCommandSpec() {
        @Override
        public LogCommand specify(LogCommand log, Repository repository) {
          return log.setMaxCount(RECENT_CHANGES_DEPTH);
        }
      })));
    } catch (GitAPIException e) {
      pageData.setContent("Unable to read history: " + e.getMessage());
    }
    // No properties, no features.
    pageData.setProperties(new WikiPageProperties());
    recentChangesPage.commit(pageData);
    return recentChangesPage;
  }

  private WikiPage createInMemoryRecentChangesPage(FileSystemPage parent) {
    MemoryFileSystem fileSystem = new MemoryFileSystem();
    return new FileSystemPage(new File(parent.getFileSystemPath(), RECENT_CHANGES), RECENT_CHANGES, parent, new MemoryVersionsController(fileSystem));
  }

  private String convertToWikiText(Collection<GitVersionInfo> history) {
    final SimpleDateFormat dateFormat = new SimpleDateFormat(FitNesseContext.recentChangesDateFormat);
    StringBuilder builder = new StringBuilder(1024);

    for (GitVersionInfo versionInfo : history) {
      builder.append("|")
              .append(versionInfo.getComment())
              .append("|")
              .append(versionInfo.getAuthor())
              .append("|")
              .append(dateFormat.format(versionInfo.getCreationTime()))
              .append("|\n");
    }
    return builder.toString();
  }

  @Override
  public VersionInfo addDirectory(FileVersion dir) throws IOException {
    return persistence.addDirectory(dir);
  }

  @Override
  public void rename(FileVersion fileVersion, File oldFile) throws IOException {
    File renameTo = fileVersion.getFile();
    Repository repository = getRepository(renameTo);
    persistence.rename(fileVersion, oldFile);
    Git git = new Git(repository);
    try {
      git.add()
              .addFilepattern(getPath(renameTo, repository))
              .call();
      git.rm()
              .addFilepattern(getPath(oldFile, repository))
              .call();
      commit(git, String.format("[FitNesse] Renamed file %s to %s.", oldFile.getPath(), renameTo.getPath()), fileVersion.getAuthor());
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }


  private static class GitVersionInfo extends VersionInfo {
    private final String comment;

    private GitVersionInfo(String name, String author, Date creationTime, String comment) {
      super(name, author, creationTime);
      this.comment = comment;
    }

    private String getComment() {
      return comment;
    }
  }

  private static class GitFileVersion implements FileVersion {
    private final File file;
    private final byte[] content;
    private final String author;
    private final Date lastModified;

    public GitFileVersion(File file, byte[] content, String author, Date modified) {
      this.file = file;
      this.content = content;
      this.author = author;
      this.lastModified = modified;
    }

    @Override
    public File getFile() {
      return file;
    }

    @Override
    public InputStream getContent() throws IOException {
      return new ByteArrayInputStream(content);
    }

    @Override
    public String getAuthor() {
      return author;
    }

    @Override
    public Date getLastModificationTime() {
      return lastModified;
    }

  }
}

interface LogCommandSpec {
  LogCommand specify(LogCommand log, Repository repository);
}
