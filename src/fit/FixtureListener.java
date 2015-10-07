// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

public interface FixtureListener {
  void tableFinished(Parse table);

  void tablesFinished(Counts count);
}
