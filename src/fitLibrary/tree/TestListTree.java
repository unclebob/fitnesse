/*
 * @author Rick Mugridge on Jan 4, 2005
 *
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.tree;

import junit.framework.TestCase;

/**
 *
 */
public class TestListTree extends TestCase {
    private Tree topTree;
    private Tree tree;
    private Tree toplessTree;
    
    public void setUp() {
        topTree = new ListTree("top");
        tree = new ListTree("tree", new Tree[] {
                new ListTree("a"),
                new ListTree("b", new Tree[] {new ListTree("c")})
                });
        toplessTree = new ListTree("", new Tree[] {
                new ListTree("a"),
                new ListTree("b") });
    }
    public void testEqualsSameOne() {
        treesEqual(topTree,topTree);
    }
    public void testEqualsSimilarOne() {
        treesEqual(topTree, new ListTree("top"));
    }
    public void testNotEqualsSimilarOne() {
        treesUnEqual(topTree,new ListTree("bottom"));
    }
    public void testTopToString() {
        assertEquals("top",topTree.toString());
    }
    public void testToplessTreeToString() {
        assertEquals("<ul><li>a</li><li>b</li></ul>",
                toplessTree.toString());
    }
    
    public void testEqualsSameTree() {
        treesEqual(tree,tree);
    }
    public void testEqualsSimilarTree() {
        Tree tree2 = new ListTree("tree", new Tree[] {
                new ListTree("a"),
                new ListTree("b", new Tree[] {new ListTree("c")})
                });
        treesEqual(tree,tree2);
    }
    public void testNotEqualsTop() {
        treesUnEqual(tree,topTree);
        treesUnEqual(topTree,toplessTree);
}
    public void testNotEqualsSimilarShapedTree() {
        Tree tree2 = new ListTree("tree", new Tree[] {
                new ListTree("a"),
                new ListTree("b", new Tree[] {new ListTree("C")})
                });
        treesUnEqual(tree,tree2);
    }
    public void testNotEqualsDifferentShapedTree() {
        Tree tree2 = new ListTree("tree", new Tree[] {
                new ListTree("a", new Tree[] {new ListTree("c")}),
                new ListTree("b")
                });
        treesUnEqual(tree,tree2);
        treesUnEqual(tree,toplessTree);
    }
    public void testTreeToString() {
        assertEquals("tree<ul><li>a</li><li>b<ul><li>c</li></ul></li></ul>",
                tree.toString());
    }
    public void testParseTop() {
        assertEquals(topTree,ListTree.parse("top"));
    }
    public void testParseTree1() {
        assertEquals("tree<ul><li>a</li></ul>",ListTree.parse(
                "tree<ul><li>a</li></ul>").toString());
    }
    public void testParseTree() {
        assertParsed("tree<ul><li>a</li><li>b<ul><li>c</li></ul></li></ul>");
    }
    public void testParseToplessTree() {
        assertParsed("<ul><li>a</li><li>b<ul><li>c</li></ul></li></ul>");
    }
    public void testParseTags0() {
        assertParsed("<i>a</i>");
    }
    public void testParseSpace() {
        assertEquals("a",ListTree.parse("<i>a  </i>").text());
        }
    public void testParseTags1() {
        assertParsed("tree<ul><li><i>a</i></li></ul>");
    }
    public void testParseTags2() {
        assertParsed("tree<ul><li>a<i>b</i><b>c</b></li></ul>");
    }
    public void testEqualsSimilarWithTags() {
        treesEqual(topTree,new ListTree("<i>top</i>"));
    }
    public void testTopText() {
        assertEquals("top",new ListTree("top").text());
    }
    public void testToplessTreeText() {
        assertEquals("<ul><li>a</li><li>b</li></ul>",
                toplessTree.text());
    }
    public void testTopTextWithTags() {
        assertEquals("top",new ListTree("<i><b>top</b></i>").text());
    }
    public void testTreeText() {
        assertEquals("tree<ul><li>a</li><li>b<ul><li>c</li></ul></li></ul>",
                tree.text());
    }
    public void testTreeTextWithTags() {
        String s = "tree<ul><li>a<i>b</i><b>c</b></li></ul>";
        assertEquals("tree<ul><li>abc</li></ul>",ListTree.parse(s).text());
    }
    public void testParseTreeNoCloseLi() {
        try {
            ListTree.parse("tree<ul><li>a<li>b<ul><li>c</ul></ul>");
            fail("Doesn't handle lists with </li> missing.");
        }
        catch (RuntimeException e) {
        }
    }
    private void assertParsed(String s) {
        assertEquals(s,ListTree.parse(s).toString());
    }
    private void treesEqual(Tree t1, Tree t2) {
        assertTrue(t1.equals(t2));
        assertTrue(t2.equals(t1));
        assertTrue(ListTree.equals(t1,t2));
        assertTrue(ListTree.equals(t2,t1));
    }
    private void treesUnEqual(Tree t1, Tree t2) {
        assertFalse(t1.equals(t2));
        assertFalse(t2.equals(t1));
        assertFalse(ListTree.equals(t1,t2));
        assertFalse(ListTree.equals(t2,t1));
    }
}
