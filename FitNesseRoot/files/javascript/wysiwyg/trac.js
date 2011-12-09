function addEvent(element, type, listener) {
    if (element.addEventListener) {
        element.addEventListener(type, listener, false);
        return true;
    }
    else if (element.attachEvent) {
        return element.attachEvent("on" + type, listener);
    }
    return false;
}

function getAncestorByTagName(node, tag) {
    tag = tag.toLowerCase();
    do {
        node = node.parentNode;
    } while (node.nodeType == 1 && node.tagName.toLowerCase() != tag);

    return node.nodeType == 1 ? node : null;
}

