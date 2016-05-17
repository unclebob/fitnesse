function TemplateInserter()
{
  this.insertInto = function(templateValue, codeMirrorDoc) {
    
    if(templateValue !== "")
    {
      var pageDataUrl = templateValue.substr(1, templateValue.length - 1) + "?pageData";
      
      $.ajax({
        url: pageDataUrl,
        success: function(result) {
          codeMirrorDoc.replaceSelection(result);
        },
        error: function() {
          alert("Error Accessing Template");
        }
      });
    }
  };
}