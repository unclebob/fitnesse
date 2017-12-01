package fitnesse.html.template;

import fitnesse.components.TraversalListener;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

class TraverseDirectiveTraverser implements TraversalListener<Object> {
    private InternalContextAdapter context;
    private Node node;
    private Writer writer;

    public TraverseDirectiveTraverser(InternalContextAdapter context, Writer writer, Node node) {
        this.context = context;
        this.writer = writer;
        this.node = node;
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
