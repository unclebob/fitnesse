package fitnesse.slimTables;

import static org.junit.Assert.*;

import org.junit.Test;

public class RowInstructionSelectorTest {

    RowInstructionSelector selector = new RowInstructionSelector();

    @Test
    public void shouldBeStartInstruction() {

        assertTrue(selector.isInstruction(RowInstructionVariable.SYNONYM_START, "start"));
        assertTrue(selector.isInstruction(RowInstructionVariable.SYNONYM_START, "sTart"));
        assertTrue(selector.isInstruction(RowInstructionVariable.SYNONYM_START, "START"));

    }

    @Test
    public void shouldBeStartInstructionSynonym() {

        selector.setSynonym(RowInstructionVariable.SYNONYM_START, "börjar");
        assertTrue(selector.isInstruction(RowInstructionVariable.SYNONYM_START, "börJar"));
        assertTrue(selector.isInstruction(RowInstructionVariable.SYNONYM_START, "börjAr"));
       

    }

}
