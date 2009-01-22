function WikiFormatter()
{
  this.trim = function(text) {
    return (text || "").replace( /^\s+|\s+$/g, "" );
  }

  this.rightPad = function(value, length) {
    var padded = value;

    for(var i = 0, j = length - value.length; i < j; i++) {
      padded += " ";
    }

    return padded;
  }
  
  this.isTableRow = function(line) {
    return line.indexOf('|') == 0;
  }

  this.splitRow = function(line) {
    var parts = this.trim(line).split('|');
    parts = parts.slice(1, parts.length - 1);

    for(var i = 0, j = parts.length; i < j; i++) {
      parts[i] = this.trim(parts[i]);
    }

    return parts;
  }

  this.getMaxColumnWidthsOf = function(table) {
    widths = [];

    for(var i = 0, j = table.length; i < j; i++) {
      var parts = this.splitRow(table[i]);

      for(var x = 0, y = parts.length; x < y; x++) {
        if(widths.length <= x) {
          widths.push(parts[x].length)
        }
        else if(parts[x].length > widths[x]) {
          widths[x] = parts[x].length;
        }
      }
    }

    return widths;
  }

  this.formatTable = function(table) {
    var formatted = "";
    var widths = this.getMaxColumnWidthsOf(table);

    for(var i = 0, j = table.length; i < j; i++) {
      formatted += "|";
      var parts = this.splitRow(table[i]);

      for(var x = 0, y = parts.length; x < y; x++) {
        formatted += this.rightPad(parts[x], widths[x]) + "|";
      }

      formatted += "\n";
    }

    return formatted;
  }

  this.format = function(wikiText) {
    var formatted = "";
    var currentTable = [];
    var currentNumberOfColumns = 0;
    var lines = wikiText.split("\n");

    for(var i = 0, j = lines.length; i < j; i++) {
      if(this.isTableRow(lines[i])) {
        var numberOfColumns = this.splitRow(lines[i]).length

        if(currentTable.length == 0 || currentNumberOfColumns == numberOfColumns) {
          currentTable.push(lines[i]);
          currentNumberOfColumns = numberOfColumns;
          continue;
        }
      }

      formatted += this.formatTable(currentTable);
      currentTable = [];

      if(this.isTableRow(lines[i])) {
        currentTable.push(lines[i]);
        currentNumberOfColumns = this.splitRow(lines[i]).length;
      }
      else {
        formatted += lines[i] + "\n";
      }
    }

    formatted += this.formatTable(currentTable);
    return formatted.slice(0, formatted.length - 1);
  }
}
