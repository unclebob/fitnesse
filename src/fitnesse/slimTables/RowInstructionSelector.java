package fitnesse.slimTables;

import java.util.HashMap;
import java.util.Map;


public class RowInstructionSelector {
    Map<RowInstructionVariable, String> synonyms = new HashMap<RowInstructionVariable,String>();
    
    public boolean isInstruction(RowInstructionVariable instruction, String cell) {
        if (cell.equalsIgnoreCase(instruction.originalValue)) {
            return true;
        }
        String synonymForInstruction = synonyms.get(instruction);
        if (synonymForInstruction == null) {
            return false;
        }
        return synonymForInstruction.equalsIgnoreCase(cell);
            
        
    }
    
    public void setSynonym(RowInstructionVariable original, String synonym) {
        if (synonym == null) {
            return;
        }
     
        
        synonyms.put(original, synonym);
        
    }

}
