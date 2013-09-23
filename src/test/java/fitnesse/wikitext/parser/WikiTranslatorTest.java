package fitnesse.wikitext.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WikiTranslatorTest {
    @Test
    public void DefineVariableIsPreserved() throws Exception {
        String content = "${something} or other";
        String newContent = ParserTestHelper.roundTrip(new TestSourcePage(), content);
        assertEquals(content, newContent);
    }

    @Test
    public void DefineVariableInLinkIsPreserved() throws Exception {
        String content = "http://localhost/${somepath}/something";
        String newContent = ParserTestHelper.roundTrip(new TestSourcePage(), content);
        assertEquals(content, newContent);
    }

    @Test
    public void DefineVariableInAliasIsPreserved() throws Exception {
        String content = "[[${a}][${b}]]";
        String newContent = ParserTestHelper.roundTrip(new TestSourcePage(), content);
        assertEquals(content, newContent);
    }
}
