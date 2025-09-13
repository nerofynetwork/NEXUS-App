function addInstance(id,name,icon,group) {

    id = decodeURIComponent(id);
    name = decodeURIComponent(name);
    icon = decodeURIComponent(icon);
    group = decodeURIComponent(group);

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
                    button.querySelector("img").src = icon;
                    button.querySelector("img").display = "";
                    button.querySelector("i").remove();
                } else {
                    button.querySelector("i").className = "bi bi-dice-"+(Math.floor(Math.random() * 6) + 1);
                    if(Math.random() < 0.5) {
                        button.querySelector("i").className = button.querySelector("i").className + "-fill";
                    }
                    button.querySelector("img").remove();
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

function showInstance(id,name,version,summary,description,tagsString) {
    if(!document.getElementById("update-button").classList.contains("d-none")) {
        document.getElementById("update-button").classList.add("d-none");
    }
    document.getElementById("library-title").querySelector("span").classList.remove("icon");
    document.getElementById("library-title").querySelector("img").src = "";
    id = decodeURIComponent(id);
    name = decodeURIComponent(name);
    version = decodeURIComponent(version);
    summary = decodeURIComponent(summary);
    description = decodeURIComponent(description);

    if(activeInstance) {
        if(document.getElementById(activeInstance)) {
            document.getElementById(activeInstance).classList.remove("active");
        }
    }

    activeInstance = id;
    if(document.getElementById(activeInstance)) {
        document.getElementById(activeInstance).classList.add("active");
    }
    document.getElementById("library-title").querySelector("span").innerText = name;

        if(document.getElementById(id)&&document.getElementById(id).querySelector("img")&&document.getElementById(id).querySelector("img").src) {
            document.getElementById("library-title").querySelector("img").src = document.getElementById(id).querySelector("img").src;
            document.getElementById("library-title").querySelector("span").classList.add("icon");
        }

    document.getElementById("instance-view").style.display = "flex";
    document.getElementById("instance-name").innerText = name;
    document.getElementById("instance-version").innerText = version;
    document.getElementById("instance-summary").innerText = summary;
    document.getElementById("tab-about-content").innerHTML = marked.parse(description);
    openLinksInNewTab(document.getElementById("tab-about-content"));

    document.getElementById("launch-button").innerHTML = "<i class=\"bi bi-rocket-takeoff\"></i> LAUNCH";
    document.getElementById("launch-button").onclick = function () {
        console.log('[CONNECTOR] library.start.'+activeInstance); document.getElementById("launch-button").onclick = function () {}
        document.getElementById("launch-button").innerText = "LAUNCHED"; document.getElementById("launch-button").classList.add("disabled");
    }

    document.getElementById("library-tags").innerHTML = "";
    const tags = tagsString.split(", ");
    for(let i = 0; i < tags.length; i++) {
        let tag = tags[i];
        if(tag.startsWith("minecraft-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-black'>Minecraft " + tag.replaceAll("minecraft-", "") + "</span>";
        } else if(tag.startsWith("fabric-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-info text-black'>Fabric " + tag.replaceAll("fabric-", "") + "</span>";
        } else if(tag.startsWith("forge-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-info text-black'>Forge " + tag.replaceAll("forge-", "") + "</span>";
        } else if(tag.startsWith("neoforge-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-info text-black'>NeoForge " + tag.replaceAll("neoforge-", "") + "</span>";
        } else if(tag.startsWith("quilt-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-info text-black'>Quilt " + tag.replaceAll("quilt-", "") + "</span>";
        } else if(!tag.startsWith("modloader-")&&!tag.startsWith("modloder-")) {
            document.getElementById("library-tags").innerHTML += "<span class='badge bg-white text-black'>"+tag+"</span>";
        }
    }

    document.getElementById("update-button").onclick = function () {
        console.log('[CONNECTOR] library.update.'+activeInstance);
    }
    document.getElementById("folder-button").onclick = function () {
        console.log('[CONNECTOR] library.folder.'+activeInstance);
    }
    document.getElementById("settings-button").onclick = function () {
        console.log('[CONNECTOR] library.settings.'+activeInstance);
    }
}

function openLinksInNewTab(containerElement) {
    if (!containerElement) {
        return;
    }

    const links = containerElement.querySelectorAll('a');
    links.forEach(link => {
        link.target = '_blank';
        link.rel = 'noopener noreferrer';
    });
}