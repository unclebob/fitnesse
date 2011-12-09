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

