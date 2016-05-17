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

package fitnesse.wiki;

import java.io.IOException;
import java.util.Properties;

import fitnesse.plugins.PluginException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class VariableToolTest {

  @Test
  public void replacesVariablesFromPropertiesFile() throws IOException, PluginException {
    Properties properties = new Properties();
    properties.setProperty("replaceMe", "replacedValue");
    SystemVariableSource variableSource = new SystemVariableSource(properties);

    assertThat(new VariableTool(variableSource).replace("a ${replaceMe}"), is("a replacedValue"));
  }

}
