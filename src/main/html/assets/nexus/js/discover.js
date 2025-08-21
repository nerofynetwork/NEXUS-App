document.getElementById('discover').querySelector('li.nav-item').innerHTML = version;
resetResults();

function initDiscover() {
    document.querySelector(".menu-panel").querySelector(".card-body").innerHTML = "<i onclick='window.open(`https://discord.gg/Awwh6JrJBS`,`_blank`);' class='bi bi-discord'></i><i onclick='window.open(`https://github.com/zyneonstudios/nexus-app`,`_blank`);' class='bi bi-github'></i><i onclick='window.open(`https://nexus.zyneonstudios.org/app`,`_blank`);' class='bi bi-globe'></i><i onclick='console.log(`[CONNECTOR] exit`)' class='bi bi-door-open'></i>";
    const urlParams = new URLSearchParams(window.location.search);
    console.log("[CONNECTOR] discover.search.init");
    if(urlParams.has("dt")) {
        if(urlParams.get("dt")==="search") {
            disableMenu(true);
            document.getElementById("search").classList.add("active");
            document.getElementById("search-label").querySelector("input").focus();
        }
    }
    if(urlParams.has("q")) {
        try {
            const searchInput = document.getElementById("search-label").querySelector("input");
            searchInput.value = decodeURIComponent(urlParams.get("q"));
            startSearch(0);
        } catch (e) {
            startSearch(0);
        }
    } else {
        startSearch(0);
    }
}


/**
 * Adds a search result card to the discover search panel.
 *
 * @param id The id of the result project
 * @param iconUrl The url of the result project's icon
 * @param name The name of the result project
 * @param downloads The count of downloads of the result project
 * @param followers The count of followers of the result project
 * @param authors The authors of the result project
 * @param summary The summary of the result project
 * @param url The url of the result project
 * @param source The source of the result project
 * @param connector The connector of the result project
 */

function addSearchResult(id,iconUrl,name,downloads,followers,authors,summary,url,source,connector) {
    if(!document.getElementById(id)) {
        const template = document.querySelector(".search-result-template");
        const result = template.cloneNode(true);
        result.id = id;
        result.classList.remove("search-result-template");
        result.style.display = "flex";
        result.querySelector("img").src = iconUrl;
        if(url!=="hidden") {
            result.querySelector("img").onclick = function () {
                window.open(url,`_blank`);
            };
            result.querySelector("img").classList.add("active");
        }
        result.querySelector(".result-name").innerText = decodeURIComponent(name);
        result.querySelector(".result-authors").innerText = decodeURIComponent(authors);
        result.querySelector(".result-summary").innerText = decodeURIComponent(summary);
        result.querySelector(".result-source").innerText = decodeURIComponent(source);
        result.querySelector(".result-downloads").innerText = downloads;
        if(downloads==="hidden") {
            result.querySelector(".result-downloads").parentElement.remove();
        }
        result.querySelector(".result-followers").innerText = followers;
        if(followers==="hidden") {
            result.querySelector(".result-followers").parentElement.remove();
        }
        result.querySelector(".result-url").onclick = function () {
            window.open(url,`_blank`);
        };
        if(url==="hidden") {
            result.querySelector(".result-url").remove();
        }
        result.querySelector(".result-install").onclick = function () {
            console.log("[CONNECTOR] "+decodeURIComponent(connector));
        };
        result.classList.add(source.toLowerCase());
        template.parentNode.insertBefore(result, template);
        document.getElementById("loadmore").style.display = "";
    }
}

function resetResults() {
    searchOffset = 0;
    document.querySelector(".results-container").innerHTML = document.querySelector(".template-container").innerHTML;
}

function startSearch(offset) {
    if(!offset||offset < 1) {
        offset = 0;
        resetResults();
    }
    document.getElementById('loadmore').style.display = 'none';
    console.log("[CONNECTOR] search."+offset+"."+document.getElementById('search-label').querySelector('input').value);
    window.history.pushState({}, document.title, window.location.pathname + "?page=discover.html&q=" + encodeURIComponent(document.getElementById('search-label').querySelector('input').value));
}