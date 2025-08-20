package com.zyneonstudios.nexus.application.search;

import com.zyneonstudios.nexus.index.ReadableZyndex;
import com.zyneonstudios.nexus.index.Zyndex;

public class ZyndexSearch {

    // ToDO - Search for a Zyndex, list all Informations about the Zyndexes
    // Name/String: list all Zyndexes wich match Name or ID
    // Only give Instances which are isHidden() == false, if ID is EXACTLY the same as the search string, return the Instance

    private final ReadableZyndex zyndex;

    public ZyndexSearch(ReadableZyndex zyndex) {
        this.zyndex = zyndex;
    }

    public ReadableZyndex getZyndex() {
        return zyndex;
    }

}
