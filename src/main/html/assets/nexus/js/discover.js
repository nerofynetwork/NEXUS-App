document.getElementById('discover').querySelector('li.nav-item').innerHTML = version;
resetResults();

function initDiscoverPanel() {
    document.querySelector(".menu-panel").querySelector(".card-body").innerHTML = "<i onclick='window.open(`https://discord.gg/Awwh6JrJBS`,`_blank`);' class='bi bi-discord'></i><i onclick='window.open(`https://github.com/zyneonstudios/nexus-app`,`_blank`);' class='bi bi-github'></i><i onclick='window.open(`https://nexus.zyneonstudios.org/app`,`_blank`);' class='bi bi-globe'></i><i onclick='console.log(`[CONNECTOR] exit`)' class='bi bi-door-open'></i>";
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
        result.querySelector(".result-name").innerText = name;
        result.querySelector(".result-authors").innerText = authors;
        result.querySelector(".result-summary").innerText = summary;
        result.querySelector(".result-source").innerText = "@"+source;
        result.querySelector(".result-downloads").innerText = downloads;
        result.querySelector(".result-followers").innerText = followers;
        result.classList.add(source.toLowerCase());
        template.parentNode.insertBefore(result, template);
    }
}

function resetResults() {
    document.querySelector(".results-container").innerHTML = document.querySelector(".template-container").innerHTML;
}