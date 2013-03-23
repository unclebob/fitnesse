
describe("text should be pasted formatted according to the limitations of the wiki text", function () {
    console.log("init editor");
    Wysiwyg.paths = { base: ".", stylesheets: [] };
    var options = Wysiwyg.getOptions();
    var instance = new Wysiwyg(document.getElementById("pageContent"), options);
    var contentDocument = instance.contentDocument;

    // Ensure the wysiwyg editor is visible
    $('#editor-wysiwyg-1').click();

    it("should format a single word inline", function () {

    })
});