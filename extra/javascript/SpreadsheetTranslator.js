// Copyright (C) 2004 by Alain Bienvenue. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.

function SpreadsheetTranslator()
{
    this.currentLine;
    this.rows;
    this.fitNesseTables;
    
    this.isImplicitTableStart =  function(index)
    {
        if (this.rows[index][0].match("^[A-Za-z][0-9A-Za-z]*\\.[A-Za-z][0-9A-Za-z]*"))
        {
        	return true;
        }
        var i;
        for (i = index; i < this.rows.length; i++)
        {
	        if ((this.rows[i][0] == '!') || this.lineSize(this.rows[i]) == 0)
	        {
	        	return false;
	        }
        	if (this.lineSize(this.rows[i]) > 1)
        	{
        		return true;
        	}
        }
        return false;
    }
    
    this.isExplicitTableStart =  function(rows,index)
    {
        return (rows[index][0] == "!");
    }
    
    this.isNotTableLine = function(currentLine,columnsToSkip)
    {
    	var row = this.rows[currentLine];
        if (this.lineSize(row) == 0)
        {
        	return true;
        }

        var i;
        for (i = 0; i < columnsToSkip; i++)
	    {	
    		if (row[i] != '')
    		{
    	    	return true;
        	}
	    }
	    return false;
    }
    
    this.lineSize = function(row)
    {
        var x;
        for (x = row.length - 1; x >= 0; x--)
        {
            if (row[x] != '') return x + 1;
        }
        return 0;
    }
    
    this.parseExcelTable = function(excelTable)
    {
        var table = this.removeCarriageReturns(excelTable);
        var lines = table.split("\n");
        this.rows = new Array;
        for(i = 0; i < lines.length; i++)
        {
            this.rows[i] = lines[i].split("\t");
        }
    }
    
    
    this.removeCarriageReturns = function(str)
    {
         return str.replace(/\r\n/g,'\n').replace(/\r/g, '\n');
    }
    
    this.getFitNesseTables = function()
    {
        this.currentLine = 0;
        this.fitNesseTables = new String();
        while (this.currentLine < this.rows.length)
        {
            if (this.isExplicitTableStart(this.rows,this.currentLine))
            {
                this.processTable(1);
            }
            else if (this.isImplicitTableStart(this.currentLine))
            {
            	if (this.rows[this.currentLine][0] == '')
            	{
	                this.processTable(1);
            	}
            	else
            	{
	                this.processTable(0);
	            }
            }
            else
            {
                this.fitNesseTables += "\n" + this.rows[this.currentLine][0];
            }
            this.currentLine++;
        }
        return this.fitNesseTables.substring(1);
    }
    

    this.processTable = function(columnsToSkip)
    {
        var tableFirstLine = this.currentLine;
        var tableSize;
        
        while (this.currentLine < this.rows.length)
        {
            var row = this.rows[this.currentLine];
            if (this.currentLine > tableFirstLine && this.isNotTableLine(this.currentLine,columnsToSkip))
            {
            	this.currentLine--;
                return;
            }
                        
            this.fitNesseTables += "\n";
            var lineSize = 0;
            if (this.currentLine == tableFirstLine)
            {
                this.fitNesseTables += "!";
                lineSize = this.lineSize(row);
            }
            else if (this.currentLine == tableFirstLine + 1)
            {
                lineSize = this.lineSize(row);
                tableSize = lineSize;
            }
            else
            {
                lineSize = Math.max(tableSize,this.lineSize(row));
                lineSize = Math.min(lineSize,row.length);
            }
            
            this.fitNesseTables += "|";
            var j;
            for(j = columnsToSkip; j < lineSize; j++)
            {
                this.fitNesseTables += row[j] + "|";
            }
            this.currentLine++;
        }
    }
}
