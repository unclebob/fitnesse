// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.testutil.FitNesseUtil;

/**
 * Task to stop fitnesse.
 * <pre>
 * Usage:
 * &lt;taskdef name=&quot;stop-fitnesse&quot; classname=&quot;fitnesse.ant.StopFitnesseTask&quot; classpathref=&quot;classpath&quot; /&gt;
 * OR
 * &lt;taskdef classpathref=&quot;classpath&quot; resource=&quot;tasks.properties&quot; /&gt;
 *
 * &lt;stop-fitnesse fitnesseport=&quot;8082&quot; /&gt;
 * </pre>
 */
public class StopFitnesseTask extends Task {
  private int fitnessePort = 8082;

  @Override
  public void execute() throws BuildException {
    FitNesseContext context = FitNesseUtil.makeTestContext(fitnessePort);
    try {
      new FitNesse(context).stop();
      log("Sucessfully stoped Fitnesse on port " + fitnessePort);
    }
    catch (Exception e) {
      throw new BuildException("Failed to stop FitNesse. Error Msg: " + e.getMessage(), e);
    }
  }

  /**
   * Set the port on which fitnesse would run. Defaults to <b>8082</b>.
   *
   * @param fitnessePort port on which fitnesse would run
   */
  public void setFitnessePort(int fitnessePort) {
    this.fitnessePort = fitnessePort;
  }
}
