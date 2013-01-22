function TemplateInserter()
{
  this.insertInto = function(templateValue, textArea) {
    
    if(templateValue !== "")
    {
      var pageDataUrl = templateValue.substr(1, templateValue.length - 1) + "?pageData";
      
      $.ajax({
        url: pageDataUrl,
        success: function(result) {
          $('#pageContent').replaceSelectedText(result);
        },
        error: function() {
          alert("Error Accessing Template");
        }
      });
    }
  };
}