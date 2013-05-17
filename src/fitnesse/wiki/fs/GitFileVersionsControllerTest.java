package fitnesse.wiki.fs;

import fitnesse.wiki.WikiPage;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class GitFileVersionsControllerTest {

  private VersionsControllerFixture fixture;

  @Before
  public void setUp() throws GitAPIException {
    fixture = new VersionsControllerFixture(GitFileVersionsController.class.getCanonicalName());
    fixture.createWikiRoot();
    fixture.initialiseGitRepository();
  }

  @After
  public void tearDown() {
    fixture.cleanUp();
  }

  @Test
  public void shouldAddFileToVersionControl() {
    fixture.savePageWithContent("TestPage", "content");
  }

  @Test
  public void shouldDeleteFileFromVersionControl() {
    fixture.savePageWithContent("TestPage", "content");
    fixture.deletePage("TestPage");
  }

  @Test
  public void shouldReadRecentChangesOnEmptyRepository() {
    GitFileVersionsController versionsController = new GitFileVersionsController();
    WikiPage recentChanges = versionsController.toWikiPage(fixture.getRootPage());

    assertTrue(recentChanges.getData().getContent().startsWith("Unable to read history: "));
  }

  @Test
  public void shouldReadRecentChanges() {
    fixture.savePageWithContent("TestPage", "content");

    GitFileVersionsController versionsController = new GitFileVersionsController();
    WikiPage recentChanges = versionsController.toWikiPage(fixture.getRootPage());

    assertTrue(recentChanges.getData().getContent().startsWith("|FitNesse page TestPage updated."));
  }
}
