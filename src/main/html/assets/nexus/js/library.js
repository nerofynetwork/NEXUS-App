function addInstance(id,name,icon,group) {
    if(!document.getElementById(id)) {
        let list = document.getElementById("instance-list");
        if (group) {
            if (document.getElementById(group)) {
                list = document.getElementById(group);
            }
        }
        const template = list.querySelector(".instance-list-template");
        if (template) {
            const button = template.cloneNode(true);
            button.classList.remove("d-none");
            button.classList.remove("instance-list-template");

            if (id && name) {
                button.id = id;
                button.onclick = function () {
                    console.log("[CONNECTOR] library.showInstance."+id);
                }
                button.querySelector("span").innerText = name;

                if(icon) {

                } else {
                    button.querySelector("i").className = "bi bi-dice-"+(Math.floor(Math.random() * 6) + 1);
                    if(Math.random() < 0.5) {
                        button.querySelector("i").className = button.querySelector("i").className + "-fill";
                    }
                }

                template.parentElement.insertBefore(button, template);
            }
        }
    }
}

function addInstanceGroup(id,name,colorName) {
    if(!document.getElementById(id)) {
        let list = document.getElementById("instance-list");
        const template = list.querySelector(".instance-group-template");
        if (template&&id&&name) {
            const group = template.cloneNode(true);
            group.id = id;
            group.classList.remove("d-none");
            group.classList.remove("instance-group-template");
            group.querySelector(".collapse").id = id+"-collapse";
            group.querySelector("a").id = id+"-collapse-button";
            group.querySelector("a").onclick = function () {
                toggleSubMenuGroup(id+"-collapse")
            };
            group.querySelector("h6").innerText = name;
            group.querySelector("h6").onclick = function () {
                toggleSubMenuGroup(id+"-collapse")
            };
            if(colorName) {
                group.classList.add(colorName);
            }
            template.parentElement.insertBefore(group, template);
        }
    }
}

function loadFolderButtonHoverEvent() {
    const button = document.querySelector(".title-menu").querySelector(".buttons").querySelector(".folder");
    const icon = button.querySelector("i");
    button.addEventListener("mouseover", () => {
        icon.className = "bi bi-folder2-open";
    });
    button.addEventListener("mouseout", () => {
        icon.className = "bi bi-folder2";
    });
}

function initLibrary() {
    console.log("[CONNECTOR] library.init");
}

function showInstance(id,name,version,summary,description) {
    id = decodeURIComponent(id);
    name = decodeURIComponent(name);
    version = decodeURIComponent(version);
    summary = decodeURIComponent(summary);
    description = decodeURIComponent(description);

    activeInstance = id;
    document.getElementById("library-title").innerText = "Library » " + name;
    document.getElementById("instance-view").style.display = "flex";
    document.getElementById("instance-name").innerText = name;
    document.getElementById("instance-version").innerText = version;
    document.getElementById("instance-summary").innerText = summary;
    document.getElementById("tab-about-content").innerText = description;
}