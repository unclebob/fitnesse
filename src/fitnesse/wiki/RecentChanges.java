package fitnesse.wiki;

/**
 * Created with IntelliJ IDEA.
 * User: arjan
 * Date: 5/17/13
 * Time: 2:37 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RecentChanges {
  String RECENT_CHANGES = "RecentChanges";

  void updateRecentChanges(WikiPage page);

  WikiPage toWikiPage(WikiPage root);
}
