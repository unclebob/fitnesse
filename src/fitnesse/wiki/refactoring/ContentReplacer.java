// Copyright (C) 2026 by netcare GmbH. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.refactoring;

/**
 * Computes the replaced content of a page, without changing the page.
 */
public interface ContentReplacer {

  /**
   * @param content page content
   * @return the content after the replacement, the same instance if nothing was replaced
   */
  String replace(String content);
}
