/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */

package util;

import static org.junit.Assert.*;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnvironmentVariableToolTest {

  @Test
  public void testReplace() {
    String path = System.getenv("PATH");
    String stringWithEnvVarInIt = "Test-${PATH}-Test";
    String expectedResult = "Test-" + path + "-Test";
    
    assertTrue("Environment Variable is Replaced.", 
        expectedResult.equals(EnvironmentVariableTool.replace(stringWithEnvVarInIt)));
  
  }
  
  

}
