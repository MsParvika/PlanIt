package com.example.planit.util;

import com.example.planit.EntityInfo;

public interface Callback {

    /**
     * Called when an "entities" API request is complete.
     *
     * @param entities The entities.
     */
    void onEntitiesReady(EntityInfo[] entities);

}