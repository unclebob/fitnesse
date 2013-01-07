// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class FitNesseContextTest {
  private FitNesseContext.Builder builder;

  @Before
  public void setUp() {
    builder = new FitNesseContext.Builder();
  }

  @Test
  public void builderPageThemeInputShouldEqualsCreatedContextParam() {
    String pageThemeValue = "testPageTheme";
    builder.pageTheme = pageThemeValue;

    FitNesseContext context = builder.createFitNesseContext();
    assertEquals("pageTheme not correctly set in context from builder", pageThemeValue, context.pageTheme);
  }

  @Test
  public void builderDefaultNewPageContentShouldEqualsCreatedContextParam() {
    String defaultNewPageContentValue = "testDefaultNewPageContentValue";
    builder.defaultNewPageContent = defaultNewPageContentValue;

    FitNesseContext context = builder.createFitNesseContext();
    assertEquals("defaultNewPageContent not correctly set in context from builder", defaultNewPageContentValue, context.defaultNewPageContent);
  }
}
