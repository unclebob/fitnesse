// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import fit.ColumnFixture;
import fitnesse.runner.TestRunner;
import fitnesse.wikitext.Utils;

public class TestRunnerFixture extends ColumnFixture {
  public String pageName;
  public String args;
  private TestRunner runner;
  private ByteArrayOutputStream outputBytes;

  public void execute() throws Exception {
    outputBytes = new ByteArrayOutputStream();
    runner = new TestRunner(new PrintStream(outputBytes));
    runner.run(buildArgs());
  }

  private String[] buildArgs() {
    List<String> list = new LinkedList<String>();
    if (args != null && !args.equals("")) {
      String[] startingArg = args.split(" ");
      for (int i = 0; i < startingArg.length; i++)
        list.add(startingArg[i]);
    }
    String hostName;
    try {
      hostName = java.net.InetAddress.getLocalHost().getHostName();
    }
    catch (UnknownHostException e) {
      hostName = "localhost";
    }
    list.add(hostName);
    list.add(String.valueOf(FitnesseFixtureContext.context.port));
    list.add(pageName);

    return (String[]) list.toArray(new String[]
      {});
  }

  public int exitCode() {
    return runner.exitCode();
  }

  public String output() {
    String output = outputBytes.toString();
    output = output.replaceAll("\r", "");
    output = output.replaceAll("\n", "\\\\n");
    output = Utils.escapeHTML(output);

    return output;
  }
}
