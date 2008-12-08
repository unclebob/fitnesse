TableSorter = function(id, dateParser)
{
	this.id = id;
	this.table = document.getElementById(id);
	this.activeSort = null;
	this.dateParser = dateParser;

	this.getSortDirection = function(sortCol)
	{
		if (this.activeSort == null || this.activeSort.column != sortCol)
			this.activeSort = new SortReference(sortCol, "asc");
		else
		{
			if (this.activeSort.direction == "asc")
				this.activeSort.direction = "dsc";
			else
				this.activeSort.direction = "asc";
		}
		return this.activeSort.direction;
	}

	this.sort = function(sortCol, datatype)
	{
		var rowDataArray = new TableRowDataArray(this.id);
		rowDataArray.setSortCol(sortCol);
		var direction = this.getSortDirection(sortCol);
		if (datatype == 'number')
			rowDataArray.sort(new NumericComparator());
		else if (datatype == 'date')
			rowDataArray.sort(new DateComparator(this.dateParser));
		else
			rowDataArray.sort();
		if (direction == "dsc")
			rowDataArray.reverse();
		rowDataArray.rebuildTable();
	}
	return this;
}

TableRowData = function(cols)
{
	this.cols = cols;
	this.sortCol = 0;
	this.setSortCol = function(col)
	{
		this.sortCol = col;
	}
	this.toString = function()
	{
		return this.cols[this.sortCol];
	}
}

TableRowDataArray = function(id)
{
	this.arr = new Array();
	this.arr.table = document.getElementById(id);
	this.arr.cssClasses = new Array();

	var tbody = this.arr.table.tBodies[0];
	var rows = tbody.rows;
	for (var row = 0; row < rows.length; row++)
	{
		this.arr.cssClasses.push(rows[row].className);
		var cells = rows[row].cells;
		var cellTextArray = new Array();
		for (var j = 0; j < cells.length; j++)
			cellTextArray.push(cells[j].innerHTML);
		this.arr.push(new TableRowData(cellTextArray));
	}

	this.arr.rebuildTable = function()
	{
		var tbody = this.table.tBodies[0];
		var mybody = tbody.cloneNode(false);
		for (var row = 0; row < this.length; row++)
		{
			var tr = document.createElement("tr");
			tr.className = this.cssClasses[row];
			for (var i = 0; i < this[row].cols.length; i++)
			{
				var td = document.createElement("td");
				td.innerHTML = this[row].cols[i];
				tr.appendChild(td);
			}
			mybody.appendChild(tr);
		}
		this.table.replaceChild(mybody, tbody);
	}

	this.arr.setSortCol = function(sortCol)
	{
		for(var i = 0; i < this.length; i++)
		{
			this[i].setSortCol(sortCol);
		}
	}
	return this.arr;
}

SortReference = function (column, direction)
{
	this.column = column;
	this.direction = direction;
}


NumericComparator = function()
{
	function compare(a,b)
	{
		a = parseInt(a);
		b = parseInt(b);
		if (a > b)
			return 1;
		if (a < b)
			return -1;
		return 0;
	}
	return compare;
}

DateComparator = function(dateParser)
{
	this.compare = function(x,y)
	{
		x = dateParser.parse(x).getTime();
		y = dateParser.parse(y).getTime();
		if (x > y)
			return 1;
		if (x < y)
			return -1;
		return 0;
	}
	return this.compare;
}

DateParser = function(re, a, b, c, d, e, f)
{
	this.re = re;
	this.a = a;
	this.b = b;
	this.c = c;
	this.d = d || -1;
	this.e = e || -1;
	this.f = f || -1;
	this.parseMonth = function(month)
	{
		month = month.toLowerCase();
		months = "jan,feb,mar,apr,may,jun,jul,aug,sep,oct,nov,dec".split(',');
		for (var i = 0; i < months.length; i++)
		{
			if (month == months[i])
			{
				return i;
			 }
		}
	}

	this.parse = function(dateStr)
	{
		value = dateStr.toString().toLowerCase();
		if (this.re.test(value))
		{
			matches = this.re.exec(value);
			year = parseInt(matches[a]);
			if (parseInt(matches[b]))
				month = parseInt(matches[b] - 1);
			else
				month = this.parseMonth(matches[b]);
			day = parseInt(matches[c]);
			hour = d > 0 ? parseInt(matches[d]) : 0;
			minute = e > 0 ? parseInt(matches[e]) : 0;
			second = f > 0 ? parseInt(matches[f]) : 0;
			date = new Date(year,month,day,hour,minute,second);
			return date;
		}
	}

	return this;
}
