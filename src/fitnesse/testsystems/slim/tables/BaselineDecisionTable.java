package fitnesse.testsystems.slim.tables;

import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;

public class BaselineDecisionTable extends DecisionTable {

	public BaselineDecisionTable(Table table, String id, SlimTestContext context) {
		super(table, id, context);
		setBaselineDecisionTable(true);
	}
}
