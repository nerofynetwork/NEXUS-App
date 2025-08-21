package com.zyneonstudios.nexus.application.search;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.application.search.curseforge.search.CurseForgeSearch;
import com.zyneonstudios.nexus.application.search.curseforge.search.facets.CurseForgeFacetsBuilder;
import com.zyneonstudios.nexus.application.search.modrinth.search.ModrinthSearch;
import com.zyneonstudios.nexus.application.search.modrinth.search.facets.ModrinthFacetsBuilder;
import com.zyneonstudios.nexus.application.search.modrinth.search.facets.ModrinthProjectType;
import com.zyneonstudios.nexus.application.search.zyndex.ZyndexSearch;
import com.zyneonstudios.nexus.index.ReadableZyndex;
import com.zyneonstudios.nexus.instance.Instance;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CombinedSearch {

    private final ZyndexSearch NEXSearch;
    private final CurseForgeSearch curseForgeSearch;
    private final ModrinthSearch modrinthSearch;

    private int offset = 0;
    private int hits = 20;

    public CombinedSearch() {
        NEXSearch = new ZyndexSearch(new ReadableZyndex("https://zyneonstudios.github.io/nexus-nex/zyndex/index.json"));
        curseForgeSearch = new CurseForgeSearch();
        modrinthSearch = new ModrinthSearch();

        curseForgeSearch.setFacets(new CurseForgeFacetsBuilder().withClassId(4471).build());
        modrinthSearch.setFacets(new ModrinthFacetsBuilder().withProjectType(ModrinthProjectType.modpack).build());
    }

    public void setLimit(int limit) {
        this.hits = limit;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public CurseForgeSearch getCurseForgeSearch() {
        return curseForgeSearch;
    }

    public ModrinthSearch getModrinthSearch() {
        return modrinthSearch;
    }

    public ZyndexSearch getNEXSearch() {
        return NEXSearch;
    }

    public int getLimit() {
        return hits;
    }

    public int getOffset() {
        return offset;
    }

    public JsonArray search(String query) {
        curseForgeSearch.setLimit(hits);
        curseForgeSearch.setOffset(offset*hits);
        modrinthSearch.setLimit(hits);
        modrinthSearch.setOffset(offset*hits);

        JsonArray results = new JsonArray();
        curseForgeSearch.setQuery(encodeData(query));
        modrinthSearch.setQuery(encodeData(query));
        ArrayList<Instance> nexResults;
        JsonObject modrinthResults;
        JsonObject curseForgeResults;

        if(NexusApplication.getInstance().getLocalSettings().isDiscoverSearchNEX()) {
            nexResults = NEXSearch.search(query);
        } else {
            nexResults = new ArrayList<>();
        }

        if(NexusApplication.getInstance().getLocalSettings().isDiscoverSearchCurseForge()) {
            curseForgeResults = curseForgeSearch.search();
        } else {
            curseForgeResults = null;
        }

        if(NexusApplication.getInstance().getLocalSettings().isDiscoverSearchModrinth()) {
            modrinthResults = modrinthSearch.search();
        } else {
            modrinthResults = null;
        }

        try {
            List<JsonObject> nexJsonResults = new ArrayList<>();
            for (Instance instance : nexResults) {
                JsonObject result = new JsonObject();
                result.addProperty("id", encodeData(instance.getId()));
                result.addProperty("iconUrl", instance.getThumbnailUrl());
                result.addProperty("name", encodeData(instance.getName()));
                result.addProperty("downloads", "hidden");
                result.addProperty("followers", "hidden");
                JsonArray authors = new JsonArray();
                for (String author : instance.getAuthors()) {
                    authors.add(encodeData(author));
                }
                result.add("authors", authors);
                result.addProperty("summary", encodeData(instance.getSummary()));
                result.addProperty("url", "hidden");
                result.addProperty("source", "NEX");
                result.addProperty("connector", "install.minecraft.nex."+instance.getId());
                nexJsonResults.add(result);
            }

            List<JsonObject> curseForgeJsonResults = new ArrayList<>();
            if (curseForgeResults != null) {
                for (JsonElement hit : curseForgeResults.getAsJsonArray("data")) {
                    JsonObject curseforgeResult = hit.getAsJsonObject();
                    JsonObject result = new JsonObject();
                    result.addProperty("id", encodeData(curseforgeResult.get("id").getAsString()));
                    if (curseforgeResult.get("logo").isJsonObject()) {
                        result.addProperty("iconUrl", curseforgeResult.get("logo").getAsJsonObject().get("url").getAsString());
                    }
                    result.addProperty("name", encodeData(curseforgeResult.get("name").getAsString()));
                    result.addProperty("downloads", curseforgeResult.get("downloadCount").getAsString());
                    result.addProperty("followers", "hidden");
                    JsonArray authors = new JsonArray();
                    for (JsonElement author : curseforgeResult.get("authors").getAsJsonArray()) {
                        authors.add(encodeData(author.getAsJsonObject().get("name").getAsString()));
                    }
                    result.add("authors", authors);
                    result.addProperty("summary", encodeData(curseforgeResult.get("summary").getAsString()));
                    result.addProperty("url", curseforgeResult.get("links").getAsJsonObject().get("websiteUrl").getAsString());
                    result.addProperty("source", "CurseForge");
                    result.addProperty("connector", encodeData("install.minecraft.curseforge."+curseforgeResult.get("id").getAsString()));
                    curseForgeJsonResults.add(result);
                }
            }

            List<JsonObject> modrinthJsonResults = new ArrayList<>();
            if (modrinthResults != null) {
                for (JsonElement hit : modrinthResults.getAsJsonArray("hits")) {
                    JsonObject modrinthResult = hit.getAsJsonObject();
                    JsonObject result = new JsonObject();
                    result.addProperty("id", encodeData(modrinthResult.get("project_id").getAsString()));
                    result.addProperty("iconUrl", modrinthResult.get("icon_url").getAsString());
                    result.addProperty("name", encodeData(modrinthResult.get("title").getAsString()));
                    result.addProperty("downloads", modrinthResult.get("downloads").getAsString());
                    result.addProperty("followers", modrinthResult.get("follows").getAsString());
                    JsonArray authors = new JsonArray();
                    authors.add(encodeData(modrinthResult.get("author").getAsString()));
                    result.add("authors", authors);
                    result.addProperty("summary", encodeData(modrinthResult.get("description").getAsString()));
                    result.addProperty("url", "https://modrinth.com/modpack/" + modrinthResult.get("slug").getAsString());
                    result.addProperty("source", "Modrinth");
                    result.addProperty("connector", encodeData("install.minecraft.modrinth."+modrinthResult.get("project_id").getAsString()));
                    modrinthJsonResults.add(result);
                }
            }

        int i = 0;
        while (i < nexJsonResults.size() || i < modrinthJsonResults.size() || i < curseForgeJsonResults.size()) {
            if (i < nexJsonResults.size()) {
                results.add(nexJsonResults.get(i));
            }
            if (i < curseForgeJsonResults.size()) {
                results.add(curseForgeJsonResults.get(i));
            }
            if (i < modrinthJsonResults.size()) {
                results.add(modrinthJsonResults.get(i));
            }
            i++;
        }

        } catch (Exception e) {
            NexusApplication.getLogger().printErr("NEXUS","COMBINED SEARCH","Couldn't process the search results...",e.getMessage(), e.getStackTrace());
        }
        return results;
    }

    private String encodeData(String searchTerm) {
        try {
            return URLEncoder.encode(searchTerm, StandardCharsets.UTF_8).replace("+", "%20");
        } catch (Exception e) {
            return searchTerm.replace(" ","%20");
        }
    }
}