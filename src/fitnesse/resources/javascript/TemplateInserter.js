function TemplateInserter()
{
  this.insertInto = function(templateList, textArea) {
    
    var selectedValue = templateList.options[templateList.selectedIndex].value;
    
    if(selectedValue !== "")
    {
      pageDataUrl = selectedValue.substr(1, selectedValue.length - 1) + "?pageData";
      
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