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
        return zyndex;
    }

    public ArrayList<Instance> search(String searchString) {

        ArrayList<Instance> instances = new ArrayList<>();

        for (Instance instance : zyndex.getInstances()) {
            if (instance.getName().equalsIgnoreCase(searchString) || instance.getId().equalsIgnoreCase(searchString)) {
                instances.add(instance);
            } else {
                if (instance.getId().contains(searchString) || instance.getName().contains(searchString)) {
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
