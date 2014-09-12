/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.service.resources;

import org.triple_brain.module.model.User;

public interface SearchResourceFactory {
    public SearchResource withUser(User user);
}
