package fitnesse.html.template;

import fitnesse.html.HtmlUtil;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;

public class EscapeDirective extends Directive {
  private static final Pattern INVALID_CONTROL_CHAR_PATTERN = Pattern.compile("[\\u0000-\\u0008\\u000B-\\u000C\\u000E-\\u001F]");

  @Override
  public String getName() {
    return "escape";
  }

  @Override
  public int getType() {
    return LINE;
  }

  @Override
  public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException {
    Object value = node.jjtGetChild(0).value(context);
    if (value != null) {
      String text = HtmlUtil.escapeHTML(getValueString(value));
      writer.write(text);
    }
    return true;
  }

  private String getValueString(Object value) {
    return INVALID_CONTROL_CHAR_PATTERN.matcher(value.toString()).replaceAll("");
  }
}
