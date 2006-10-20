
var collapsableOpenCss = "collapsable";
var collapsableClosedCss = "hidden";
var collapsableOpenImg = "/files/images/collapsableOpen.gif";
var collapsableClosedImg = "/files/images/collapsableClosed.gif";

function toggleCollapsable(id)
{
	var div = document.getElementById(id);
	var img = document.getElementById("img" + id);
	if(div.className.indexOf(collapsableClosedCss) != -1)
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