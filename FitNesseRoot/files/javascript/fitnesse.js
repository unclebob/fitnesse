var collapsableOpenCss = "collapsable";
var collapsableClosedCss = "hidden";
var collapsableOpenImg = "/files/images/collapsableOpen.gif";
var collapsableClosedImg = "/files/images/collapsableClosed.gif";

function toggleCollapsable(id)
{
  var div = document.getElementById(id);
  var img = document.getElementById("img" + id);
  if (div.className.indexOf(collapsableClosedCss) != -1)
  {
    div.className = collapsableOpenCss;
    img.src = collapsableOpenImg;
  }
  else
  {
    div.className = collapsableClosedCss;
    img.src = collapsableClosedImg;
  }
}

function popup(window_id) {
  var window = document.getElementById(window_id);
  window.style.visibility = "visible";
}

function popdown(window_id) {
  var window = document.getElementById(window_id);
  window.style.visibility = "hidden";
}

function expandOrCollapseAll(cssClass)
{
  divs = document.getElementsByTagName("div");
  for (i = 0; i < divs.length; i++)
  {
    div = divs[i];
    if (div.className == cssClass)
    {
      toggleCollapsable(div.id);
    }
  }
}

function collapseAll()
{
  expandOrCollapseAll(collapsableOpenCss);
}

function expandAll()
{
  expandOrCollapseAll(collapsableClosedCss);
}

function symbolicLinkRename(linkName, resource)
{
  var newName = document.symbolics[linkName].value.replace(/ +/g, '');

  if (newName.length > 0)
    window.location = resource + '?responder=symlink&rename=' + linkName + '&newname=' + newName;
  else
    alert('Enter a new name first.');
}

// Allow ctrl-s to save the changes.
// Currently this alone appears to work on OS X. For windows (and linux??) use alt-s, which doesn't work on OS X!
formToSubmit = null;
function enableSaveOnControlS(control, formToSubmit)
{
  formToSubmit = formToSubmit;
  if (document.addEventListener)
  {
    document.addEventListener("keypress", keypress, false);
  }
  else if (document.attachEvent)
  {
    document.attachEvent("onkeypress", keypress);
  }
  else
  {
    document.onkeypress = keypress;
  }

}
function keypress(e)
{
  if (!e) e = event;
  if (e.keyIdentifier == "U+0053" || e.keyIdentifier == "U+0060")
  {
    suppressdefault(e, formToSubmit.keypress.checked);
    if (formToSubmit != null)
    {
      formToSubmit.submit
    }
  }
}

function doSilentRequest(url)
{
  var xmlHttp;
  try
  {
    // Firefox, Opera 8.0+, Safari
    xmlHttp=new XMLHttpRequest();
  }
  catch (e)
  {
    // Internet Explorer
    try
    {
      xmlHttp=new ActiveXObject("Msxml2.XMLHTTP");
    }
    catch (e)
    {
      try
      {
        xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
      }
      catch (e)
      {
        alert("Your browser does not support AJAX!");
        return false;
      }
    }
  }
  xmlHttp.onreadystatechange=function() {}
  xmlHttp.open("GET",url,true);
  xmlHttp.send(null);
  return false;
}

