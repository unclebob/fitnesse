package fitnesse.wiki.fs;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

}
