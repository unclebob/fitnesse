# Known limitations

## Codeblocks 

In Fitnesse, it is allowed to do this:

	before{{{import fit.ColumnFixture;

	public class AddRemovePlayerFixture extends ColumnFixture {
	  public String playerName;
	  
	} }}}after
	
Both the 'before' and the 'after' text will be rendered as normal text.

In wysiwyg editor, the {{{ and }}} need to be 'alone' on the line, as such:

	before
	{{{
	import fit.ColumnFixture;

	public class AddRemovePlayerFixture extends ColumnFixture {
	  public String playerName;
	} 
	}}}
	after