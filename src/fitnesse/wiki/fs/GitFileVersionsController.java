package fitnesse.wiki.fs;

import fitnesse.FitNesseContext;
import fitnesse.wiki.*;
import fitnesse.wiki.mem.InMemoryPage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static fitnesse.wiki.fs.SimpleFileVersionsController.*;

/**
 * This class requires jGit to be available.
 */
public class GitFileVersionsController implements VersionsController, RecentChanges {

  private static final int RECENT_CHANGES_DEPTH = 100;

  private final VersionsController persistence;

  private int historyDepth;

  public GitFileVersionsController() {
    // Fix on Disk file system, since that's what GitFileVersionsController can deal with.
    persistence = new SimpleFileVersionsController(new DiskFileSystem());
  }

  @Override
  public void setHistoryDepth(int historyDepth) {
    this.historyDepth = historyDepth;
  }

  @Override
  public PageData getRevisionData(FileSystemPage page, String label) {
    // Workaround for CachingPage
    if (label == null) {
      return persistence.getRevisionData(page, null);
    }
    String content, propertiesXml;
    RevCommit revCommit;
    Repository repository = getRepository(page);

    try {
      String fileSystemPath = getPath(page, repository);
      ObjectId rev = repository.resolve(label);
      RevWalk walk = new RevWalk(repository);
      revCommit = walk.parseCommit(rev);

      content = getRepositoryContent(repository, revCommit, fileSystemPath + "/" + contentFilename);
      propertiesXml = getRepositoryContent(repository, revCommit, fileSystemPath + "/" + propertiesFilename);
    } catch (IOException e) {
      throw new RuntimeException("Unable to get data for revision " + label, e);
    }

    final PageData pageData = new PageData(page);
    pageData.setContent(content);
    pageData.setProperties(parsePropertiesXml(propertiesXml, revCommit.getAuthorIdent().getWhen().getTime()));
    return pageData;
  }

  private String getRepositoryContent(Repository repository, RevCommit revCommit, String fileName) throws IOException {
    TreeWalk treewalk = TreeWalk.forPath(repository, fileName, revCommit.getTree());

    if(treewalk != null) {
      return new String(repository.open(treewalk.getObjectId(0)).getBytes());
    } else {
      return null;
    }
  }

  @Override
  public Collection<? extends VersionInfo> history(FileSystemPage page) {
    try {
      return history(page, new LogCommandSpec() {
        public LogCommand specify(LogCommand log, String fileSystemPath) {
           return log
                  .addPath(fileSystemPath + "/" + contentFilename)
                  .addPath(fileSystemPath + "/" + propertiesFilename)
                  .setMaxCount(historyDepth);

        }
      });
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  private Collection<GitVersionInfo> history(FileSystemPage page, LogCommandSpec logCommandSpec) throws GitAPIException{
    Repository repository = getRepository(page);
    Git git = new Git(repository);
    String fileSystemPath = getPath(page, repository);

    Iterable<RevCommit> log = logCommandSpec.specify(git.log(), fileSystemPath).call();
    List<GitVersionInfo> versions = new ArrayList<GitVersionInfo>(historyDepth);
    for (RevCommit revCommit : log) {
      versions.add(makeVersionInfo(revCommit));
    }
    return versions;
  }

  @Override
  public VersionInfo makeVersion(FileSystemPage page, PageData data) {
    persistence.makeVersion(page, data);
    Repository repository = getRepository(page);
    Git git = new Git(repository);
    String fileSystemPath = getPath(page, repository);
    try {
      git.add()
              .addFilepattern(fileSystemPath + "/" + contentFilename)
              .addFilepattern(fileSystemPath + "/" + propertiesFilename)
              .call();
      commit(git, String.format("FitNesse page %s updated.", PathParser.render(page.getPageCrawler().getFullPath())));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return getCurrentVersion(page, repository);
  }

  @Override
  public VersionInfo getCurrentVersion(FileSystemPage page) {
    return getCurrentVersion(page, getRepository(page));
  }

  @Override
  public void delete(FileSystemPage page) {
    Repository repository = getRepository(page);
    Git git = new Git(repository);
    String fileSystemPath = getPath(page, repository);
    try {
      git.rm()
              .addFilepattern(fileSystemPath + "/" + contentFilename)
              .addFilepattern(fileSystemPath + "/" + propertiesFilename)
              .call();
      commit(git, String.format("FitNesse page %s deleted.", PathParser.render(page.getPageCrawler().getFullPath())));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    persistence.delete(page);
  }

  private void commit(Git git, String message) throws GitAPIException {
    Status status = git.status().call();
    if (!status.getAdded().isEmpty() || !status.getChanged().isEmpty() || !status.getRemoved().isEmpty()) {
      git.commit().setMessage(message).call();
    }
  }

  // Paths we feed to Git should be relative to the git repo. Absolute paths are not appreciated.
  private String getPath(FileSystemPage page, Repository repository) {
    String workTreePath = repository.getWorkTree().getAbsolutePath();
    String pagePath = new File(page.getFileSystemPath()).getAbsolutePath();

    assert pagePath.startsWith(workTreePath);

    // Add 1 for trailing '/' (not included in abs. path)
    return pagePath.substring(workTreePath.length() + 1);
  }

  private VersionInfo getCurrentVersion(FileSystemPage page, Repository repository) {
    try {
      ObjectId head = repository.resolve("HEAD");
      RevWalk walk = new RevWalk(repository);
      RevCommit revCommit = walk.parseCommit(head);
      return makeVersionInfo(revCommit);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private GitVersionInfo makeVersionInfo(RevCommit revCommit) {
    PersonIdent authorIdent = revCommit.getAuthorIdent();
    return new GitVersionInfo(revCommit.name(), authorIdent.getName(), authorIdent.getWhen(), revCommit.getShortMessage());
  }

  public static Repository getRepository(FileSystemPage page) {
    try {
      return new FileRepositoryBuilder()
              .findGitDir(new File(page.getFileSystemPath()))
              .readEnvironment()
              .setMustExist(true)
              .build();
    } catch (IOException e) {
      throw new RuntimeException("No Git repository found", e);
    }

  }

  @Override
  public void updateRecentChanges(PageData pageData) {
    // Nothing to do, read history from Git repository
  }

  @Override
  public WikiPage toWikiPage(WikiPage root) {
    FileSystemPage fsPage = (FileSystemPage) root;
    WikiPage recentChangesPage = InMemoryPage.createChildPage(RECENT_CHANGES, fsPage);
    PageData pageData = recentChangesPage.getData();
    try {
      pageData.setContent(convertToWikiText(history(fsPage, new LogCommandSpec() {
        @Override
        public LogCommand specify(LogCommand log, String fileSystemPath) {
          return log.setMaxCount(RECENT_CHANGES_DEPTH);
        }
      })));
    } catch (GitAPIException e) {
      pageData.setContent("Unable to read history: " + e.getMessage());
    }
    // No properties, no features.
    pageData.setProperties(recentChangesPageProperties());
    recentChangesPage.commit(pageData);
    return recentChangesPage;
  }

  private WikiPageProperties recentChangesPageProperties() {
    return new WikiPageProperties();
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
}

interface LogCommandSpec {
  LogCommand specify(LogCommand log, String fileSystemPath);
}
