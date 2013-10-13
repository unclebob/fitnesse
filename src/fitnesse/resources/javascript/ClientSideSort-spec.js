describe("ClientSideSort", function () {
    beforeEach(function () {
      createTable('tableData');
      sorter = new TableSorter('tableData', new DateParser(getRegex(), 8, 2, 3, 4, 5, 6));
    });

    afterEach(function () {
      document.body.removeChild(document.getElementById('tableData'));
    });

    function getRegex () {
      return /^(\w+) (jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec) (\d+) (\d+).(\d+).(\d+) (\w+) (\d+)$/;
    }

    it("testCorrectTable", function () {
      expect(sorter.table).toBe(table);
    });

    it("testGetSortDirection", function () {
      expect(sorter.getSortDirection(0)).toBe("asc");
      expect(sorter.getSortDirection(0)).toBe("dsc");
      expect(sorter.getSortDirection(0)).toBe("asc");
      expect(sorter.getSortDirection(1)).toBe("asc");
    });

    it("testParseMonth", function () {
      parser = new DateParser();
      expect(parser.parseMonth('Jan')).toBe(0);
      expect(parser.parseMonth('jan')).toBe(0);
      expect(parser.parseMonth('May')).toBe(4);
      expect(parser.parseMonth('may')).toBe(4);
      expect(parser.parseMonth('Dec')).toBe(11);
      expect(parser.parseMonth('dec')).toBe(11);
    });

    it("testNumericComparator", function () {
      a = 2;
      b = 12;
      comparator = new NumericComparator();
      expect(comparator(a, a)).toBe(0);
      expect(comparator(b, b)).toBe(0);
      expect(comparator(a, b)).toBe(-1);
      expect(comparator(b, a)).toBe(1);
    });

    it("testSortReference", function () {
      a = new Object();
      b = new Object();
      ref = new SortReference(a, b);
      expect(ref.column).toBe(a);
      expect(ref.direction).toBe(b);
    });

    it("testDateComparator", function () {
      a = 'Thu Mar 31 14:44:55 CST 2005';
      b = 'Fri Apr 15 22:55:47 CST 2005';
      comparator = new DateComparator(new DateParser(getRegex(), 8, 2, 3, 4, 5, 6));
      expect(comparator(a, a)).toBe(0);
      expect(comparator(b, b)).toBe(0);
      expect(comparator(a, b)).toBe(-1);
      expect(comparator(b, a)).toBe(1);
    });

    it("testDateParser", function () {
      parser = new DateParser(getRegex(), 8, 2, 3, 4, 5, 6);
      date = new Date(2005, 3, 5, 22, 55, 47);
      expect(parser.parse('Tue Apr 05 22:55:47 CDT 2005').getTime()).toBe(date.getTime());
    });

    it("testDateParserOtherRegex", function () {
      regex = /^(\d+)\/(\d+)\/(\d+)$/;
      parser = new DateParser(regex, 3, 1, 2);
      date = new Date(1963, 10, 26);
      expect(parser.parse('11/26/1963').getTime()).toBe(date.getTime());
    });

    it("testDataRowDefault", function () {
      tableRowData = new TableRowData("a,b,c".split(','));
      expect(tableRowData.toString()).toBe("a");
    });

    it("testDataRowSetSortCol", function () {
      tableRowData = new TableRowData("a,b,c".split(','));
      tableRowData.setSortCol(1);
      expect(tableRowData.toString()).toBe("b");
    });

    it("testDataRowArray", function () {
      setText('1,1', 0, 0);
      setText('1,2', 0, 1);
      setText('2,1', 1, 0);
      setText('2,2', 1, 1);
      setText('3,1', 2, 0);
      setText('3,2', 2, 1);
      tableRowDataArray = new TableRowDataArray('tableData');
      expect(tableRowDataArray.length).toBe(3);
      expect(tableRowDataArray[0].cols.length).toBe(2);
      expect(tableRowDataArray[1].cols.length).toBe(2);
      expect(tableRowDataArray[2].cols.length).toBe(2);
      expect(tableRowDataArray[0].cols[0]).toBe('1,1');
      expect(tableRowDataArray[0].cols[1]).toBe('1,2');
      expect(tableRowDataArray[1].cols[0]).toBe('2,1');
      expect(tableRowDataArray[1].cols[1]).toBe('2,2');
      expect(tableRowDataArray[2].cols[0]).toBe('3,1');
      expect(tableRowDataArray[2].cols[1]).toBe('3,2');
      expect(getCssClass(0)).toBe('row1');
      expect(getCssClass(1)).toBe('row2');
      expect(getCssClass(2)).toBe('row3');
    });

    it("testRebuildTableAfterSort", function () {
      setText('b', 0, 0);
      setText('b2', 0, 1);
      setText('c', 1, 0);
      setText('c2', 1, 1);
      setText('a', 2, 0);
      setText('a2', 2, 1);
      tableRowDataArray = new TableRowDataArray('tableData');
      tableRowDataArray.sort();
      tableRowDataArray.rebuildTable();
      expect(getText(0, 0)).toBe('a');
      expect(getText(0, 1)).toBe('a2');
      expect(getText(1, 0)).toBe('b');
      expect(getText(1, 1)).toBe('b2');
      expect(getText(2, 0)).toBe('c');
      expect(getText(2, 1)).toBe('c2');
    });

    it("testDateSort", function () {
      date1 = 'Thu Mar 31 14:44:55 CST 2005';
      date2 = 'Wed Apr 06 22:36:46 CDT 2005';
      date3 = 'Thu Apr 07 21:35:35 CDT 2005';

      setText(date2, 0, 0);
      setText(date3, 1, 0);
      setText(date1, 2, 0);
      sorter.sort(0, 'date');
      expect(getText(0, 0)).toBe(date1);
      expect(getText(1, 0)).toBe(date2);
      expect(getText(2, 0)).toBe(date3);

      expect(getCssClass(0)).toBe('row1');
      expect(getCssClass(1)).toBe('row2');
      expect(getCssClass(2)).toBe('row3');
    });

    function setText(text, row, col)
    {
      table = document.getElementById('tableData');
      table.tBodies[0].rows[row].cells[col].innerHTML = text;
    }

    function getText(row, col)
    {
      table = document.getElementById('tableData');
      return table.tBodies[0].rows[row].cells[col].innerHTML;
    }

    function getCssClass(row)
    {
      table = document.getElementById('tableData');
      return table.tBodies[0].rows[row].className;
    }

    function createTable(id)
    {
      table = document.createElement('table');
      table.setAttribute('id', id);
      tbody = document.createElement('tbody');

      tr1 = document.createElement('tr');
      tr1.className = 'row1';
      tr2 = document.createElement('tr');
      tr2.className = 'row2';
      tr3 = document.createElement('tr');
      tr3.className = 'row3';

      td1 = document.createElement('td');
      td2 = document.createElement('td');
      td3 = document.createElement('td');
      td4 = document.createElement('td');
      td5 = document.createElement('td');
      td6 = document.createElement('td');

      table.appendChild(tbody);
      tbody.appendChild(tr1);
      tbody.appendChild(tr2);
      tbody.appendChild(tr3);
      tr1.appendChild(td1);
      tr1.appendChild(td2);
      tr2.appendChild(td3);
      tr2.appendChild(td4);
      tr3.appendChild(td5);
      tr3.appendChild(td6);

      document.body.appendChild(table);
    }
});
