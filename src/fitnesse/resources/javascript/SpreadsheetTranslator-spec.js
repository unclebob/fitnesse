describe("SpreadsheetTranslator", function () {

    beforeEach(function () {
        translator = new SpreadsheetTranslator();
    });

    it("testLineSizeNoCells", function () {
        var line = new Array;
        expect(translator.lineSize(line)).toBe(0);
    });

    it("testLineSize0", function() {
        var line = new Array;
        line[0] = '';
        line[1] = '';
        expect(translator.lineSize(line)).toBe(0);
    });

    it("testLineSize1", function() {
        var line = new Array;
        line[0] = 'x';
        line[1] = '';
        expect(translator.lineSize(line)).toBe(1);
    });

    it("testLineSize2", function() {
        var line = new Array;
        line[0] = '';
        line[1] = 'x';
        line[2] = '';
        expect(translator.lineSize(line)).toBe(2);
    });

    it("testLineSize3", function () {
        var line = new Array;
        line[0] = '';
        line[1] = 'x';
        line[2] = 'x';
        expect(translator.lineSize(line)).toBe(3);
    });

    it("testRemoveCarriageReturns", function () {
        var before = "1\r\n2\r\n";
        var after = translator.removeCarriageReturns(before);
        expect(after).toBe("1\n2\n");
    });


    function verifySpreadsheetToFitnesse (input, expected) {
        translator.parseExcelTable(input);
        var output = translator.getFitNesseTables();
        expect(output).toBe(expected);

        input = output;
        translator.parseExcelTable(input);
        output = translator.getFitNesseTables();
        expect(output).toBe(expected);
    }

    it("testSetupFixture", function() {
        var input = "fit.Setup";
        var expected = "|fit.Setup|";

        verifySpreadsheetToFitnesse(input, expected);
    });


    it("testSetupFixtureAndComments", function() {
        var input = "\Prefix comment\r\n" +
            "\r\n" +
            "package.Class\r\n" +
            "\r\n" +
            "Suffix Comment\r\n";
        var expected = "Prefix comment\n" +
            "\n" +
            "|package.Class|\n" +
            "\n" +
            "Suffix Comment\n";

        verifySpreadsheetToFitnesse(input, expected);
    });

    it("testExplicitTableAndComments", function() {
        var input = "\Prefix comment\r\n" +
            "!\tpackage.Class\r\n" +
            "Suffix Comment\r\n";
        var expected = "Prefix comment\n" +
            "!|package.Class|\n" +
            "Suffix Comment\n";

        verifySpreadsheetToFitnesse(input, expected);
    });

    it("testMultipleColumnsTable", function() {
        var input = "fit.OneTest\t\t\r\n" +
            "input1\tinput2\toutput()\r\n" +
            "1\t2\t3\r\n" +
            "1\t2\t";
        var expected = "|fit.OneTest|\n" +
            "|input1|input2|output()|\n" +
            "|1|2|3|\n" +
            "|1|2||";

        verifySpreadsheetToFitnesse(input, expected);
    });

    it("testExplicitTable", function() {
        var input = "\r\n!\tfit.OneTest\t\t\r\n" +
            "\tinput1\tinput2\toutput()\r\n" +
            "\t1\t2\t3\r\n" +
            "\t1\t2\t";
        var expected = "\n!|fit.OneTest|\n" +
            "|input1|input2|output()|\n" +
            "|1|2|3|\n" +
            "|1|2||";

        verifySpreadsheetToFitnesse(input, expected);
    });

    it("testActionFixtureTable", function() {
        var input = "\r\n!\tfit.ActionFixture\t\t\r\n" +
            "\tstart\tpackage.MyFixture\t\r\n" +
            "\tpress\tstart\t\r\n" +
            "\tcheck\tstarted\ttrue\r\n";
        var expected = "\n!|fit.ActionFixture|\n" +
            "|start|package.MyFixture|\n" +
            "|press|start|\n" +
            "|check|started|true|\n";

        verifySpreadsheetToFitnesse(input, expected);
    });

    it("testPlainTable", function() {
        var input = "1\t2\t3\r\n" +
            "1\t2\t";
        var expected = "|1|2|3|\n" +
            "|1|2|";

        verifySpreadsheetToFitnesse(input, expected);
    });

// this tests the case where a user converts wiki text to spreadsheet,
// and back to wiki. In this case, it is possible that not all lines contain
// as many cells as the longest line.
    it("testConvertBackUnevenTable", function() {
        var input = "!\t1\t2\t3\r\n" +
            "\t1\t2\t3\r\n" +
            "\t1" ;
        var expected = "!|1|2|3|\n" +
            "|1|2|3|\n" +
            "|1|";

        verifySpreadsheetToFitnesse(input, expected);
    });

    it("testGracefulNamesTable", function() {
        var input = "One Test\t\r\n" +
            "input1\toutput()\r\n" +
            "1\t3";
        var expected = "|One Test|\n" +
            "|input1|output()|\n" +
            "|1|3|";

        verifySpreadsheetToFitnesse(input, expected);
    });

    it("testExplicitOneColumnnTable", function() {
        var input = "!\tAdd Rows\t\t\r\n" +
            "\tone\t\t\r\n" +
            "\ttwo\t\t\r\n";
        var expected = "!|Add Rows|\n" +
            "|one|\n" +
            "|two|\n";

        verifySpreadsheetToFitnesse(input, expected);
    });

// a test table is not escaped and translated to spreadsheet format
// the first column will be empty for all lines.
    it("testNotEscapedWikiSpreadsheetTable", function() {
        var input = "\tfit.OneTest\t\t\r\n" +
            "\tinput1\tinput2\toutput()\r\n" +
            "\t1\t2\t3\r\n" +
            "\t1\t2\t";
        var expected = "|fit.OneTest|\n" +
            "|input1|input2|output()|\n" +
            "|1|2|3|\n" +
            "|1|2||";

        verifySpreadsheetToFitnesse(input, expected);
    });

    it("testHiddenOneColumnnTable", function() {
        var input = "-\tAdd Rows\t\t\r\n" +
            "\tone\t\t\r\n" +
            "\ttwo\t\t\r\n";
        var expected = "-|Add Rows|\n" +
            "|one|\n" +
            "|two|\n";

        verifySpreadsheetToFitnesse(input, expected);
    });

    it("testHiddenExplicitOneColumnnTable", function() {
        var input = "-!\tSome Rows\t\t\r\n" +
            "\tx\t\t\r\n" +
            "\ty\t\t\r\n";
        var expected = "-!|Some Rows|\n" +
            "|x|\n" +
            "|y|\n";

        verifySpreadsheetToFitnesse(input, expected);
    });

});