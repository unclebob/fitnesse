
describe("parser and formatter", function () {

    var editor, contentDocument, contentBody;

    beforeEach(function() {
        document.getElementById("editor").innerHTML = '<textarea class="wikitext no_wrap" id="pageContent" name="pageContent" wrap="off"></textarea>';
        jasmine.Clock.useMock();

        Wysiwyg.editorMode = 'wysiwyg';
        var options = Wysiwyg.getOptions();
        editor = Wysiwyg.newInstance(document.getElementById("pageContent"), options);
        jasmine.Clock.tick(1000);

        contentDocument = editor.contentDocument;
        contentBody = contentDocument.getElementsByTagName("body")[0];
    });

    function fragment() {
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
    }

    function element(tag) {
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
    }

    function br() {
        return element("br")
    }

    function a(link, label, autolink) {
        var attrs = {
            href: link,
            title: link,
            'data-wysiwyg-link': link };
        if (autolink) {
            attrs['data-wysiwyg-autolink'] = 'true';
        }
        return element("a", attrs, label || link);
    }

    function generate(dom, wikitext, options, withoutDomToWikitext, withoutWikitextToFragment) {
        dom = dom.cloneNode(true);
        var anonymous = dom.ownerDocument.createElement("div");
        anonymous.appendChild(dom);

        if (!withoutWikitextToFragment) {
            var fragment = editor.wikitextToFragment(wikitext, contentDocument, options);
            var generated = contentDocument.createElement("div");
            generated.appendChild(fragment);
            var generatedHtml = generated.innerHTML;
            if (!generated.addEventListener || window.opera) {
                generatedHtml = generatedHtml.replace(/\n\r/g, "\uffff").replace(/\uffff\n?/g, "\n");
            }
            expect(generatedHtml).toBe(anonymous.innerHTML); // wikitextToFragment
        }
        if (!withoutDomToWikitext) {
            expect(editor.domToWikitext(anonymous, options)).toBe(wikitext + "\n"); // domToWikitext
        }
    }

    function generateFragment(dom, wikitext, options) {
        generate(dom, wikitext, options, true, false);
    }

    function generateWikitext(dom, wikitext, options) {
        generate(dom, wikitext, options, false, true);
    }

    it("should be able to walk over the elements in the DOM tree", function () {
        var list;
        function iterator(node) {
            var value;
            if (node) {
                switch (node.nodeType) {
                    case 1: value = node.tagName.toLowerCase(); break;
                    case 3: value = "#text"; break;
                }
            }
            else {
                value = "(null)";
            }
            list.push(value);
        }

        function doTreeWalk(expected, dom) {
            list = [];
            editor.treeWalk(dom, iterator);
            expect(list.join(" ")).toBe(expected);
            list = [];
            editor._treeWalkEmulation(dom, iterator);
            expect(list.join(" ")).toBe(expected);
        }

        doTreeWalk("p #text (null)", element("div", element("p", "paragraph")));
        doTreeWalk("#text (null)", element("div", element("p", "paragraph")).firstChild);
        doTreeWalk("(null)", element("div", element("p")).firstChild);

        var dom = element("div");
        dom.innerHTML = [
            '<h2 id="Tables">Tables</h2>',
            '<p>',
            'Simple tables can be created like this:',
            '</p>',
            '<pre class="wiki">||Cell 1||Cell 2||Cell 3||',
            '||Cell 4||Cell 5||Cell 6||',
            '</pre><p>',
            'Display:',
            '</p>',
            '<table class="wiki">',
            '<tbody><tr><td>Cell 1</td><td>Cell 2</td><td>Cell 3',
            '</td></tr><tr><td>Cell 4</td><td>Cell 5</td><td>Cell 6',
            '</td></tr></tbody></table>',
            '<p>',
            'Note that more complex tables can be created using',
            '<a class="wiki" href="/practice/wiki/WikiRestructuredText#BiggerReSTExample">re<em>Structured</em>Text</a>.',
            '</p>' ].join("");
        var expected = [
            'h2', '#text', 'p', '#text', 'pre', '#text', 'p', '#text',
            'table', 'tbody',
            'tr', 'td', '#text', 'td', '#text', 'td', '#text',
            'tr', 'td', '#text', 'td', '#text', 'td', '#text',
            'p', '#text', 'a', '#text', 'em', '#text', '#text', '#text',
            '(null)'].join(" ");
        doTreeWalk(expected, dom);
    });

    it("test isBogusLineBreak (can get rid of)", function() {
        var dom = fragment(
            element("p", element("br")),
            element("p", "foobar", element("br"), "foobar"),
            element("p", element("b", "foobar", element("br"))),
            element("p", element("b", "foobar"), element("br")),
            element("br"));
        function assert(expected, node) {
            expect(editor.isBogusLineBreak(node)).toBe(expected); //, "#" + (count++));
        }
        assert(true, dom.childNodes[0].childNodes[0]);
        assert(false, dom.childNodes[1].childNodes[0]);
        assert(false, dom.childNodes[1].childNodes[1]);
        assert(true, dom.childNodes[1].childNodes[2]);
        assert(false, dom.childNodes[2].childNodes[0].childNodes[0]);
        assert(true, dom.childNodes[2].childNodes[0].childNodes[1]);
        assert(false, dom.childNodes[3].childNodes[0].childNodes[0]);
        assert(true, dom.childNodes[3].childNodes[1]);
        assert(true, dom.childNodes[4]);
    });

    it("should convert a code block", function() {
        var dom = fragment(
            element("p", "`abc`"),
            element("p", element("pre", br(), "{{{code-block", br())));
        var wikitext = [
            "`abc`",
            "",
            "{{{",
            "{{{code-block",
            "}}}" ].join("\n");
        generate(dom, wikitext);
    });

    it("should ignore nested code blocks", function() {
        var dom = fragment(
            element("p", element("pre", br(), "#!python", br(), "= level 1", br(), "{{{", br(), "= level 2", br()), br(), "= level 1}}}"));
        generateFragment(dom, [
            "{{{",
            "#!python",
            "= level 1",
            "{{{",
            "= level 2",
            "}}}",
            "= level 1",
            "}}}" ].join("\n"));
        generateWikitext(dom, [
            "{{{",
            "#!python",
            "= level 1",
            "{{{",
            "= level 2",
            "}}}",
            "= level 1}}}" ].join("\n"));
    });


    it("should format code block with empty lines", function() {
        var dom = fragment(
            element("p", "test:",
                element("pre", br(), "first line", br(), br(), "  second line ")));
        var wikitext = [
            "test:{{{",
            "first line",
            "",
            "  second line }}}" ].join("\n");
        generate(dom, wikitext);
    });

    it("paragraph", function() {
        var dom = fragment(
            element("p", "Paragraph", br(), "continued..."),
            element("p", "Second paragraph", br(), "continued..."));
        generate(dom, [
            "Paragraph",
            "continued...",
            "",
            "Second paragraph",
            "continued..." ].join("\n"));
    });

    it("link", function() {
        var dom = fragment(
            element("p", a("LinkPage", "LinkPage", true)));
        generate(dom,
            "LinkPage");
    });

    it("link with markup", function() {
        var dom = fragment(
            element("p", a("LinkPage", element("i", "label"))));
        generate(dom,
            "[[''label''][LinkPage]]");
    });

    it("link with hash", function() {
        var dom = fragment(
            element("p", a("LinkPage#foo", "label")));
        generate(dom,
            "[[label][LinkPage#foo]]");
    });

    it("link with parameter", function() {
        var dom = fragment(
            element("p", a("LinkPage?edit&test", "label")));
        generate(dom,
            "[[label][LinkPage?edit&test]]");
    });

    it("wiki macros", function() {
        var dom = fragment(
            element("table", element("tbody",
                element("tr", element("td", "!c !2 Contents")),
                element("tr", element("td", "!contents -g")))),
            element("p", { 'class': 'meta' }, "!path fitnesse.jar"),
            element("p", { 'class': 'meta' }, "!path classes"));
        generateFragment(dom, [
            "|!c !2 Contents|",
            "|!contents -g|",
            "",
            "!path fitnesse.jar",
            "!path classes" ].join("\n"));
    });

    it("hr", function() {
        var dom = fragment(
            element("p", "Paragraph"),
            element("hr"),
            element("p", "Another paragraph"),
            element("hr"));
        generateFragment(dom, [
            "Paragraph",
            "----",
            "Another paragraph",
            "----" ].join("\n"));
        generate(dom, [
            "Paragraph",
            "",
            "----",
            "Another paragraph",
            "",
            "----" ].join("\n"));
    });

    it("escape !- .. -! - literal escape", function() {
        var dom = element("p", "foo ", element("tt", {'class': 'escape'}, "bar"), " baz");
        var wikitext = "foo !-bar-! baz";
        generate(dom, wikitext);
    });

    it("escape !< .. >! - html escape", function() {
        var dom = element("p", "foo ", element("tt", {'class': 'htmlescape'}, "bar"), " baz");
        var wikitext = "foo !<bar>! baz";
        generate(dom, wikitext);
    });

    it("hash table", function() {
        var dom = element("p",
            element("table", {'class': 'hashtable'},
                element("tbody",
                    element("tr",
                        element("td", "bar"),
                        element("td", "val")))));

        var wikitext = "!{bar:val}";
        generate(dom, wikitext);
    });

    it("hash table, empty entries should be skipped", function() {
        var dom = element("p",
            element("table", {'class': 'hashtable'},
                element("tbody",
                    element("tr",
                        element("td", ""),
                        element("td", "")),
                element("tr",
                    element("td", "bar"),
                    element("td", "val")))));

        var wikitext = "!{bar:val}";
        generateWikitext(dom, wikitext);
    });

    it("hash table with escaped multi-line value", function() {
        var dom = element("p",
            element("table", {'class': 'hashtable'},
                element("tbody",
                    element("tr",
                        element("td", "a"),
                        element("td", element("tt", { "class": "escape"}, "bc", br(), "def"))))));

        var wikitext = "!{a:!-bc\ndef-!}";
        generate(dom, wikitext);
    });

    it("hash table with multi-line value", function() {
        var dom = element("p",
            element("table", {'class': 'hashtable'},
                element("tbody",
                    element("tr",
                        element("td", "a"),
                        element("td", "bc", br(), "def")))));

        var wikitext = "!{a:bc\ndef}";
        var expectedWikitext = "!{a:bc!-\n-!def}";
        generateFragment(dom, wikitext);
        generateWikitext(dom, expectedWikitext);
    });

    it("hash table in table", function() {
        var dom = element("table",
            element("tbody",
                element("tr",
                    element("td", " test "),
                    element("td", " text", element("table", {'class': 'hashtable'},
                        element("tbody",
                            element("tr",
                                element("td", "$contactId1"),
                                element("td", "id1")),
                            element("tr",
                                element("td", "$contactId2"),
                                element("td", "id2")))), "trailer "))));

        var wikitext = "| test | text!{$contactId1:id1,$contactId2:id2}trailer |";
        generate(dom, wikitext);
    });

    it("hash table with variable in table", function() {
        var dom = element("table",
            element("tbody",
                element("tr",
                    element("td", " test "),
                    element("td", " ", element("table", {'class': 'hashtable'},
                        element("tbody",
                            element("tr",
                                element("td", "$contactId1"),
                                element("td", "${ID1}")),
                            element("tr",
                                element("td", "$contactId2"),
                                element("td", "id2")))), " "))));
        var wikitext = "| test | !{$contactId1:${ID1},$contactId2:id2} |";
        generate(dom, wikitext);
    });

    it("hash table with multi-line content in table", function() {
        var dom = element("table",
            element("tbody",
                element("tr",
                    element("td", " test "),
                    element("td", " text", element("table", {'class': 'hashtable'},
                        element("tbody",
                            element("tr",
                                element("td", "$contactId1"),
                                element("td", element("tt", {"class": "escape"}, "id", br(), "1"))),
                            element("tr",
                                element("td", "$contactId2"),
                                element("td", "id", br(), "2")))), "trailer "))));

        var wikitext = "| test | text!{$contactId1:!-id\n1-!,$contactId2:id\n2}trailer |";
        var expectedWikitext = "| test | text!{$contactId1:!-id\n1-!,$contactId2:id!-\n-!2}trailer |";
        generateFragment(dom, wikitext);
        generateWikitext(dom, expectedWikitext);
    });

    it("table with nested table !( .. )!", function() {
        var dom = element("table",
            element("tbody",
                element("tr",
                    element("td", {colspan: 2}, " table ")),
                element("tr",
                    element("td", " ", element("div", {'class': 'nested'},
                        element("table",
                            element("tbody",
                                element("tr",
                                    element("td", " foo "),
                                    element("td", " bar ")),
                                element("tr",
                                    element("td", {colspan: 2}, " baz "))))), " "),
                    element("td", " quit "))));
        var wikitext = "| table |\n| !(| foo | bar |\n| baz |)! | quit |";
        generate(dom, wikitext);
    });

    it("escape ![ .. ]! - plain text table", function() {
        var dom = element("p", "foo ", element("tt", {'class': 'plaintexttable'}, "bar", br(), "baz"), " bee");
        var wikitext = "foo ![bar\nbaz]! bee";
        generate(dom, wikitext);
    });

    it("bold italic", function() {
        var dom = element("p", element("b", element("i", "bold italic")));
        var wikitext = "'''''bold italic'''''";
        generate(dom, wikitext);
    });

    it("bold", function() {
        var wikitext = [
            "The quick '''brown''' fox.",
            "",
            "The quick '''brown''' fox." ].join("\n");
        generateWikitext.call(this,
            fragment(
                element("p", "The quick ", element("b", "brown"), " fox."),
                element("p", "The quick ", element("strong", "brown"), " fox.")),
            wikitext);
        generateFragment.call(this,
            fragment(
                element("p", "The quick ", element("b", "brown"), " fox."),
                element("p", "The quick ", element("b", "brown"), " fox.")),
            wikitext);
    });
    it("italic", function() {
        var wikitext = [
            "The quick ''brown'' fox.",
            "",
            "The quick ''brown'' fox." ].join("\n");
        generateWikitext.call(this,
            fragment(
                element("p", "The quick ", element("i", "brown"), " fox."),
                element("p", "The quick ", element("em", "brown"), " fox.")),
            wikitext);
        generateFragment.call(this,
            fragment(
                element("p", "The quick ", element("i", "brown"), " fox."),
                element("p", "The quick ", element("i", "brown"), " fox.")),
            wikitext);
    });
    it("strike-through", function() {
        var dom = element("p", element("strike", "strike-through"));
        var wikitext = "--strike-through--";
        generate(dom, wikitext);
    });
    it("code", function() {
        var dom = element("p", "`monospace`",
            ", ", element("pre", "mono`s`pace"),
            ", ", "`mono", element("pre", "s"), "pace`");
        generateFragment(dom, "`monospace`, {{{mono`s`pace}}}, `mono{{{s}}}pace`");
        generateWikitext(dom, "`monospace`,{{{mono`s`pace}}}, `mono{{{s}}}pace`");
    });
    it("escaped !-..-!", function() {
        var dom = element("p", "`monospace`",
            ", ", element("tt", {'class': 'escape'}, "mono`s`pace"),
            ", ", "`mono", element("tt", {'class': 'escape'}, "s"), "pace`",
            ", ", element("tt", {'class': 'escape'}, "<ul><li>list item</li></ul>"));
        generate(dom, "`monospace`, !-mono`s`pace-!, `mono!-s-!pace`, !-<ul><li>list item</li></ul>-!");
    });
    it("italic -> bold", function() {
        var dom = element("p",
            "normal",
            element("i", "italic"),
            element("b", "bold"),
            "normal");
        var wikitext = "normal''italic'''''bold'''normal";
        generate(dom, wikitext);
    });
    it("bold -> italic", function() {
        var dom = element("p",
            "normal",
            element("b", "bold"),
            element("i", "italic"),
            "normal");
        var wikitext = "normal'''bold'''''italic''normal";
        generate(dom, wikitext);
    });
    it("[ italic [ xyz ] bold ]", function() {
        var dom = element("p",
            "normal",
            element("i", "italic", element("b", "xyz")),
            element("b", "bold"),
            "normal");
        var wikitext = "normal''italic'''xyz''bold'''normal";
        generate(dom, wikitext);
    });
    it("overlapped markups", function() {
        var dom = element("p",
            "normal",
            element("b", "bold",
                element("i", "italic",
                    element("strike", "strike-through")),
                "sub"),
            ".");
        var wikitext = "normal'''bold''italic--strike-through--''sub'''.";
        generateFragment(dom, wikitext);
    });
    it("repeated markups", function() {
        generateWikitext.call(this,
            element("p", "ab", element("b", "cd"), element("b", "ef"), "gh"),
            "ab'''cdef'''gh");
        generateWikitext.call(this,
            element("p", "ab", element("i", "cd"), element("i", "ef"), "gh"),
            "ab''cdef''gh");
        generateWikitext.call(this,
            element("p", "ab",
                element("i", element("b", "cd")),
                element("b", element("i", "ef")),
                "gh"),
            "ab'''''cdef'''''gh");
    });
    it("markups without text", function() {
        generateWikitext.call(this,
            element("p", "abc", element("b", ""), "def"),
            "abcdef");
        generateWikitext.call(this,
            element("p", "abc", element("i", ""), "def"),
            "abcdef");
        generateWikitext.call(this,
            element("p", "abc", element("tt", ""), "def"),
            "abcdef");
        generateWikitext.call(this,
            element("p", "abc", element("b", element("i", "")), "def"),
            "abcdef");
        generateWikitext.call(this,
            element("p", "abc", element("i", element("b", "")), "def"),
            "abcdef");
    });

    it("! bold italic", function() {
        var dom = element("p", element("b", element("i", "bold",
            element("tt", {'class': 'escape'}, "'''''"), " italic")), ".");
        var wikitext = "'''''bold!-'''''-! italic'''''.";
        generate(dom, wikitext);
    });
    it("! bold", function() {
        var dom = element("p", element("b", "bold", element("tt", {'class': 'escape'}, "'''"), " bold"), ".");
        var wikitext = "'''bold!-'''-! bold'''.";
        generate(dom, wikitext);
    });
    it("! italic", function() {
        var dom = element("p", element("i", "italic", element("tt", {'class': 'escape'}, "''"), " italic"), ".");
        var wikitext = "''italic!-''-! italic''.";
        generate(dom, wikitext);
    });
    it("! strike-through", function() {
        var dom = element("p", element("strike", "strike", element("tt", {'class': 'escape'}, "--"), "through"), ".");
        var wikitext = "--strike!----!through--.";
        generate(dom, wikitext);
    });
    it("! monospace", function() {
        var dom = element("p", element("tt", {'class': 'escape'}, "{{{monospace}}}"), " or ",
            element("tt", {'class': 'escape'}, "`monospace`"));
        var wikitext = "!-{{{monospace}}}-! or !-`monospace`-!";
        generate(dom, wikitext);
    });

    it("multiline escape", function() {
        var dom = element("p", element("tt", {'class': 'escape'}, "escaped", br(), "\"quoted line\"", br()), br());
        var wikitext = [
            "!-escaped",
            "\"quoted line\"",
            "-!" ].join("\n");
        generate(dom, wikitext);
    });

    it("multiline escape 2", function() {
        var dom = element("p", {"class": "meta"}, "!define var (", element("tt", {'class': 'escape'}, "escaped", br(), "line", br(), "123", br()), ")");
        var wikitext = [
            "!define var (!-escaped",
            "line",
            "123",
            "-!)" ].join("\n");
        generate(dom, wikitext);
    });

    it("WikiPageName", function() {
        var dom = fragment(
            element("p",
                a("CamelCase", "CamelCase", true),
                " ", element("tt", {'class': 'escape'}, "CamelCase"), " ",
                a("FooBarA", "FooBarA", true), " FOo ",
                a("FoobarA", "FoobarA", true), " ",
                a("<ParentLink", "<ParentLink", true), " ",
                a(">ChildLink", ">ChildLink", true), " ",
                a(".AbsoluteLink", ".AbsoluteLink", true), " ",
                a(".AbsoluteLink.WikiPage", ".AbsoluteLink.WikiPage", true),
                " OneÅngström Oneångström setTextColor"));
        generateFragment(dom,
            "CamelCase !-CamelCase-! FooBarA FOo FoobarA <ParentLink >ChildLink .AbsoluteLink .AbsoluteLink.WikiPage OneÅngström Oneångström setTextColor");
    });

    it("links2wiki", function() {
        var dom = fragment(
            element("p",
                a("TestPage", "label"),
                a("TestPage", "läbel"),
                a("TestPage", "TestPage", true),
                a("FrontPage?edit", "Edit"),
                " button and add a ",
                a("FitNesse.UserGuide.WikiWord", element("tt", {'class': 'escape'}, "WikiWord")),
                a("http://external.link/bladieblah", "bla")
            ));

        var wikitext = "[[label][TestPage]]"
            + "[[läbel][TestPage]]"
            + "TestPage"
            + "[[Edit][FrontPage?edit]] button and add a [[!-WikiWord-!][FitNesse.UserGuide.WikiWord]]"
            + "[[bla][http://external.link/bladieblah]]";

        // should go both ways (wiki2html and html2wiki)
        generate(dom, wikitext);
        //generateFragment(dom, wikitext);
    });

    it("header", function() {
        var dom = fragment(
            element("h1", "Heading 1"),
            element("h2", "Heading 2"),
            element("h3", element("b", "Heading"), " ", element("i", "3")),
            element("h4", "Heading 4 with ", a("WikiStart", "WikiStart", true)),
            element("h5", "Heading 5"),
            element("h6", "Heading 6"));
        generate(dom, [
            "!1 Heading 1",
            "!2 Heading 2",
            "!3 '''Heading''' ''3''",
            "!4 Heading 4 with WikiStart",
            "!5 Heading 5",
            "!6 Heading 6" ].join("\n"));
    });

    it("header 2", function() {
        var dom = fragment(
            element("h1", "Heading 1  "),
            element("h2", "Heading 2"),
            element("h3", element("b", "Heading"), " ", element("i", "3"), "    "),
            element("h4", "Heading 4 with ", a("WikiStart", "WikiStart", true), "    "),
            element("h5", "Heading 5      "),
            element("h6", "Heading 6 "));
        generateFragment(dom, [
            "!1 Heading 1  ",
            "!2 Heading 2",
            "!3 '''Heading''' ''3''    ",
            "!4 Heading 4 with WikiStart    ",
            "!5 Heading 5      ",
            "!6 Heading 6 " ].join("\n"));
    });

    it("header with link", function() {
        var dom = fragment(
            element("h3",
                a("http://encyclopedia.thefreedictionary.com/XUnit", "xUnit"),
                ": Building the ", element("i", "Code Right")));
        generateFragment(dom, [
            "!3 [[xUnit][http://encyclopedia.thefreedictionary.com/XUnit]]: Building the ''Code Right''"
        ].join("\n"));
    });

    it("list", function() {
        var dom = fragment(
            element("p", "Paragraph"),
            element("ul",
                element("li", "foo bar"),
                element("ul", element("li", "Subitem")),
                element("li", "item 2")),
            element("p", "Paragraph"));
        generateFragment(dom, [
            "Paragraph",
            " * foo bar",
            "   * Subitem",
            " * item 2",
            "Paragraph" ].join("\n"));
        generate(dom, [
            "Paragraph",
            "",
            " * foo bar",
            "   * Subitem",
            " * item 2",
            "",
            "Paragraph" ].join("\n"));
    });

    it("list 2", function() {
        var dom = fragment(
            element("ul",
                element("li", "foo bar"),
                element("ul",
                    element("li", "Subitem 1"),
                    element("ul",
                        element("li", "nested item 1"),
                        element("li", "nested item 2")),
                    element("li", "Subitem 2"),
                    element("li", "Subitem 3")),
                element("li", "item 2")),
            element("p", "Paragraph"));
        generateFragment(dom, [
            "    * foo bar",
            "           * Subitem 1",
            "             * nested item 1",
            "             * nested item 2",
            "            * Subitem 2",
            "            * Subitem 3",
            "    * item 2",
            "Paragraph",
            "" ].join("\n"));
        generate(dom, [
            " * foo bar",
            "   * Subitem 1",
            "     * nested item 1",
            "     * nested item 2",
            "   * Subitem 2",
            "   * Subitem 3",
            " * item 2",
            "",
            "Paragraph" ].join("\n"));
    });

    it("list 3", function() {
        var dom = fragment(
            element("ul",
                element("li", "Item 1"),
                element("ul", element("li", "Item 1.1")),
                element("li", "Item 2")),
            element("p", "And numbered lists can also be given an explicit number"));
        generateFragment(dom, [
            " - Item 1",
            "   - Item 1.1",
            " - Item 2",
            "And numbered lists can also be given an explicit number" ].join("\n"));
        generate(dom, [
            " * Item 1",
            "   * Item 1.1",
            " * Item 2",
            "",
            "And numbered lists can also be given an explicit number" ].join("\n"));
    });

    it("ordered list", function() {
        var dom = fragment(
            element("ol",
                element("li", "foo bar"),
                element("ol", element("li", "Subitem")),
                element("li", "item 2")));
        generate(dom, [
            " 1 foo bar",
            "   1 Subitem",
            " 1 item 2" ].join("\n"));
    });

    it("list at beginning of line", function() {
        var dom = fragment(
            element("ul",
                element("li", "item 1"),
                element("li", "item 2"),
                element("ul",
                    element("li", "sub 2.1"),
                    element("li", "sub 2.2"))),
            element("p", "a. item A", br(), "b. item B", br(), "Paragraph"));
        generateFragment(dom, [
            "- item 1",
            "- item 2",
            "  - sub 2.1",
            "  - sub 2.2",
            "a. item A",
            "b. item B",
            "Paragraph" ].join("\n"));
        generateWikitext(dom, [
            " * item 1",
            " * item 2",
            "   * sub 2.1",
            "   * sub 2.2",
            "",
            "a. item A",
            "b. item B",
            "Paragraph" ].join("\n"));
    });

    it("list + code block", function() {
        var dom = fragment(
            element("p", "Paragraph"),
            element("ul",
                element("li",
                    "item 1")),
            element("p",
                element("pre", br(), "code", br())),
            element("ul",
                element("li",
                    "item 1.1")),
            element("p",
                element("pre", br(), "code", br()),
                element("pre", br(), "code", br())),
            element("ul",
                element("li",
                    "item 2")),
            element("p",
                element("pre", br(), "code", br())));
        generateFragment(dom, [
            "Paragraph",
            " * item 1",
            "{{{",
            "code",
            "}}}",
            "   * item 1.1",
            "{{{",
            "code",
            "}}}",
            "{{{",
            "code",
            "}}}",
            " * item 2",
            "{{{",
            "code",
            "}}}" ].join("\n"));
        generate(dom, [
            "Paragraph",
            "",
            " * item 1",
            "",
            "{{{",
            "code",
            "}}}",
            "",
            " * item 1.1",
            "",
            "{{{",
            "code",
            "}}}{{{",
            "code",
            "}}}",
            "",
            " * item 2",
            "",
            "{{{",
            "code",
            "}}}" ].join("\n"));
    });

    it("definition", function() {
        var dom = fragment(
            element("p", "Paragraph"),
            element("p", { 'class': 'meta' }, "!define Key1 {Val1}"),
            element("p", { 'class': 'meta' }, "!define Key2 {Val2 and more}"),
            element("p", { 'class': 'meta' }, "!define Key3 {Val3}"),
            element("p", "Paragraph"));
        generateFragment(dom, [
            "Paragraph",
            "!define Key1 {Val1}",
            "!define Key2 {Val2 and more}",
            "",
            "!define Key3 {Val3}",
            "Paragraph" ].join("\n"));
        generate(dom, [
            "Paragraph",
            "",
            "!define Key1 {Val1}",
            "!define Key2 {Val2 and more}",
            //"",
            "!define Key3 {Val3}",
            //"",
            "Paragraph" ].join("\n"));
    });

    it("comment", function() {
        var dom = fragment(
            element("p", "Paragraph"),
            element("p", { 'class': 'comment' }, "# comment goes here"),
            element("p", { 'class': 'comment' }, "# second comment"),
            element("p", " #Not a comment"),
            element("p", { 'class': 'comment' }, "# third comment"),
            element("p", { 'class': 'comment' }, "# | table comment |"),
            element("p", "Paragraph"));
        generateFragment(dom, [
            "Paragraph",
            "# comment goes here",
            "# second comment",
            "",
            " #Not a comment",
            "# third comment",
            "# | table comment |",
            "Paragraph" ].join("\n"));
        generateWikitext(dom, [
            "Paragraph",
            "",
            "# comment goes here",
            "",
            "# second comment",
            "",
            "#Not a comment",
            "",
            "# third comment",
            "",
            "# | table comment |",
            "",
            "Paragraph" ].join("\n"));
    });

    it("table", function() {
        var dom = fragment(
            element("p", "Paragraph"),
            element("table",
                element("tbody",
                    element("tr", element("td", "1.1"), element("td", { colspan: "2" }, "1.2")),
                    element("tr", element("td", { colspan: "3" }, "2.1")),
                    element("tr",
                        element("td", "3.1"),
                        element("td", element("i", "3.2")),
                        element("td", element("tt", {'class': 'escape'}, "3"), " ", element("tt", {'class': 'escape'}, "*"))))),
            element("p", "Paragraph"));
        generateFragment(dom, [
            "Paragraph",
            "|1.1|1.2|",
            "|2.1",
            "|3.1|''3.2''|!-3-! !-*-!",
            "Paragraph" ].join("\n"));
    });

    it("escaped table", function() {
        var dom = fragment(
            element("table", { "class": "escaped" },
                element("tbody",
                    element("tr", element("td", " table "), element("td", " ", element("tt", {'class': 'escape'}, "escaped"), " ")),
                    element("tr", element("td", " ''not italic'' "), element("td", " '''not bold''' ")))));
        generate(dom, [
            "!| table | !-escaped-! |",
            "| ''not italic'' | '''not bold''' |" ].join("\n"));
    });

    it("table, hidden top row", function() {
        var dom = fragment(
            element("table",
                element("tbody",
                    element("tr", { "class": "hidden" }, element("td", " table "), element("td", " ", element("tt", {'class': 'escape'}, "escaped"), " ")),
                    element("tr", element("td", " ", element("i", "italic"), " "), element("td", " ", element("b", "bold"), " ")))));
        generate(dom, [
            "-| table | !-escaped-! |",
            "| ''italic'' | '''bold''' |" ].join("\n"));
    });

    it("escaped table, hidden top row", function() {
        var dom = fragment(
            element("table", { "class": "escaped" },
                element("tbody",
                    element("tr", { "class": "hidden" }, element("td", " table "), element("td", " ", element("tt", {'class': 'escape'}, "escaped"), " ")),
                    element("tr", element("td", " ''not italic'' "), element("td", " '''not bold''' ")))));
        generate(dom, [
            "-!| table | !-escaped-! |",
            "| ''not italic'' | '''not bold''' |" ].join("\n"));
    });

    it("escaped text + table", function() {
        var dom = fragment(
            element("p", element("tt", { "class": "escape"}, " escaped text", br()), "| table |"),
            element("table",
                element("tbody",
                    element("tr", element("td", " table text ")))));
        generateFragment(dom, [
            "!- escaped text",
            "-!| table |",
            "| table text |" ].join("\n"));
        generateFragment(dom, [
            "!- escaped text",
            "-!| table |",
            "",
            "| table text |" ].join("\n"));
    });


    it("table 2", function() {
        var dom = fragment(
            element("p", "Paragraph"),
            element("table",
                element("tbody",
                    element("tr", element("td", " 1.1 "), element("td", { colspan: "2" }, " 1.2 ")),
                    element("tr", element("td", { colspan: "3" }, " 2.1 ")),
                    element("tr",
                        element("td", " 3.1 "),
                        element("td", " ", element("i", "3.2"), " "),
                        element("td", " ", element("tt", {'class': 'escape'}, "3"), " ", element("tt", {'class': 'escape'}, " - "), " ")))),
            element("p", "Paragraph"));
        generateFragment(dom, [
            "Paragraph",
            "| 1.1 | 1.2 |",
            "| 2.1 |",
            "| 3.1 | ''3.2'' | !-3-! !- - -! |",
            "Paragraph" ].join("\n"));
        generate(dom, [
            "Paragraph",
            "",
            "| 1.1 | 1.2 |",
            "| 2.1 |",
            "| 3.1 | ''3.2'' | !-3-! !- - -! |",
            "",
            "Paragraph" ].join("\n"));
    });

    it("two tables", function() {
        var dom = fragment(
            element("p", "Paragraph"),
            element("table",
                element("tbody",
                    element("tr", element("td", " 1.1 "), element("td", " 1.2 ")),
                    element("tr", element("td", " 2.1 "), element("td", " 2.2 ")))),
            element("p", ""),
            element("table",
                element("tbody",
                    element("tr", element("td", " 3.1 "), element("td", " 3.2 ")),
                    element("tr", element("td", " 4.1 "), element("td", " 4.2 ")))));
        generate(dom, [
            "Paragraph",
            "",
            "| 1.1 | 1.2 |",
            "| 2.1 | 2.2 |",
            "",
            "| 3.1 | 3.2 |",
            "| 4.1 | 4.2 |" ].join("\n"));
    });

    it("table + rule", function() {
        var dom = fragment(
            element("table",
                element("tbody", element("tr", element("td", " 1st ")))),
            element("p", element("b", "bold")),
            element("table",
                element("tbody", element("tr", element("td", " 2nd ")))),
            element("p", element("tt", {'class': 'escape'}, "'''normal")));
        generate(dom, [
            "| 1st |",
            "",
            "'''bold'''",
            "",
            "| 2nd |",
            "",
            "!-'''normal-!" ].join("\n"));
    });

    it("table [ paragraph, ul ]", function() {
        var dom = fragment(
            element("table",
                element("tbody",
                    element("tr",
                        element("td", element("p", "1.1")),
                        element("td",
                            element("ul",
                                element("li", "item 1"),
                                element("li", "item 2")))),
                    element("tr",
                        element("td",
                            element("p", "2.1* item 3 * item 4")
                        )))));
        generateWikitext(dom, [
            "| 1.1 | * item 1\n * item 2 |",
            "| 2.1* item 3 * item 4 |" ].join("\n"));
    });

    it("table with incomplete markups", function() {
        var dom = fragment(
            element("table",
                element("tbody",
                    element("tr",
                        element("td", " ", element("b", element("i", "' "))),
                        element("td", " ", element("b", "bold"), " "))
                )
            )
        );
        generateFragment(dom, "| '''''' | '''bold''' |");
    });

    it("table with newline in cell", function() {
        var dom = fragment(
            element("table",
                element("tbody",
                    element("tr",
                        element("td", "first line", element("tt", {'class': 'escape'}, br()), "next line"))
                )
            )
        );

        generateWikitext(dom, "| first line!-\n-!next line |");
    });

    it("table preserves newline", function() {
        var dom = fragment(
            element("table",
                element("tbody",
                    element("tr",
                        element("td", element("i", "first line", br(), "next line")))
                )
            )
        );

        generateWikitext(dom, "| ''first line!-\n-!next line'' |");
    });

    it("table with links", function() {
        var dom = fragment(
            element("table",
                element("tbody",
                    element("tr",
                        element("td", { 'colspan': '2' }, " ", element("b", "To Learn More..."), " ")),
                    element("tr",
                        element("td", " ", a("FitNesse.UserGuide.OneMinuteDescription", "A One-Minute Description"), " "),
                        element("td", " ", element("i", "What is ", a("FitNesse.FitNesse", "FitNesse"), "? Start here."), " ")),
                    element("tr",
                        element("td", " ", a("FitNesse.UserGuide.TwoMinuteExample", "A Two-Minute Example"), " "),
                        element("td", " ", element("i", "A brief example. Read this one next."), " "))
                )));
        generate(dom, [
            "| '''To Learn More...''' |",
            "| [[A One-Minute Description][FitNesse.UserGuide.OneMinuteDescription]] | ''What is [[FitNesse][FitNesse.FitNesse]]? Start here.'' |",
            "| [[A Two-Minute Example][FitNesse.UserGuide.TwoMinuteExample]] | ''A brief example. Read this one next.'' |" ].join("\n"));
    });



    it("table from word", function() {
        var dom = element("body");
        dom.innerHTML = [
            '',
            '<table class="MsoTableGrid" style="border: medium none ; border-collapse: collapse;" border="1" cellpadding="0" cellspacing="0">',
            ' <tbody><tr style="">',
            '  <td style="border: 1pt solid windowtext; padding: 0mm 5.4pt; width: 217.55pt;" valign="top" width="290">',
            '  <p class="MsoNormal"><span lang="EN-US">a<o:p></o:p></span></p>',
            '  <p class="MsoNormal"><span lang="EN-US">b<o:p></o:p></span></p>',
            '  </td>',
            '  <td style="border-style: solid solid solid none; border-color: windowtext windowtext windowtext -moz-use-text-color; border-width: 1pt 1pt 1pt medium; padding: 0mm 5.4pt; width: 217.55pt;" valign="top" width="290">',
            '',
            '  <p class="MsoNormal"><span lang="EN-US">b<o:p></o:p></span></p>',
            '  </td>',
            ' </tr>',
            ' <tr style="">',
            '  <td style="border-style: none solid solid; border-color: -moz-use-text-color windowtext windowtext; border-width: medium 1pt 1pt; padding: 0mm 5.4pt; width: 217.55pt;" valign="top" width="290">',
            '  <p class="MsoNormal"><span lang="EN-US">c<o:p></o:p></span></p>',
            '  </td>',
            '  <td style="border-style: none solid solid none; border-color: -moz-use-text-color windowtext windowtext -moz-use-text-color; border-width: medium 1pt 1pt medium; padding: 0mm 5.4pt; width: 217.55pt;" valign="top" width="290">',
            '',
            '  <p class="MsoNormal"><span lang="EN-US">d<o:p></o:p></span></p>',
            '  </td>',
            ' </tr>',
            '</tbody></table>',
            '' ].join("\n");
        generateWikitext(dom, [
            "| a\n\nb | b |",
            "| c | d |" ].join("\n"));
    });

    it("script table", function() {
        var dom = fragment(
            element("p",
                element("tt", {'class': 'plaintexttable'},
                    " script", br(),
                    "Build SIP call with ID 21 to 21@${SOME_IP} and state ${SOME_STATE}.", br()), br()));
        generate(dom, [
            "![ script",
            "Build SIP call with ID 21 to 21@${SOME_IP} and state ${SOME_STATE}.",
            "]!" ].join("\n"));

    });

    it("domToWikitext for code block", function() {
        var br = function() { return element("br") };
        var dom = fragment(
            element("h1", "Heading", br(), "1"),
            element("h2", "Heading", br(), "2"),
            element("h3", "Heading", br(), "3"),
            element("h4", "Heading", br(), "4"),
            element("h5", "Heading", br(), "5"),
            element("h6", "Heading", br(), "6"),
            element("p",
                "var Wysiwyg = function(textarea) {", " ... ", "}"),
            element("p", "> citation continued"),
            element("p", "quote continued"),
            element("ul",
                element("li", "item 1", br(), "continued"),
                element("ol", element("li", "item", br(), "1.1"))),
            element("p", { 'class': 'meta' }, "!define def {dt dd}"),
            element("table",
                element("tbody",
                    element("tr",
                        element("td", "cell", br(), "1"),
                        element("th", "cell", br(), "2")))));
        var wikitext = editor.domToWikitext(dom, { formatCodeBlock: true });
        expect(wikitext).toBe([
            "!1 Heading",
            "1",
            "!2 Heading",
            "2",
            "!3 Heading",
            "3",
            "!4 Heading",
            "4",
            "!5 Heading",
            "5",
            "!6 Heading",
            "6",
            "var Wysiwyg = function(textarea) { ... }",
            "",
            "> citation continued",
            "",
            "quote continued",
            "",
            " * item 1",
            "continued",
            "   1 item",
            "1.1",
            "",
            "!define def {dt dd}",
            "| cell!-",
            "-!1 | cell!-",
            "-!2 |",
            "" ].join("\n"));
    });

    it("selectRange", function() {
        var d = contentDocument;
        function _element() {
            var args = [ d ];
            args.push.apply(args, arguments);
            return element.apply(this, args);
        }
        function assertRangeText(expected, start, startOffset, end, endOffset) {
            editor.selectRange(start, startOffset, end, endOffset);
            if (expected instanceof RegExp) {
                expect(editor.getSelectionText()).toMatch(expected);
            }
            else {
                expect(editor.getSelectionText()).toBe(expected);
            }
        }
        var body = editor.frame;
        while (body.childNodes.length > 0) {
            body.removeChild(body.lastChild);
        }
        body.appendChild(fragment(d,
            _element("p",
                "The", " quick", " brown",
                _element("b", " fox", " jumps", " over"),
                " the", " lazy", " dog."),
            _element("p", "Brick ", "quiz ", "whangs ", "jumpy ", "veldt ", "fox.")));

        var paragraph1 = body.childNodes[0];
        var paragraph2 = body.childNodes[1];
        var bold = paragraph1.childNodes[3];
        assertRangeText("The", paragraph1.childNodes[0], 0, paragraph1.childNodes[0], 3);
        assertRangeText("he", paragraph1.childNodes[0], 1, paragraph1.childNodes[0], 3);
        assertRangeText("e quick brow", paragraph1.childNodes[0], 2, paragraph1.childNodes[2], 5);
        assertRangeText("ick brown", paragraph1.childNodes[1], 3, paragraph1.childNodes[2], 6);
        assertRangeText("ick brown fox j", paragraph1.childNodes[1], 3, bold.childNodes[1], 2);
        assertRangeText("ver the laz", bold.childNodes[2], 2, paragraph1.childNodes[5], 4);
        assertRangeText(" the lazy", paragraph1.childNodes[4], 0, paragraph1.childNodes[5], 5);
        assertRangeText("lazy dog.", paragraph1.childNodes[5], 1, paragraph1.childNodes[6], 5);
        assertRangeText(/^fox jumps over the lazy dog\.[\r\n]*Brick quiz whangs$/,
            bold.childNodes[0], 1, paragraph2.childNodes[2], 6);
        assertRangeText(" fox jumps over", paragraph1, 3, paragraph1, 4);
        assertRangeText(" dog.", paragraph1, 6, paragraph1, 7);
        assertRangeText("", paragraph1, 7, paragraph1, 7);
        assertRangeText("quick brown fox jumps over", paragraph1.childNodes[1], 1, paragraph1, 4);
        assertRangeText(" fox jumps over t", paragraph1, 3, paragraph1.childNodes[4], 2);
    });

    it("Collapsible area", function() {
        var dom = fragment(
            element("div", { "class": "collapsible" },
                element("p", { "class": "title" }, "My content")),
            element("p", br()));
        generateFragment(dom, [
            "!*",
            "My content",
            "*!"].join("\n"));
        generateWikitext(dom, [
            "!*** My content",
            "",
            "*!"].join("\n"));
    });

    it("Collapsible area with only title", function() {
        var dom = fragment(
            element("div", { "class": "collapsible" },
                element("p", { "class": "title" }, "My content"),
                element("p", br())),
            element("p", br()));
        generateFragment(dom, [
            "!* My content",
            "",
            "*!"].join("\n"));
        generateWikitext(dom, [
            "!*** My content",
            "",
            "*!"].join("\n"));
    });

    it("Collapsible area styles", function() {
        var dom = fragment(
            element("div", { "class": "collapsible" },
                element("p", { "class": "title" }, "EXPANDED"),
                element("p", br()),
                element("p", "Expanded content")),
            element("p", br()),
            element("div", { "class": "collapsible closed" },
                element("p", { "class": "title" }, "COLLAPSED"),
                element("p", br()),
                element("p", "Collapsed content")),
            element("p", br()),
            element("div", { "class": "collapsible hidden" },
                element("p", { "class": "title" }, "HIDDEN"),
                element("p", br()),
                element("p", "Hidden content")),
            element("p", br()));
        generate(dom, [
            "!*** EXPANDED",
            "",
            "Expanded content",
            "",
            "*!",
            "!***> COLLAPSED",
            "",
            "Collapsed content",
            "",
            "*!",
            "!***< HIDDEN",
            "",
            "Hidden content",
            "",
            "*!"].join("\n"));
    });

    it("Nested collapsible area", function() {
        var dom = fragment(
            element("p", "Paragraph"),
            element("div", { "class": "collapsible" },
                element("p", { "class": "title" }, "outer"),
                element("p", "Text"),
                element("div", { "class": "collapsible" },
                    element("p", { "class": "title" }, "inner"),
                    element("p", "More text")
                ),
                element("p", br())),
            element("p", br()));
        generateFragment(dom, [
            "Paragraph",
            "!*** outer",
            "Text",
            "!*** inner",
            "More text",
            "*!",
            "*!"].join("\n"));
        generateWikitext(dom, [
            "Paragraph",
            "",
            "!*** outer",
            "",
            "Text",
            "",
            "!*** inner",
            "",
            "More text",
            "",
            "*!",
            "*!"].join("\n"));
    });

    it("Collapsible area with table", function() {
        var dom = fragment(
            element("div", { "class": "collapsible" },
                element("p", { "class": "title" }, "title"),
                element("p", "Text"),
                element("table",
                    element('tbody',
                        element('tr',
                            element('td', ' table '),
                            element('td', ' row ')))),
                element("p", "More text")
            ),
            element("p", br()));
        generateFragment(dom, [
            "!*** title",
            "Text",
            "| table | row |",
            "More text",
            "*!"].join("\n"));
        generateWikitext(dom, [
            "!*** title",
            "",
            "Text",
            "",
            "| table | row |",
            "",
            "More text",
            "",
            "*!"].join("\n"));
    });

    it("Collapsible area with header", function() {
        var dom = fragment(
            element("div", { "class": "collapsible" },
                element("p", { "class": "title" }, "title"),
                element("p", br()),
                element("h2", "Header"),
                element("p", "More text")
            ),
            element("p", br()));
        generateFragment(dom, [
            "!*** title",
            "!2 Header",
            "More text",
            "*!"].join("\n"));
        generateWikitext(dom, [
            "!*** title",
            "",
            "!2 Header",
            "More text",
            "",
            "*!"].join("\n"));
    });

    it("Collapsible area with list", function() {
        var dom = fragment(
            element("div", { "class": "collapsible" },
                element("p", { "class": "title" }, "title"),
                element("p", "Text"),
                element("ul",
                    element("li", "item 1"),
                    element("li", "item 2")),
                element("p", "More text")
            ),
            element("p", br()));
        generateFragment(dom, [
            "!*** title",
            "Text",
            " * item 1",
            " * item 2",
            "More text",
            "*!"].join("\n"));
        generateWikitext(dom, [
            "!*** title",
            "",
            "Text",
            "",
            " * item 1",
            " * item 2",
            "",
            "More text",
            "",
            "*!"].join("\n"));
    });

    it("table with escaped content", function() {
        var dom = fragment(
            element("table",
                element("tbody",
                    element("tr",
                        element("td", "sql")
                    ), element("tr",
                        element("td",
                            element("tt", { "class": "escape" }, " SELECT *", br(), "     FROM bar", br()), br()
                        )
                    )
                )
            ));
        generateFragment(dom, [
            "|sql|",
            "|!- SELECT *",
            "     FROM bar",
            "-!|",
            ""].join("\n"));
        generateWikitext(dom, "| sql |\n| !- SELECT *\n     FROM bar\n-! |");
    });

    it("table with preformatted, escaped content", function() {
        var dom = fragment(
            element("table",
                element("tbody",
                    element("tr",
                        element("td", "sql")
                    ), element("tr",
                        element("td",
                            element("pre",
                                element("tt", { "class": "escape" }, " SELECT *", br(), "     FROM bar", br())
                            )
                        )
                    )
                )
            ));
        generateFragment(dom, [
            "|sql|",
            "|{{{!- SELECT *",
            "     FROM bar",
            "-!}}}|",
            ""].join("\n"));
    });

    it("table with code block", function() {
        var dom = fragment(
            element("table",
                element("tbody",
                    element("tr",
                        element("td", " table "),
                        element("td", " ",
                            element("pre", "<root>", br(),
                                "<a />", br(),
                                "<b>test</b>", br(),
                                "</root>"), " "
                        )
                    )
                )
            ));
        generateFragment(dom, [
            "| table | {{{<root>",
            "<a />",
            "<b>test</b>",
            "</root>}}} |",
            ""].join("\n"));
    });


    it("renders images", function () {
        var dom = fragment(
            element("p", "blah ",
                element("img", { src: "./files/some/path"})
            ));

        generateFragment(dom, "blah !img http://files/some/path   \n");
    });

    it("renders images with parameters", function () {
        var dom = fragment(
            element("p",
                element("img", { src: "./files/some/path", style: "width: 200px; border: 2px; margin: 10px;"}),
                br()
            ));

        generateFragment(dom, "!img -w 200 -b 2 -m 10 http://files/some/path");
    });

    it("parses images", function () {
        var dom = fragment(
            element("p",
                element("img", { src: "./files/some/path", style: "width: 200px; border: 2px; margin: 10px;"})
            ));

        generateWikitext(dom, "!img -b 2 -m 10 -w 200 http://files/some/path");
    });


});