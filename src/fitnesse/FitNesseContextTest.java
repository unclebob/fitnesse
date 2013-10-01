// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class FitNesseContextTest {
  private FitNesseContext.Builder builder;

  @Before
  public void setUp() {
    builder = new FitNesseContext.Builder();
  }

  @Test
  public void shouldReadProvidedProperties() {
    Properties properties = new Properties();
    properties.setProperty("TestProp", "found");

    builder.properties = properties;
    FitNesseContext context = builder.createFitNesseContext();
    assertThat(context.getProperty("TestProp"), is("found"));
  }

  @Test
  public void systemPropertyShouldTakePrecedenceOverProvidedProperties() {
    Properties properties = new Properties();
    properties.setProperty("user.name", "xxxxx");

    builder.properties = properties;
    FitNesseContext context = builder.createFitNesseContext();
    assertThat(context.getProperty("user.name"), is(System.getProperty("user.name")));
  }

  @Test
  public void environmentVariableShouldTakePrecedenceOverSystemProperties() {
    Properties properties = new Properties();
    System.setProperty("PATH", "xxxxx");
    builder.properties = properties;
    FitNesseContext context = builder.createFitNesseContext();
    assertThat(context.getProperty("PATH"), is(System.getenv("PATH")));
  }

}
