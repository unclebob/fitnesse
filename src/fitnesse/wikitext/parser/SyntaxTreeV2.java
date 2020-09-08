package fitnesse.wikitext.parser;

import fitnesse.wiki.PathParser;
import fitnesse.wikitext.SyntaxTree;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

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

  public void parse(String input, ParsingPage parsingPage) {
    this.parsingPage = parsingPage;
    tree = Parser.make(parsingPage, input, symbolProvider).parse();
  }

  public String translateToMarkUp() {
    return new WikiTranslator(parsingPage.getPage()).translateTree(tree);
  }

  public void findWhereUsed(Consumer<String> takeWhere) {
    tree.walkPreOrder(new SymbolTreeWalker() {

      @Override
      public boolean visit(Symbol node) {
        if (node.isType(WikiWord.symbolType)) {
          takeWhere.accept(node.getContent());
        }
        else if (node.isType(Alias.symbolType)) {
          String linkText = node.childAt(1).childAt(0).getContent();
          if (linkText.contains("?")) {
            linkText = linkText.substring(0, linkText.indexOf('?'));
          }
          takeWhere.accept(linkText);
        }
        return true;
      }

      @Override
      public boolean visitChildren(Symbol node) { return true; }
    });
  }

  public void findReferences(Function<String, Optional<String>> changeReference) {
    tree.walkPreOrder(new SymbolTreeWalker() {

      @Override
      public boolean visit(Symbol node) {
        if (node.isType(WikiWord.symbolType)) {
          changeReference.apply(node.getContent()).ifPresent(node::setContent);
        } else if (node.isType(Alias.symbolType)) {
          Symbol wikiWord = node.childAt(1).childAt(0);
          String aliasReference = wikiWord.getContent();
          if (PathParser.isWikiPath(aliasReference)) {
            changeReference.apply(aliasReference).ifPresent(wikiWord::setContent);
          }
        }
        return true;
      }

      @Override
      public boolean visitChildren(Symbol node) {
        return !node.isType(Alias.symbolType);
      }
    });
  }

  @Override
  public String translateToHtml() {
    return new HtmlTranslator(parsingPage.getPage(), this).translateTree(tree);
  }

  @Override
  public Optional<String> findVariable(String name) {
    return parsingPage.findVariable(name);
  }

  @Override
  public void findPaths(Consumer<String> takePath) {
    HtmlTranslator translator = new HtmlTranslator(parsingPage.getPage(), this);
    tree.walkPostOrder(new SymbolTreeWalker() {

      @Override
      public boolean visit(Symbol node) {
        if (node.getType() instanceof PathsProvider) {
          for (String path: (((PathsProvider) node.getType()).providePaths(translator, node))) {
            takePath.accept(path);
          }
        }
        return true;
      }

      @Override
      public boolean visitChildren(Symbol node) { return true; }
    });
  }

  @Override
  public void findXrefs(Consumer<String> takeXref) {
    tree.walkPreOrder(new SymbolTreeWalker() {
      @Override
      public boolean visit(Symbol node) {
        if (node.isType(See.symbolType)) {
          if (node.childAt(0).isType(Alias.symbolType)) {
            takeXref.accept(node.childAt(0).lastChild().childAt(0).getContent());
          } else {
            takeXref.accept(node.childAt(0).getContent());
          }
        }
        return true;
      }

      @Override
      public boolean visitChildren(Symbol node) { return true; }
    });
  }

  private final SymbolProvider symbolProvider;

  private Symbol tree;
  private ParsingPage parsingPage;
}
