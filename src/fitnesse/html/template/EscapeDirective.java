package fitnesse.html.template;

import fitnesse.html.HtmlUtil;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

public class EscapeDirective extends Directive {
  @Override
  public String getName() {
    return "escape";
  }

  @Override
  public int getType() {
    return LINE;
  }

  @Override
  public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
    Object value = node.jjtGetChild(0).value(context);
    if (value != null) {
        String text = HtmlUtil.escapeHTML(String.valueOf(value).replaceAll(
                "([\\u0000-\\u0008\\u000B-\\u000C\\u000E-\\u001F])",""));
      writer.write(text);
    }
    return true;
  }
}
