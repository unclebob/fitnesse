package fitnesse.util;

import java.util.Vector;

import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.util.NodeList;

/**
 * HtmlParserUtils -- all sorts of trickiness not implemented in the html-parser library
 */
public final class HtmlParserTools {

  private HtmlParserTools() {

  }

  /**
   * Clone just this one node. No nesting
   */
  public static Node flatClone(Node node) {
    if (node == null) return null;
    Node newNode = cloneOnlyNode(node, null);
    newNode.setChildren(new NodeList());
    if (newNode instanceof Tag) {
      ((Tag) newNode).setEndTag(null);
    }
    return newNode;
  }

  /**
   * Make a 1:1 clone of the NodeList.
   *
   * @param nodeList
   * @return
   */
  public static NodeList deepClone(NodeList nodeList) {
    return deepClone(nodeList, null);
  }

  /**
   * Make a 1:1 clone of the Node.
   *
   * @param node
   * @return
   */
  public static <T extends Node> T deepClone(T node) {
    return (T) deepClone(new NodeList(node), null).elementAt(0);
  }

  /**
   * Get closing node for this node, if any.
   * @param node
   * @return node or null
   */
  public static Node endTag(Node node) {
    // No copying required since the node is not modified and has no children.
    return node instanceof Tag ? ((Tag) node).getEndTag() : null;
  }


  private static NodeList deepClone(NodeList tree, Node clonedParent) {
    NodeList newNodeList = new NodeList();
    for (int i = 0; i < tree.size(); i++) {
      Node node = tree.elementAt(i);
      Node newNode = cloneOnlyNode(node, clonedParent);
      newNodeList.add(newNode);
      if (node.getChildren() != null) {
        newNode.setChildren(deepClone(node.getChildren(), newNode));
      }
    }
    return newNodeList;
  }

  private static Node cloneOnlyNode(Node node, Node clonedParent) {
    Node newNode;
    try {
      newNode = (Node) node.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Node must be cloneable", e);
    }
    node.setParent(clonedParent);
    if (newNode instanceof Tag) {
      Tag newTag = (Tag) newNode;
      newTag.setAttributesEx(cloneAttributes(((Tag) node).getAttributesEx()));
    }
    return newNode;
  }

  private static Vector cloneAttributes(Vector<Attribute> attributes) {
    Vector<Attribute> newAttributes = new Vector<Attribute>(attributes.size());
    for (Attribute a : attributes) {
      newAttributes.add(new Attribute(a.getName(), a.getAssignment(), a.getValue(), a.getQuote()));
    }
    return newAttributes;
  }

}
