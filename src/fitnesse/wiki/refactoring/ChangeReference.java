package fitnesse.wiki.refactoring;

import fitnesse.wiki.WikiPage;

import java.util.Optional;

public interface ChangeReference {
  Optional<String> changeReference(WikiPage page, String reference);
}
