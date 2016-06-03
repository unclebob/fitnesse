package fitnesse.html.template;

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import fitnesse.components.TraversalListener;
import fitnesse.components.Traverser;

public class TraverseDirective extends Directive implements TraversalListener<Object> {

    private InternalContextAdapter context;
    private Node node;
    private Writer writer;

    @Override
    public String getName() {
        return "traverse";
    }

    @Override
    public int getType() {
        return BLOCK;
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

      this.context = context;
      this.writer = writer;
      this.node = node.jjtGetChild(1);

      @SuppressWarnings("unchecked")
      Traverser<Object> traverser = (Traverser<Object>) node.jjtGetChild(0).value(context);

      traverser.traverse(this);

      return true;
    }

    @Override
    public void process(Object page) {

      context.put("result", page);
      try {
        node.render(context, writer);
      } catch (IOException e) {
        throw new TemplateRenderException(e);
      }

    }

    private static class TemplateRenderException extends RuntimeException {
      public TemplateRenderException(final Exception exception) {
        super(exception);
      }
    }
}

