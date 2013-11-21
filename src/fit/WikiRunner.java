// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.


public class WikiRunner extends FileRunner {

  public static void main(String argv[]) {
    new WikiRunner().run(argv);
  }

  public void process() {
    try {
      String tags[] = {"wiki", "table", "tr", "td"};
      tables = new Parse(input, tags);    // look for wiki tag enclosing tables
      fixture.doTables(tables.parts);     // only do tables within that tag
    } catch (Exception e) {
      exception(e);
    }
    tables.print(output);
  }
}
