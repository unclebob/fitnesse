function addClassName(element, className) {
  element.className += (element.className ? ' ' : '') + className;
}

function removeClassName(element, className) {
  var c = element.className;
  
  if(c && c.length > 0) {
    element.className = c.replace(new RegExp("(^|\\s)" + className + "(\\s|$)"), '');
  }
}

function setWrap(area, wrap) {
  if (area.wrap) {
      area.wrap = wrap;
  } else { // wrap attribute not supported - try Mozilla workaround
      area.setAttribute('wrap', wrap);
      var newarea= area.cloneNode(true);
      newarea.value= area.value;
      area.parentNode.replaceChild(newarea, area);
  }
}

function toggleWrap(checkbox) {
  setWrap(document.f.pageContent, checkbox.checked ? 'soft' : 'off');
  
  if(checkbox.checked) {
    removeClassName(document.f.pageContent, 'no_wrap');
  }
  else {
    addClassName(document.f.pageContent, 'no_wrap');
  }
}

document.write('<input type="checkbox" accesskey="w" onClick="toggleWrap(this)" title="Turns on/off wrapping">');
document.write('<span>wrap</span>');
