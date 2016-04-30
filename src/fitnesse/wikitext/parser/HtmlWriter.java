package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import java.util.Stack;

public class HtmlWriter {
    private StringBuilder html = new StringBuilder();
    private int lastEndLine = -1;
    private Tags tags = new Tags();

    public void startTag(String tag) {
        closeCurrentTag(false);
        indentTag();
        tags.push(tag, false);
        writeTag(tag);
    }

    public void startTagInline(String tag) {
        closeCurrentTag(true);
        tags.push(tag, true);
        writeTag(tag);
    }

    public void putText(String text) {
        closeCurrentTag(true);
        html.append(text);
    }

    private void writeTag(String tag) {
        html.append('<');
        html.append(tag);
    }

    private void indentTag() {
        for (int i = 0; i < tags.size(); i++) html.append('\t');
    }

    private void closeCurrentTag(boolean newTagInline) {
        if (tags.size() > 0) {
            if (tags.isOpen()) {
                html.append('>');
                tags.close();
            }
            if (!newTagInline) writeEndLine();
        }
    }

    public void endTag() {
        Tag tag = tags.pop();
        if (tag.open) {
            html.append(" />");
        }
        else {
            if (html.length() == lastEndLine) {
                indentTag();
            }
            html.append("</");
            html.append(tag.name);
            html.append('>');
        }
        if (!tag.inline) writeEndLine();
    }

    private void writeEndLine() {
        if (html.length() == lastEndLine) return;
        html.append(HtmlElement.endl);
        lastEndLine = html.length();
    }

    public void putTag(String tag) {
        startTag(tag);
        endTag();
    }

    public void putTagInline(String tag) {
        startTagInline(tag);
        endTag();
    }

    public void putAttribute(String name, String value) {
        html.append(' ');
        html.append(name);
        html.append("=\"");
        html.append(value);
        html.append('"');
    }

    public String toHtml() { return html.toString(); }

    private class Tags {
        private Stack<Tag> tags = new Stack<>();
        private Tag top = new Tag(null, false);
        private boolean isEmpty = true;

        public void push(String name, boolean inline) {
            if (!isEmpty) {
                tags.push(top);
                top = new Tag(name, inline);
            }
            else {
                top.name = name;
                top.inline = inline;
                isEmpty = false;
            }
        }

        public Tag pop() {
            Tag result = top;
            if (tags.empty()) {
                isEmpty = true;
            }
            else {
                top = tags.pop();
            }
            return result;
        }

        public int size() { return tags.size() + (isEmpty ? 0 : 1); }

        public boolean isOpen() { return top.open; }

        public void close() { top.open = false; }
    }

    private class Tag {
        public String name;
        public boolean open;
        public boolean inline;

        public Tag(String name, boolean inline) {
            this.name = name;
            this.inline = inline;
            open = true;
        }
    }
}
