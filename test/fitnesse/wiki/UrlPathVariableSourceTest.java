// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.wiki;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class UrlPathVariableSourceTest {

  @Test
  public void shouldReadProvidedProperties() {
    Properties properties = new Properties();
    properties.setProperty("TestProp", "found");

    SystemVariableSource source = new SystemVariableSource(properties);
    UrlPathVariableSource urlSource = new UrlPathVariableSource(source, null);

    assertThat(urlSource.findVariable("TestProp").getValue(), is("found"));
  }

  @Test
  public void urlVariableShouldTakePrecedenceOverEnvironmentAndSystemProperties() {
    Properties properties = new Properties();
    System.setProperty("PATH", "xxxxx");
    properties.setProperty("PATH", "zzzzz");

    SystemVariableSource source = new SystemVariableSource(properties);
    Map<String, String> urlVariables = new HashMap<>();
    urlVariables.put("PATH", "yyyyy");
    UrlPathVariableSource urlSource = new UrlPathVariableSource(source, urlVariables);

    assertThat(urlSource.findVariable("PATH").getValue(), is(urlVariables.get("PATH")));
  }

}
