describe("WikiFormatter", function () {
    var formatter;

    beforeEach(function () {
        formatter = new WikiFormatter();
    });

    it("testIsNoTableRowIfRowStartsWithOtherCharacter", function () {
        expect(formatter.isTableRow(">!|askj|as|")).toBeNull();
    });

    it("testIsTableRowIfRowStartsWithBangBar", function () {
        expect(formatter.isTableRow("!|askj|as|")).not.toBeNull();
    });

    it("testIsTableRowIfRowStartsWithBar", function () {
        expect(formatter.isTableRow("|askj|as|")).not.toBeNull();
    });

    it("testIsTableRowIfRowStartsWithBarButDoesNotEndWithBar", function () {
        expect(formatter.isTableRow("|askj|as|dd")).not.toBeNull();
    });

    it("testFormat", function () {
        var original = "|cell1|cell2|cell3|\n"
            + "|value1|value2|value3|\n"
            + "|value11|value22|value33|\n";

        var expected = "|cell1  |cell2  |cell3  |\n"
            + "|value1 |value2 |value3 |\n"
            + "|value11|value22|value33|\n";

        expect(formatter.format(original)).toBe(expected);
    });

    it("testFormatOnWikificationPreventedTable", function () {
        var original = "!|cell1|cell2|cell3|\n"
            + "|value11|value22|value33|\n";

        var expected = "!|cell1 |cell2  |cell3  |\n"
            + "|value11|value22|value33|\n";

        expect(formatter.format(original)).toBe(expected);
    });

    it("testFormatOnWikificationPreventedTableWhenFirstColumnInFirstRowIsLongerThanTheOthers", function () {
        var original = "!|cell1|cell2|cell3|\n"
            + "|v1|v2|v3|\n";

        var expected = "!|cell1|cell2|cell3|\n"
            + "|v1    |v2   |v3   |\n";

        expect(formatter.format(original)).toBe(expected);
    });

    it("testFormatTwoTables", function () {
        var original = "|cell1|cell2|cell3|\n"
            + "|value1|value2|value3|\n"
            + "|value11|value22|value33|\n"
            + "\n"
            + "|cell1|cell2|\n"
            + "|value1|value2|\n"
            + "|value11|value22|\n";

        var expected = "|cell1  |cell2  |cell3  |\n"
            + "|value1 |value2 |value3 |\n"
            + "|value11|value22|value33|\n"
            + "\n"
            + "|cell1  |cell2  |\n"
            + "|value1 |value2 |\n"
            + "|value11|value22|\n";

        expect(formatter.format(original)).toBe(expected);
    });

    it("testFormatTableWithColspans", function () {
        var original = "|c1|ce2|cel3|cell4|\n"
            + "|c1|ce2|cel3|\n"
            + "|c1|ce2|\n";

        var expected = "|c1|ce2|cel3|cell4|\n"
            + "|c1|ce2|cel3      |\n"
            + "|c1|ce2           |\n";

        expect(formatter.format(original)).toBe(expected);
    });

    it("testWhenColspanExpandsToFillLineLength", function () {
        var original = "|header|\n"
            + "|c1|ce2|cel3|cell4|\n";

        var expected = "|header           |\n"
            + "|c1|ce2|cel3|cell4|\n";

        expect(formatter.format(original)).toBe(expected);
    });

    it("testWhenColspan3DictatesFullRowColumnLengths", function () {
        var original = "|some long column|\n"
            + "|x|y|z|\n";

        var expected = "|some long column|\n"
            + "|x    |y    |z   |\n";

        expect(formatter.format(original)).toBe(expected);
    });

    it("testWhenColspan2DictatesFullRowColumnLengths", function () {
        var original = "|xy|some long column|\n"
            + "|x|y|z|\n";

        var expected = "|xy|some long column|\n"
            + "|x |y       |z      |\n";

        expect(formatter.format(original)).toBe(expected);
    });

    it("testFormatWithNoTables", function () {
        var original = "!note some wiki text\nwithout tables";
        expect(formatter.format(original)).toBe(original);
    });

    it("testFormatWithXmlWikiMarkup", function () {
        var original = "|xy|a|b|\n"
            + "|x|!<<a>b</a>>!|z|\n";

        var expected = "|xy|a           |b|\n"
            + "|x |!<<a>b</a>>!|z|\n";

        expect(formatter.format(original)).toBe(expected);
    });

    it("testFormatWithMultiLineXmlWikiMarkup", function () {
        var original = "|xy|a|bc|\n"
            + "|x|!<\n"
            + "<a>b</a>\n"
            + ">!|z|\n";

        var expected = "|xy|a|bc|\n"
            + "|x      |!<\n"
            + "<a>b</a>\n"
            + ">!|z|\n";

        expect(formatter.format(original)).toBe(expected);
    });

    it("format table with nested table", function () {
        var original = "| Key | Nested Data |\n"
            + "| Test  | !(| Key2 | Example   |)! |";

        var expected =
            "|Key |Nested Data       |\n"
          + "|Test|!(|Key2|Example|)!|";

        expect(formatter.format(original)).toBe(expected);
    });

    it("format table with nested table", function () {
        var original = "| Key | Nested Data |\n"
            + "| Test  | !(| Key2 | Example   |\n"
            + "|             1    | Number 1  |)! |";

        var expected =
              "|Key |Nested Data          |\n"
            + "|Test|!(|Key2|Example|\n"
            + "|1   |Number 1|)!          |";

        expect(formatter.format(original)).toBe(expected);
    });
});
