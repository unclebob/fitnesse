package fitnesse.wiki.fs;

import static fitnesse.wiki.fs.SimpleFileVersionsController.contentFilename;
import static fitnesse.wiki.fs.SimpleFileVersionsController.parsePropertiesXml;
import static fitnesse.wiki.fs.SimpleFileVersionsController.propertiesFilename;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

/**
 * This class requires jGit to be available.
 */
public class GitFileVersionsController implements VersionsController {

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
    Repository repository = getRepository(page);
    Git git = new Git(repository);
    String fileSystemPath = getPath(page, repository);

    try {
      Iterable<RevCommit> log = git.log()
              .addPath(fileSystemPath + "/" + contentFilename)
              .addPath(fileSystemPath + "/" + propertiesFilename)
              .setMaxCount(historyDepth)
              .call();
      List<VersionInfo> versions = new ArrayList<VersionInfo>(historyDepth);
      for (RevCommit revCommit : log) {
        versions.add(makeVersionInfo(revCommit));
      }
      return versions;
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
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
      commit(git, String.format("FitNesse page %s updated.", page.getName()));
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
      commit(git, String.format("FitNesse page %s deleted.", page.getName()));
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

  private VersionInfo makeVersionInfo(RevCommit revCommit) {
    PersonIdent authorIdent = revCommit.getAuthorIdent();
    return new VersionInfo(revCommit.name(), authorIdent.getName(), authorIdent.getWhen());
  }

  private Repository getRepository(FileSystemPage page) {
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
}
