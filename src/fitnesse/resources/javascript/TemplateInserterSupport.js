function InsertTemplate(templateList, textArea)
{
  var inserter = new TemplateInserter();
  textArea.value = inserter.insertInto(templateList, textArea);
  textArea.focus();
}

document.write('<input type="button" value="Insert Template" onClick="InsertTemplate(document.f.templateList, document.f.pageContent)" title="Inserts the selected template">');
