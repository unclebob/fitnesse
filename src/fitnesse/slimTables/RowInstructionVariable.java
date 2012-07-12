package fitnesse.slimTables;

public enum RowInstructionVariable {
    SYNONYM_START("start"), SYNONYM_CHECK("check"), SYNONYM_CHECK_NOT("check not"), SYNONYM_ENSURE("ensure"), SYNONYM_REJECT("reject"), SYNONYM_NOTE(
        "note"), SYNONYM_SHOW("show");
    public final String originalValue;
    private RowInstructionVariable(final String originalValue) {
        this.originalValue = originalValue; 
    }
    

}
