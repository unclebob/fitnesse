package fitnesse.util;

import java.util.Vector;

import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;

/**
 * HtmlParserUtils -- all sorts of trickiness not implemented in the html-parser library
 */
public final class HtmlParserTools {

  private HtmlParserTools() {

  }

  /**
   * Make flat clone of just this one node. No nesting
   *
   * @param node Node to clone
   * @return cloned version of node
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
   * Make a 1:1 clone of a list of Nodes
   *
   * @param nodeList NodeList to clone
   * @return cloned version of NodeList
   */
  public static NodeList deepClone(NodeList nodeList) {
    return deepClone(nodeList, null);
  }

  /**
   * Make a 1:1 clone of the Node.
   *
   * @param node Node to deepclone
   * @param <T> Node of child of Node
   * @return deepcloned version of node
   */
  @SuppressWarnings("unchecked")
  public static <T extends Node> T deepClone(T node) {
    return (T) deepClone(new NodeList(node), null).elementAt(0);
  }

  /**
   * Get closing node for this node, if any.
   * @param node Node to find closing sibling for
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
      throw new IllegalStateException("Node must be cloneable", e);
    }
    node.setParent(clonedParent);
    if (newNode instanceof Tag) {
      Tag newTag = (Tag) newNode;
      newTag.setAttributesEx(cloneAttributes(((Tag) node).getAttributesEx()));
    }
    return newNode;
  }

  private static Vector cloneAttributes(Vector<Attribute> attributes) {
    Vector<Attribute> newAttributes = new Vector<>(attributes.size());
    for (Attribute a : attributes) {
      newAttributes.add(new Attribute(a.getName(), a.getAssignment(), a.getValue(), a.getQuote()));
    }
    return newAttributes;
  }

  public static boolean nodeHasClass(Node node, String classToCheck) {
    if (!(node instanceof TagNode)) {
      return false;
    }
    String classAttribute = ((TagNode) node).getAttribute("class");
    if (null == classAttribute) {
      return false;
    }
    for (String className : classAttribute.split(" ")) {
      if (classToCheck.equals(className)) {
        return true;
      }
    }
    return false;
  }

}
