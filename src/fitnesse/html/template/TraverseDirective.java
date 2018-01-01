package fitnesse.html.template;

import fitnesse.components.TraversalListener;
import fitnesse.components.Traverser;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

public class TraverseDirective extends Directive {

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

      @SuppressWarnings("unchecked")
      Traverser<Object> traverser = (Traverser<Object>) node.jjtGetChild(0).value(context);

      traverser.traverse(new TraverseDirectiveTraverser(context, writer, node.jjtGetChild(1)));

      return true;
    }
}

