function FormatWiki(textArea)
{
  var formatter = new WikiFormatter();
  textArea.value = formatter.format(textArea.value);
  textArea.focus();
}

document.write('<input type="button" accesskey="f" value="Format" onClick="FormatWiki(document.f.pageContent)" title="Formats the wiki text">');

