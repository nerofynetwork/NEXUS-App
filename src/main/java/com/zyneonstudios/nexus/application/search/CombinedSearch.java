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
    private final ModrinthSearch modrinthSearch;
    private final CurseForgeSearch curseForgeSearch;

    private int offset = 0;
    private int hits = 20;

    public CombinedSearch() {
        NEXSearch = new ZyndexSearch(new ReadableZyndex("https://zyneonstudios.github.io/nexus-nex/zyndex/index.json"));
        modrinthSearch = new ModrinthSearch();
        curseForgeSearch = new CurseForgeSearch();

        modrinthSearch.setFacets(new ModrinthFacetsBuilder().withProjectType(ModrinthProjectType.modpack).build());
        curseForgeSearch.setFacets(new CurseForgeFacetsBuilder().withClassId(4471).build());
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
        modrinthSearch.setLimit(hits);
        modrinthSearch.setOffset(offset*hits);
        curseForgeSearch.setLimit(hits);
        curseForgeSearch.setOffset(offset*hits);

        JsonArray results = new JsonArray();
        modrinthSearch.setQuery(encodeSearchTerm(query));
        curseForgeSearch.setQuery(encodeSearchTerm(query));
        ArrayList<Instance> nexResults = NEXSearch.search(query);
        JsonObject modrinthResults = modrinthSearch.search();
        JsonObject curseForgeResults = curseForgeSearch.search();

        try {
            List<JsonObject> nexJsonResults = new ArrayList<>();
            for (Instance instance : nexResults) {
                JsonObject result = new JsonObject();
                result.addProperty("id", instance.getId());
                result.addProperty("iconUrl", instance.getIconUrl());
                result.addProperty("name", instance.getName());
                result.addProperty("downloads", "hidden");
                result.addProperty("followers", "hidden");
                JsonArray authors = new JsonArray();
                for (String author : instance.getAuthors()) {
                    authors.add(author);
                }
                result.add("authors", authors);
                result.addProperty("summary", instance.getDescription());
                result.addProperty("url", "hidden");
                result.addProperty("source", "NEX");
                result.addProperty("connector", "install.minecraft.nex.");
                nexJsonResults.add(result);
            }

            List<JsonObject> modrinthJsonResults = new ArrayList<>();
            if (modrinthResults != null) {
                for (JsonElement hit : modrinthResults.getAsJsonArray("hits")) {
                    JsonObject modrinthResult = hit.getAsJsonObject();
                    JsonObject result = new JsonObject();
                    result.addProperty("id", modrinthResult.get("project_id").getAsString());
                    result.addProperty("iconUrl", modrinthResult.get("icon_url").getAsString());
                    result.addProperty("name", modrinthResult.get("title").getAsString());
                    result.addProperty("downloads", modrinthResult.get("downloads").getAsString());
                    String f = "0";
                    try {
                        f = modrinthResult.get("follows").getAsString();
                    } catch (Exception e) {
                        NexusApplication.getLogger().err(e.getMessage());
                        f = "hidden";
                    }
                    result.addProperty("followers", f);
                    JsonArray authors = new JsonArray();
                    authors.add(modrinthResult.get("author").getAsString());
                    result.add("authors", authors);
                    result.addProperty("summary", modrinthResult.get("description").getAsString());
                    result.addProperty("url", "https://modrinth.com/modpack/" + modrinthResult.get("slug").getAsString());
                    result.addProperty("source", "Modrinth");
                    result.addProperty("connector", "install.minecraft.modrinth.");
                    modrinthJsonResults.add(result);
                }
            }

            List<JsonObject> curseForgeJsonResults = new ArrayList<>();
            if (curseForgeResults != null) {
                for (JsonElement hit : curseForgeResults.getAsJsonArray("data")) {
                    JsonObject curseforgeResult = hit.getAsJsonObject();
                    JsonObject result = new JsonObject();
                    result.addProperty("id", curseforgeResult.get("id").getAsString());
                    if (curseforgeResult.get("logo").isJsonObject()) {
                        result.addProperty("iconUrl", curseforgeResult.get("logo").getAsJsonObject().get("url").getAsString());
                    }
                    result.addProperty("name", curseforgeResult.get("name").getAsString());
                    result.addProperty("downloads", curseforgeResult.get("downloadCount").getAsString());
                    result.addProperty("followers", "hidden");
                    JsonArray authors = new JsonArray();
                    for (JsonElement author : curseforgeResult.get("authors").getAsJsonArray()) {
                        authors.add(author.getAsJsonObject().get("name").getAsString());
                    }
                    result.add("authors", authors);
                    result.addProperty("summary", curseforgeResult.get("summary").getAsString());
                    result.addProperty("url", curseforgeResult.get("links").getAsJsonObject().get("websiteUrl").getAsString());
                    result.addProperty("source", "CurseForge");
                    result.addProperty("connector", "install.minecraft.curseforge.");
                    curseForgeJsonResults.add(result);
                }
            }

        int i = 0;
        while (i < nexJsonResults.size() || i < modrinthJsonResults.size() || i < curseForgeJsonResults.size()) {
            if (i < nexJsonResults.size()) {
                results.add(nexJsonResults.get(i));
            }
            if (i < modrinthJsonResults.size()) {
                results.add(modrinthJsonResults.get(i));
            }
            if (i < curseForgeJsonResults.size()) {
                results.add(curseForgeJsonResults.get(i));
            }
            i++;
        }

        } catch (Exception e) {
            NexusApplication.getLogger().printErr("NEXUS","COMBINED SEARCH","Couldn't process the search results...",e.getMessage(), e.getStackTrace());
        }
        return results;
    }

    private String encodeSearchTerm(String searchTerm) {
        try {
            String encoded = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
            return encoded.replace("+", "%20");
        } catch (Exception e) {
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }
    }
}