package fitnesse.html.template;

import fitnesse.wikitext.Utils;
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
    String text = Utils.escapeHTML(String.valueOf(node.jjtGetChild(0).value(context)));
    writer.write(text);
    return true;
  }
}
