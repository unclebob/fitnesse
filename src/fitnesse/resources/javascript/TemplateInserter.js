function TemplateInserter()
{
  this.insertInto = function(templateList, textArea) {
    
    var xmlhttp;
    if (window.XMLHttpRequest) {
      xmlhttp=new XMLHttpRequest();
    } else {
      xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
    }
    
    xmlhttp.onreadystatechange=function()
    {
      if (xmlhttp.readyState==4 && xmlhttp.status==200)
      {
        textArea.value = xmlhttp.responseText;
      }
    }
    
    var selectedValue = templateList.options[templateList.selectedIndex].value;
    selectedValue = selectedValue.substr(1, selectedValue.length - 1);
    
    xmlhttp.open("GET", selectedValue + "?pageData",true);
    
    xmlhttp.send();
  }
}