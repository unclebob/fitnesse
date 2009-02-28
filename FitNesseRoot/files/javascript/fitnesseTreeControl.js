function add_fitnesse_methods_to_YUI_textnode() {
  YAHOO.widget.TextNode.prototype.fitnesse_path = function() {
    var node_list = this.path_labels();
    return node_list.join('.');
  };
  YAHOO.widget.TextNode.prototype.path_labels = function() {
    if (!this.parent.path_labels) {
      return [this.label];
    }
    return this.parent.path_labels().concat(this.label);
  };
}

function add_nodes_to_parent(child_node_names, parent_node) {
  $(child_node_names).sort().each(function() {
    var node = new YAHOO.widget.TextNode({label:this, target:"page_frame"}, parent_node, false);
    node.href = "/" + node.fitnesse_path();
  });
}

var tree;
function tree_init(div_id) {
  add_fitnesse_methods_to_YUI_textnode();

  tree = new YAHOO.widget.TreeView(div_id);
  tree.setDynamicLoad(load_child_nodes, 1);
  $.getJSON("/root?names&format=json",
    function(json) {
      add_nodes_to_parent(json, tree.getRoot());
      tree.render();
    }
    );
  tree.subscribe("clickEvent", function(node) {
    return false;
  });
}
function load_child_nodes(node, fnLoadComplete)
{
  var callback = function(json) {
    add_nodes_to_parent(json, node);
    fnLoadComplete();
  };
  var url = "/" + node.fitnesse_path() + "?names&format=json";
  $.getJSON(url, callback);
}
