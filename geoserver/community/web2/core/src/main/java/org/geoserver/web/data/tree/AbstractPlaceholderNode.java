/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.data.tree;

import java.util.Collections;
import java.util.List;

public abstract class AbstractPlaceholderNode extends AbstractCatalogNode {

    public AbstractPlaceholderNode(String id, AbstractCatalogNode parent) {
        super(id, parent);
    }

    // override equals and hash code so that they don't clash with the ones in
    // DataStoreInfo node
    public boolean equals(Object obj) {
        return this == obj;
    }

    public int hashCode() {
        return 17 + 3 * getModel().hashCode();
    }

    @Override
    protected List<AbstractCatalogNode> buildChildNodes() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean isSelectable() {
        return false;
    }

}
