package com.zyneonstudios.nexus.application.search;

import com.zyneonstudios.nexus.index.ReadableZyndex;
import com.zyneonstudios.nexus.instance.Instance;

import java.util.ArrayList;

public class ZyndexSearch {

    private final ReadableZyndex zyndex;

    public ZyndexSearch(ReadableZyndex zyndex) {
        this.zyndex = zyndex;
    }

    public ReadableZyndex getZyndex() {
        search("suche");
        return zyndex;
    }

    public ArrayList<Instance> search(String searchString, String... tags) {
        ArrayList<Instance> instances = new ArrayList<>();

        for (Instance instance : zyndex.getInstances()) {

            if (tags != null && tags.length > 0) {
                boolean tagMissmatch = false;
                for (String tag : tags) {
                    if (!instance.getTags().contains(tag.toLowerCase())) {
                        tagMissmatch = true;
                        break;
                    }
                }
                if (tagMissmatch) {
                    continue;
                }
            }

            if (instance.getId().equals(searchString)) {
                instances.add(instance);
            } else {
                String query = searchString.toLowerCase();
                if (instance.getId().toLowerCase().contains(query) || instance.getName().toLowerCase().contains(query)) {
                    if (!instance.isHidden()) {
                        instances.add(instance);
                    }
                }
            }
        }

        System.out.println("ZyndexSearch: Found " + instances.size() + " instances matching '" + searchString + "'");
        for (Instance instance : instances) {
            System.out.println("ZyndexSearch: - " + instance.getName() + " (" + instance.getId() + ")");
        }
        return instances;
    }

}
