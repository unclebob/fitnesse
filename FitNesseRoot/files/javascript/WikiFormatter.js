function WikiFormatter()
{
  /*
   * This is the entry point, it takes a chunk of text, splits it into lines, loops
   * through the lines collecting consecutive lines that are part of a table, and returns
   * a chunk of text with those tables it collected formatted.
   */
  this.format = function(wikiText) {
    this.wikificationPrevention = false;
    
    var formatted = "";
    var currentTable = [];
    var lines = wikiText.split("\n");
    var line = null;

    for(var i = 0, j = lines.length; i < j; i++) {
      line = lines[i];
      
      if(this.isTableRow(line)) {
        currentTable.push(line);
      }
      else {
        formatted += this.formatTable(currentTable);
        currentTable = [];        
        formatted += line + "\n";
      }
    }

    formatted += this.formatTable(currentTable);
    return formatted.slice(0, formatted.length - 1);
  }

  /*
   * This function receives an array of strings(rows), it splits each of those strings
   * into an array of strings(columns), calls off to calculate what the widths
   * of each of those columns should be and then returns a string with each column
   * right/space padded based on the calculated widths.
   */
  this.formatTable = function(table) {
    var formatted = "";
    var rows = this.splitRows(table);
    var widths = this.calculateColumnWidths(rows);
    var row = null;
  
    for(var rowIndex = 0, numberOfRows = rows.length; rowIndex < numberOfRows; rowIndex++) {
      row = rows[rowIndex];
      formatted += "|";

      for(var columnIndex = 0, numberOfColumns = row.length; columnIndex < numberOfColumns; columnIndex++) {
        formatted += this.rightPad(row[columnIndex], widths[rowIndex][columnIndex]) + "|";
      }

      formatted += "\n";
    }

    if(this.wikificationPrevention) {
      formatted = '!|' + formatted.substr(2);
      this.wikificationPrevention = false;
    }

    return formatted;
  }

  /* 
   * This is where the nastiness starts due to trying to emulate
   * the html rendering of colspans.
   *   - make a row/column matrix that contains data lengths
   *   - find the max widths of those columns that don't have colspans
   *   - update the matrix to set each non colspan column to those max widths
   *   - find the max widths of the colspan columns
   *   - increase the non colspan columns if the colspan columns lengths are greater
   *   - adjust colspan columns to pad out to the max length of the row
   *
   * Feel free to refator as necessary for clarity
   */
  this.calculateColumnWidths = function(rows) {
    var widths = this.getRealColumnWidths(rows);
    var totalNumberOfColumns = this.getNumberOfColumns(rows);

    var maxWidths = this.getMaxWidths(widths, totalNumberOfColumns);    
    this.setMaxWidthsOnNonColspanColumns(widths, maxWidths);
    
    var colspanWidths = this.getColspanWidth(widths, totalNumberOfColumns);
    this.adjustWidthsForColspans(widths, maxWidths, colspanWidths);
    
    this.adjustColspansForWidths(widths, maxWidths);
    
    return widths;
  }

  this.isTableRow = function(line) {
    return line.match(/^\||!\|.*/);
  }

  this.splitRows = function(rows) {
    var splitRows = [];

    this.each(rows, function(row) {
      splitRows.push(this.splitRow(row));
    }, this);

    return splitRows;
  }

  this.splitRow = function(row) {
    var columns = this.trim(row).split('|');

    if(!this.wikificationPrevention && columns[0] == '!') {
      this.wikificationPrevention = true;
      columns[1] = '!' + columns[1]; //leave a placeholder
    }

    columns = columns.slice(1, columns.length - 1);

    this.each(columns, function(column, i) {
      columns[i] = this.trim(column);
    }, this);

    return columns;
  }
  
  this.getRealColumnWidths = function(rows) {
    var widths = [];

    this.each(rows, function(row, rowIndex) {
      widths.push([]);
      
      this.each(row, function(column, columnIndex) {
        widths[rowIndex][columnIndex] = column.length;
      }, this);
    }, this);

    return widths;
  }

  this.getMaxWidths = function(widths, totalNumberOfColumns) {
    var maxWidths = [];
    var row = null;
    
    this.each(widths, function(row, rowIndex) {
      this.each(row, function(columnWidth, columnIndex) {
        if(columnIndex == (row.length - 1) && row.length < totalNumberOfColumns) {
          return false;
        }
        
        if(columnIndex >= maxWidths.length) {
          maxWidths.push(columnWidth);
        }
        else if(columnWidth > maxWidths[columnIndex]) {
          maxWidths[columnIndex] = columnWidth;
        }        
      }, this);
    }, this);
    
    return maxWidths;
  }
  
  this.getNumberOfColumns = function(rows) {
    var numberOfColumns = 0;

    this.each(rows, function(row) {
      if(row.length > numberOfColumns) {
        numberOfColumns = row.length;
      }
    });

    return numberOfColumns;
  }
  
  this.getColspanWidth = function(widths, totalNumberOfColumns) {
    var colspanWidths = [];
    var colspan = null;
    var colspanWidth = null;

    this.each(widths, function(row, rowIndex) {
      if(row.length < totalNumberOfColumns) {
        colspan = totalNumberOfColumns - row.length;
        colspanWidth = row[row.length - 1];
        
        if(colspan >= colspanWidths.length) {
          colspanWidths[colspan] = colspanWidth;
        }
        else if(!colspanWidths[colspan] || colspanWidth > colspanWidths[colspan]) {
          colspanWidths[colspan] = colspanWidth;
        }
      }
    });
    
    return colspanWidths;
  }
  
  this.setMaxWidthsOnNonColspanColumns = function(widths, maxWidths) {
    this.each(widths, function(row, rowIndex) {
      this.each(row, function(columnWidth, columnIndex) {
        if(columnIndex == (row.length - 1) && row.length < maxWidths.length) {
          return false;
        }
                
        row[columnIndex] = maxWidths[columnIndex];
      }, this);
    }, this);
  }
  
  this.getWidthOfLastNumberOfColumns = function(maxWidths, numberOfColumns) {
    var width = 0;
    
    for(var i = 1; i <= numberOfColumns; i++) {
      width += maxWidths[maxWidths.length - i]
    }
    
    return width + numberOfColumns - 1; //add in length of separators
  }
  
  this.spreadOutExcessOverLastNumberOfColumns = function(maxWidths, excess, numberOfColumns){
    var columnToApplyExcessTo = maxWidths.length - numberOfColumns;
    
    for(var i = 0; i < excess; i++) {
      maxWidths[columnToApplyExcessTo++] += 1;
      
      if(columnToApplyExcessTo == maxWidths.length) {
        columnToApplyExcessTo = maxWidths.length - numberOfColumns;
      }
    }
  }
  
  this.adjustWidthsForColspans = function(widths, maxWidths, colspanWidths) {
    var lastNumberOfColumnsWidth = null;
    var excess = null;
    
    this.each(colspanWidths, function(colspanWidth, index) {
      lastNumberOfColumnsWidth = this.getWidthOfLastNumberOfColumns(maxWidths, index + 1);
      
      if(colspanWidth && colspanWidth > lastNumberOfColumnsWidth){
        excess = colspanWidth - lastNumberOfColumnsWidth;
        this.spreadOutExcessOverLastNumberOfColumns(maxWidths, excess, index + 1);
        this.setMaxWidthsOnNonColspanColumns(widths, maxWidths);
      }
    }, this);
  }
  
  this.adjustColspansForWidths = function(widths, maxWidths) {
    var colspan = null;
    var lastNumberOfColumnsWidth = null
    
    this.each(widths, function(row, rowIndex) {
      colspan = maxWidths.length - row.length + 1;
      
      if(colspan > 1) {
        row[row.length - 1] = this.getWidthOfLastNumberOfColumns(maxWidths, colspan);
      }      
    }, this);
  }

  /*
   * Utility functions
   */
  this.trim = function(text) {
    return (text || "").replace( /^\s+|\s+$/g, "" );
  }
  
  this.each = function(array, callback, context) {
    var index = 0;
    var length = array.length;

    while(index < length && callback.call(context, array[index], index) !== false) {
      index++;
    }
  },

  this.rightPad = function(value, length) {
    var padded = value;

    for(var i = 0, j = length - value.length; i < j; i++) {
      padded += " ";
    }

    return padded;
  }
  
}
