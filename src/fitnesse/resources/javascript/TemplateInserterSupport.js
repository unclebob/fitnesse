function InsertTemplate(templateList, textArea)
{
  if(Wysiwyg.getEditorMode() === "textarea")
  {
    var inserter = new TemplateInserter();
    inserter.insertInto(templateList, textArea);
    textArea.focus();
  }
}

document.write('<input type="button" value="Insert Template" onClick="InsertTemplate(document.f.templateMap, document.f.pageContent)" title="Inserts the selected template">');