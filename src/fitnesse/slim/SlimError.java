// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

public class SlimError extends RuntimeException {
  private static final long serialVersionUID = 1L;
  private final boolean prettyPrint;

  public SlimError(String s) {
    super(s);
    this.prettyPrint = false;
  }

  public SlimError(String s, String tag, boolean prettyPrint) {
    super(prettyPrint ? makeSlimMessage(s, tag) : s);
    this.prettyPrint = prettyPrint;
  }

  public SlimError(String s, Throwable throwable) {
    super(s, throwable);
    this.prettyPrint = false;
  }

  public SlimError(String s, Throwable throwable, String tag, boolean prettyPrint) {
    super(prettyPrint ? makeSlimMessage(s, tag) : s, throwable);
    this.prettyPrint = prettyPrint;
  }

  public SlimError(Throwable e) {
    this(e.getClass().getName() + " " + e.getMessage());
  }

  public static String makeSlimMessage(String msg, String tag) {
    StringBuilder sb = new StringBuilder();
    sb.append(SlimVersion.PRETTY_PRINT_TAG_START);
    if (tag != null && !tag.isEmpty()) {
      sb.append(tag);
      // Separator between tag and message
      sb.append(" ");
    }
    if (msg != null && !msg.isEmpty()) {
      sb.append(msg);
    }
    sb.append(SlimVersion.PRETTY_PRINT_TAG_END);
    return sb.toString();
  }

  public static String extractSlimMessage(String msg) {
    msg = msg.replaceFirst(".*" + SlimVersion.PRETTY_PRINT_TAG_START, "");
    msg = msg.replaceFirst(SlimVersion.PRETTY_PRINT_TAG_END, "");
    return msg;
  }

  public static boolean hasSlimMessage(String msg) {
    return msg.contains(SlimVersion.PRETTY_PRINT_TAG_START) &&
      msg.contains(SlimVersion.PRETTY_PRINT_TAG_END);
  }
}
