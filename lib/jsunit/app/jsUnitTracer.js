/* jsUnit */
/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Edward Hieatt code.
 *
 * The Initial Developer of the Original Code is
 * Edward Hieatt, edward@jsunit.net.
 * Portions created by the Initial Developer are Copyright (C) 2001
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Edward Hieatt, edward@jsunit.net (original author)
 * Bob Clary, bc@bclary.com
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

function jsUnitTracer() {
  this._traceWindow        = null;
  this.TRACE_LEVEL_WARNING = 1;
  this.TRACE_LEVEL_INFO    = 2;
  this.TRACE_LEVEL_DEBUG   = 3;
  this.popupWindowsBlocked = false;
}

jsUnitTracer.prototype.initialize = function () 
{
  if (this._traceWindow != null && top.testManager.closeTraceWindowOnNewRun.checked)
    this._traceWindow.close();

  this._traceWindow = null;
}

jsUnitTracer.prototype.finalize = function () 
{
  if (this._traceWindow!=null) {
    this._traceWindow.document.write('<\/body>\n<\/html>');
    this._traceWindow.document.close();
  }
}

jsUnitTracer.prototype.warn = function () 
{
  this._trace(arguments[0], arguments[1], this.TRACE_LEVEL_WARNING);
}

jsUnitTracer.prototype.inform = function () 
{
  this._trace(arguments[0], arguments[1], this.TRACE_LEVEL_INFO);
}

jsUnitTracer.prototype.debug = function () 
{
  this._trace(arguments[0], arguments[1], this.TRACE_LEVEL_DEBUG);
}

jsUnitTracer.prototype._trace = function (message, value, traceLevel) 
{
  if (this._getChosenTraceLevel() >= traceLevel) {
    var traceString = message;
    if (value)
      traceString += ': ' + value;
    this._writeToTraceWindow(traceString, traceLevel);
  }
}

jsUnitTracer.prototype._getChosenTraceLevel = function () 
{
  return eval(top.testManager.traceLevel.value);
}

jsUnitTracer.prototype._writeToTraceWindow  = function (traceString, traceLevel) 
{
  var htmlToAppend = '<p class="jsUnitDefault">' + traceString + '<\/p>\n';
  this._getTraceWindow().document.write(htmlToAppend);
}

jsUnitTracer.prototype._getTraceWindow = function () 
{
  if (this._traceWindow == null && !this.popupWindowsBlocked) {
    this._traceWindow = window.open('','','width=600, height=350,status=no,resizable=yes,scrollbars=yes');
    if (!this._traceWindow) {
      this.popupWindowsBlocked = true;
    }
    else {
      var resDoc = this._traceWindow.document;
      resDoc.write('<html>\n<head>\n<link rel="stylesheet" href="css/jsUnitStyle.css">\n<title>Tracing - JsUnit<\/title>\n<head>\n<body>');
      resDoc.write('<h2>Tracing - JsUnit<\/h2>\n');
    }
  }
  return this._traceWindow;
}

if (xbDEBUG.on)
{
  xbDebugTraceObject('window', 'jsUnitTracer');
}

