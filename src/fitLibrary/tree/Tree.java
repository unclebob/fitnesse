/*
 * @author Rick Mugridge on Jan 4, 2005
 *
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.tree;

import java.util.List;

/**
 *
 */
public interface Tree {
    String getTitle();
    String getText(); // Title without HTML tags
    List getChildren();
    String text();
}
