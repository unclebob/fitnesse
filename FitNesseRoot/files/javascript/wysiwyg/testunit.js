TracWysiwyg.TestUnit = function() {
    this.cases = {};
    this.assertCounts = {};
    this.current = null;
};

(function() {
    var prototype = TracWysiwyg.TestUnit.prototype;

    prototype.inspect = function(value) {
        var type = typeof value;
        switch (type) {
        case "string":
            return value.replace(/[\u0000-\u001f\\\u007f\ufffe\uffff]/g, function(m) {
                var code = m.charCodeAt(0);
                switch (code) {
                case 9:  return "\\t";
                case 10: return "\\n";
                case 11: return "\\v";
                case 12: return "\\f";
                case 13: return "\\r";
                case 92: return "\\\\";
                }
                return "\\u" + (0x10000 + code).toString(16).substring(1);
            });
            break;
        case "object":
        case "function":
            if (value instanceof RegExp) {
                return value.toString();
            }
        }
        return "{%}".replace("%", type) + value.toString();
    };

    prototype.fragment = function() {
        var start = 0;
        var arg = arguments[0];
        var d;
        if (arg.nodeType != 9) {
            d = document;
        }
        else {
            d = arg;
            start = 1;
        }
        var fragment = d.createDocumentFragment();
        var length = arguments.length;
        for (var i = start; i < length; i++) {
            fragment.appendChild(arguments[i]);
        }
        return fragment;
    };

    prototype.element = function(tag) {
        var start = 0;
        var arg = arguments[start++];
        var d, tag;
        if (typeof arg == "string") {
            d = document;
            tag = arg;
        }
        else {
            d = arg;
            tag = arguments[start++];
        }
        var element = d.createElement(tag);
        for (var i = start; i < arguments.length; i++) {
            arg = arguments[i];
            switch (typeof arg) {
            case "object":
                if (typeof arg.nodeType == "undefined") {
                    for (var name in arg) {
                        var value = arg[name];
                        switch (name) {
                        case "id":
                            element.id = value;
                            break;
                        case "class": case "className":
                            element.className = value;
                            break;
                        default:
                            element.setAttribute(name, value);
                            break;
                        }
                    }
                    continue;
                }
                break;
            case "string":
                arg = d.createTextNode(arg);
                break;
            }
            element.appendChild(arg);
        }
        return element;
    };

    prototype.text = function() {
        var start = 0;
        var arg = arguments[start++];
        var d, text;
        if (typeof arg == "string") {
            d = document;
            text = arg;
        }
        else {
            d = arg;
            text = arguments[start++];
        }
        return d.createTextNode(text);
    };

    prototype.$ = function(id) {
        return typeof id == "string" ? document.getElementById(id) : id;
    };

    prototype.add = function(name, method) {
        if (name in this.cases) {
            throw "'" + name + "' is in use.";
        }
        this.cases[name] = method;
        this.assertCounts[name] = 0;
    };

    prototype.assertEqual = function(expected, actual, label) {
        var count = ++this.assertCounts[this.current];
        if (typeof (expected) == typeof (actual) && expected == actual) {
            return true;
        }
        throw (label || "") + "[" + count + "]\n"
            + this.inspect(expected) + " (" + expected.length + ")\n"
            + this.inspect(actual) + " (" + actual.length + ")";
    };

    prototype.assertMatch = function(pattern, string, label) {
        var count = ++this.assertCounts[this.current];
        if (pattern.test(string)) {
            return true;
        }
        throw (label || "") + "[" + count + "]\n"
            + this.inspect(pattern) + "\n"
            + this.inspect(string) + " (" + string.length + ")";
    };

    prototype.run = function() {
        var self = this
        var $ = this.$, element = this.element, text = this.text;
        var d = document;
        var cases = this.cases;
        var assertCounts = this.assertCounts;
        var names = [];
        for (var name in cases) {
            names.push(name);
            assertCounts[name] = 0;
        }

        var container = $("testunit");
        var count;
        if (container) {
            container.parentNode.removeChild(container);
        }
        container = element(
            "table", { id: "testunit" },
            element("caption", { id: "testunit.summary" }),
            element("tbody", { id: "testunit.body" }));
        d.body.appendChild(container);
        var body = $("testunit.body");
        var summary = $("testunit.summary");
        for (count = 0; count < names.length; count++) {
            body.appendChild(
                element("tr",
                    element("td", names[count]),
                    element("td", { id: "testcase." + count }, "...")));
        }

        count = 0;
        var success = 0;
        var invoke = function() {
            if (count >= names.length) {
                self.current = null;
                return;
            }

            var current = names[count];
            self.current = current;
            var cell = $("testcase." + count);
            cell.className = "current";
            try {
                cases[current].call(self);
                cell.className = "success";
                cell.replaceChild(text("OK"), cell.firstChild);
                success++;
            }
            catch (e) {
                cell.className = "failure";
                var message = e.message || e.toString();
                if (e.stack) {
                    message = [ message, e.stack ].join("\n\n");
                }
                cell.replaceChild(
                    element("textarea", { id: "testcase." + count + ".textarea",
                                          rows: message.split("\n").length,
                                          cols: 80,
                                          readonly: "readonly" }),
                    cell.firstChild);
                $("testcase." + count + ".textarea").value = message;
            }
            summary.innerHTML = success + " / " + names.length;

            count++;
            setTimeout(invoke, 10);
        };

        invoke();
    };
})();
