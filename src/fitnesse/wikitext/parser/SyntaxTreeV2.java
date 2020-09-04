package fitnesse.wikitext.parser;

import fitnesse.wikitext.SyntaxTree;

import java.util.ArrayList;
import java.util.List;

public class SyntaxTreeV2 implements SyntaxTree {
  public SyntaxTreeV2() {
    this(SymbolProvider.wikiParsingProvider);
  }

  public SyntaxTreeV2(SymbolProvider symbolProvider) {
    this.symbolProvider = symbolProvider;
    tree = Symbol.emptySymbol;
  }

  public ParsingPage getParsingPage() { return parsingPage; }
  public Symbol getSyntaxTree() { return tree; }

  @Override
  public void parse(String input, ParsingPage parsingPage) {
    this.parsingPage = parsingPage;
    tree = Parser.make(parsingPage, input, symbolProvider).parse();
  }

  @Override
  public String getHtml() {
    return new HtmlTranslator(parsingPage.getPage(), this).translateTree(tree);
  }

  @Override
  public Maybe<String> findVariable(String name) {
    return parsingPage.findVariable(name);
  }

  @Override
  public List<String> findPaths() {
    List<String> result = new ArrayList<>();
    HtmlTranslator translator = new HtmlTranslator(parsingPage.getPage(), this);
    tree.walkPostOrder(new SymbolTreeWalker() {

      @Override
      public boolean visit(Symbol node) {
        if (node.getType() instanceof PathsProvider) {
          result.addAll(((PathsProvider) node.getType()).providePaths(translator, node));
        }
        return true;
      }

      @Override
      public boolean visitChildren(Symbol node) {
        return true;
      }
    });
    return result;
  }

  public List<String> findXrefs() {
    List<String> xrefPages = new ArrayList<>();
    tree.walkPreOrder(new SymbolTreeWalker() {
      @Override
      public boolean visit(Symbol node) {
        if (node.isType(See.symbolType)) {
          if (node.childAt(0).isType(Alias.symbolType)) {
            xrefPages.add(node.childAt(0).lastChild().childAt(0).getContent());
          } else {
            xrefPages.add(node.childAt(0).getContent());
          }
        }
        return true;
      }

      @Override
      public boolean visitChildren(Symbol node) {
        return true;
      }
    });
    return xrefPages;
  }

  private final SymbolProvider symbolProvider;

  private Symbol tree;
  private ParsingPage parsingPage;
}
