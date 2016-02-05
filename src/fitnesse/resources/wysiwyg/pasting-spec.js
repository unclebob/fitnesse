
describe("Copying and pasting", function () {

    var editor, contentDocument, contentBody;

    beforeEach(function() {
        this.addMatchers({
            // Sometimes Firefox and Chrome react differently
            toBeEither: function(expected1, expected2) {
                var actual = this.actual;
                return actual === expected1 || actual === expected2;
            }
        });
    });

    beforeEach(function() {
        document.getElementById("editor").innerHTML = '<textarea class="wikitext no_wrap" id="pageContent" name="pageContent" wrap="off"></textarea>';
        jasmine.Clock.useMock();

        Wysiwyg.paths = { base: ".", stylesheets: ["../css/fitnesse_wiki.css", "editor.css"] };
        var options = Wysiwyg.getOptions();
        editor = Wysiwyg.newInstance(document.getElementById("pageContent"), options);
        jasmine.Clock.tick(1000);

        contentDocument = editor.contentDocument;
        contentBody = contentDocument.getElementsByTagName("body")[0];

        // Ensure the wysiwyg editor is visible
        $('#editor-wysiwyg-1').click();
    });

    function clearContent() {
        while (contentBody.firstChild) {
            contentBody.removeChild(contentBody.firstChild);
        }
    }

    function givenHtml(html) {
        contentBody.innerHTML = html;
    }

    function select(startQuery, startOffset, endQuery, endOffset) {
        var startContainer = contentDocument.querySelector(startQuery);
        var endContainer = contentDocument.querySelector(endQuery);
        editor.selectRange(startContainer.firstChild, startOffset, endContainer.firstChild, endOffset);
    }

    function paste(something) {
        $(contentBody).trigger("paste");
        editor.insertHTML(something);
        jasmine.Clock.tick(20);
    }

    it("should inset a single word before", function () {
        givenHtml("<p>page contains text</p>");
        select("p", 0, "p", 0);
        paste("test-");

        expect(contentBody.innerHTML).toBe("<p>test-page contains text</p>");

    });

    it("should insert paragraph before current content", function () {
        givenHtml("<p>page contains text</p>");
        select("p", 0, "p", 0);
        paste("<p>test-</p>");

        expect(contentBody.innerHTML).toBeEither(
            "<p>test-page contains text</p>",
            "<p>test-</p><p>page contains text</p>");

    });

    it("should replace text when selected", function () {
        givenHtml("<p>page contains text</p>");
        select("p", 0, "p", 4);
        paste("test");

        expect(contentBody.innerHTML).toBe("<p>test contains text</p>");
    });

    it("should replace all content if selected", function () {
        givenHtml("<p>Page contains text</p><p>Another paragraph</p>");
        select("p", 0, "p", 18);
        paste("blah");

        expect(contentBody.innerHTML).toBe("<p>blah</p><p>Another paragraph</p>");
    });

    it("should merge two paragraphs if text is selected over paragraph boundries", function () {
        givenHtml('<p id="first">first</p><p id="second">second</p>');
        select("#first", 1, "#second", 5);
        paste("oo");

        expect(contentBody.innerHTML).toBeEither(
            '<p id="first">food</p>',
            '<p id="second">food</p>');
    });

    describe("Pasting in tables", function () {
        it("should paste only one line of content (plain text)", function () {
            givenHtml('<table><tbody><tr><td id="first">cell content</td></tr></tbody></table>');
            select("#first", 0, "#first", 4);
            paste("new");

            expect(contentBody.innerHTML).toBe('<table><tbody><tr><td id="first">new content</td></tr></tbody></table>');
        });

        it("should paste only one line of content (markup text)", function () {
            givenHtml('<table><tbody><tr><td id="first">cell content</td></tr></tbody></table>');
            select("#first", 0, "#first", 4);
            paste("<p><b>new</b></p>");

            expect(contentBody.innerHTML).toBeEither(
                '<br><table><tbody><tr><td id="first"><b>new</b> content</td></tr></tbody></table>',
                '<table><tbody><tr><td id="first"><b>new</b>   content</td></tr></tbody></table>');
        });

        it("should add pasted tables to the parent table", function () {
            givenHtml('<table><tbody><tr><td id="first">cell content</td></tr></tbody></table>');
            select('#first', 0, '#first', 0);
            paste('<table><tbody><tr><td id="nested">nested content</td></tr></tbody></table>');

            expect(contentBody.innerHTML).toBe(
                '<table><tbody><tr><td id="first">cell content</td></tr><tr><td id="nested">nested content</td></tr></tbody></table>');
        });

    });

    describe("Pasting wiki text", function () {
        it("should format multi-line text", function () {
            givenHtml("<p>page contains text</p>");
            select("p", 0, "p", 4);
            paste("test\n\n");

            expect(contentBody.innerHTML).toBeEither(
                '<p>test</p><p> contains text</p>',
                '<p>test </p><p>contains text</p>');
        });

        it("should format multi-line text formatted with br's (firefox)", function () {
            givenHtml("<p>page contains text</p>");
            select("p", 0, "p", 4);
            paste("test<br/><br/>");

            expect(contentBody.innerHTML).toBeEither(
                '<p>test</p><p> contains text</p>',
                'test<br><br><p> contains text</p>');
        });

        it("should format as rich text", function () {
            givenHtml("<p>page contains text</p>");
            select("p", 0, "p", 4);
            paste("''test''");

            expect(contentBody.innerHTML).toBe("<p><i>test</i> contains text</p>");
        });

        it("should format as rich text", function () {
            givenHtml("<p>page contains text</p>");
            select("p", 0, "p", 4);
            paste("''test'' content\n\n'''test'''");

            expect(contentBody.innerHTML).toBe('<p><i>test</i> content</p><p><b>test</b> contains text</p>');
        });

        it("should construct tables", function () {
            givenHtml("<p>page contains text</p>");
            select("p", 0, "p", 4);
            paste("| cell content |");

            expect(contentBody.innerHTML).toBe('<table><tbody><tr><td> cell content </td><td> contains text</td></tr></tbody></table>');
        });

        it("should add table if table markup is pasted", function () {
            givenHtml("<p>page contains text</p>");
            select("p", 0, "p", 0);
            paste("| cell content |\n" +
                  "| more content |\n");

            expect(contentBody.innerHTML).toBe('<table><tbody><tr><td> cell content </td></tr><tr><td> more content </td></tr></tbody></table><p>page contains text</p>');
        });

    });


});