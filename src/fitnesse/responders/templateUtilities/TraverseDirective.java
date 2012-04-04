package fitnesse.responders.templateUtilities;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.ASTBlock;
import org.apache.velocity.runtime.parser.node.Node;

import fitnesse.components.TraversalListener;
import fitnesse.components.Traverser;
import fitnesse.responders.search.ResultResponder;
import fitnesse.wiki.WikiPage;

public class TraverseDirective extends Directive implements TraversalListener<Object> {

    private Log log;
    private InternalContextAdapter context;
    private Node node;
    private Writer writer;
    private Traverser traverser;

    @Override
    public String getName() {
        return "traverse";
    }

    @Override
    public int getType() {
        return BLOCK;
    }

    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
        super.init(rs, context, node);
        log = rs.getLog();
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) 
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

      this.context = context;
      this.writer = writer;
      this.traverser = (Traverser) node.jjtGetChild(0).value(context);
      this.node = node.jjtGetChild(1);
      
      traverser.traverse(this);
      
      return true;
    }

    @Override
    public void process(Object page) {

      context.put("result", page);
      try {
        node.render(context, writer);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      
    }

}

