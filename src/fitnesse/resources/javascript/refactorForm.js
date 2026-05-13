const selectedLocation = document.getElementById("selectedLocation");

async function loadChildren(path, parentElement) {
	const loading = document.createElement("div");
    loading.className = "loading";
    loading.textContent = "Loading...";
    parentElement.appendChild(loading);
	
    const response = await fetch(
        "?responder=lazyPageTree&path=" + encodeURIComponent(path)
    );

    const nodes = await response.json();
	
	parentElement.removeChild(loading);

    const ul = document.createElement("ul");
	ul.classList.add("tree-child");

    nodes.forEach(node => {
        const li = document.createElement("li");
        li.dataset.path = node.path;
        
		let toggle = null;
		if (node.hasChildren) {
			toggle = createToggleIcon();
			addToggleOnClickListener(toggle, li, node.path);
		} else {
			toggle = document.createElement("span");
			toggle.className = "tree-toggle";
		}

		const label = document.createElement("span");
		label.className = "tree-label";
        label.textContent = node.name;

        label.onclick = function(e) {
            e.stopPropagation();
            selectNode(node.path);
        };

		li.appendChild(toggle);
        li.appendChild(label);

        ul.appendChild(li);
    });

    parentElement.appendChild(ul);
}

function addToggleOnClickListener(toggle, li, path) {
	// Adding the functionality to load the children for the node when the toggle is clicked
	toggle.onclick = async function(e) {
		e.stopPropagation();

		const expanded = toggle.classList.toggle("expanded");

		if (expanded) {
			if (!li.dataset.loaded) {
				await loadChildren(path, li);
				li.dataset.loaded = true;
			}

			li.querySelector("ul").style.display = "block";
		} else {
			li.querySelector("ul").style.display = "none";
		}
	};
}

function selectNode(path) {
	const selected = document.querySelector(`[data-path="${path}"] .tree-label`);
	const alreadySelected = selected.classList.contains("selected");
	
	document.querySelectorAll(".tree-label").forEach(label =>
        label.classList.remove("selected")
    );
	
    // Select the node if it was not already selected, otherwise deselect it
	if (selected && !alreadySelected) {
		document.getElementById("newLocation").value = path;
		selected.classList.add("selected");
		selectedLocation.textContent = path;
	} else {
		document.getElementById("newLocation").value = "";
		selected.classList.remove("selected");
		selectedLocation.textContent = "";
	}
}

function createToggleIcon() {
	const span = document.createElement("span");
	span.className = "tree-toggle";

	span.innerHTML = `
        <svg class="tree-icon" viewBox="0 0 16 16" width="12" height="12">
            <path d="M4 2 L12 8 L4 14 Z"></path>
        </svg>
    `;

    return span;
}

/**
 * The root node is created separately since it is should always be expanded and only selectable when not the root page.
 */
function createRootNode(path) {
	const li = document.createElement("li");
	li.dataset.path = path;
	
	const label = document.createElement("span");
	label.className = "tree-label";
    label.textContent = path;
    // We only allow the selection of non-root nodes
    if ("ROOT" !== path) {
	    label.onclick = function(e) {
	        e.stopPropagation();
	        selectNode(path);
	    };
    }
    
    li.appendChild(label);
    
    return li;
}

document.addEventListener("DOMContentLoaded", async function () {
    const tree = document.getElementById("pageTree");
    const path = document.getElementById("configPath").dataset.path;
    
    const rootNode = createRootNode(path);
    tree.appendChild(rootNode);
    
	await loadChildren(path, rootNode);
	
	rootNode.dataset.loaded = true;
});
