/* Copyright (c) 2012 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gwc.layer;

import static org.geoserver.gwc.GWCTestHelpers.mockGroup;
import static org.geoserver.gwc.GWCTestHelpers.mockLayer;
import junit.framework.TestCase;

import org.geoserver.catalog.impl.LayerGroupInfoImpl;
import org.geoserver.catalog.impl.LayerInfoImpl;
import org.geoserver.gwc.config.GWCConfig;

import com.google.common.collect.ImmutableSet;

public class LegacyTileLayerInfoLoaderTest extends TestCase {

    private GWCConfig defaults;

    private GeoServerTileLayerInfo defaultVectorInfo;

    @Override
    public void setUp() {
        defaults = GWCConfig.getOldDefaults();
        defaultVectorInfo = TileLayerInfoUtil.create(defaults);
        defaultVectorInfo.getMimeFormats().clear();
        defaultVectorInfo.getMimeFormats().addAll(defaults.getDefaultVectorCacheFormats());
    }

    public void testLoadLayerInfo() {
        LayerInfoImpl layer = mockLayer("testLayer");

        assertNull(LegacyTileLayerInfoLoader.load(layer));

        LegacyTileLayerInfoLoader.save(defaultVectorInfo, layer.getMetadata());

        GeoServerTileLayerInfo info2 = LegacyTileLayerInfoLoader.load(layer);

        defaultVectorInfo.setId(layer.getId());
        defaultVectorInfo.setName(layer.getResource().getPrefixedName());
        assertEquals(defaultVectorInfo, info2);
    }

    public void testLoadLayerInfoExtraStyles() {
        GeoServerTileLayerInfo info = defaultVectorInfo;
        info.setAutoCacheStyles(false);
        TileLayerInfoUtil.setCachedStyles(info, "default", ImmutableSet.of("style1"));

        LayerInfoImpl layer = mockLayer("testLayer", "style1", "style2");

        assertNull(LegacyTileLayerInfoLoader.load(layer));

        LegacyTileLayerInfoLoader.save(info, layer.getMetadata());

        GeoServerTileLayerInfo actual;
        actual = LegacyTileLayerInfoLoader.load(layer);

        info.setId(layer.getId());
        info.setName(layer.getResource().getPrefixedName());
        assertEquals(info, actual);

        layer.setDefaultStyle(null);
        TileLayerInfoUtil.setCachedStyles(info, null, ImmutableSet.of("style1"));
        LegacyTileLayerInfoLoader.save(info, layer.getMetadata());
        actual = LegacyTileLayerInfoLoader.load(layer);
        assertEquals(ImmutableSet.of("style1"), actual.cachedStyles());
    }

    public void testLoadLayerInfoAutoCacheStyles() {
        GeoServerTileLayerInfo info = defaultVectorInfo;
        info.setAutoCacheStyles(true);

        LayerInfoImpl layer = mockLayer("testLayer", "style1", "style2");
        assertNull(LegacyTileLayerInfoLoader.load(layer));

        LegacyTileLayerInfoLoader.save(info, layer.getMetadata());

        GeoServerTileLayerInfo actual;
        actual = LegacyTileLayerInfoLoader.load(layer);

        TileLayerInfoUtil.setCachedStyles(info, "default", ImmutableSet.of("style1", "style2"));

        info.setId(layer.getId());
        info.setName(layer.getResource().getPrefixedName());
        assertEquals(info, actual);

        layer.setDefaultStyle(null);
        TileLayerInfoUtil.setCachedStyles(info, null, ImmutableSet.of("style1", "style2"));

        actual = LegacyTileLayerInfoLoader.load(layer);
        assertEquals(ImmutableSet.of("style1", "style2"), actual.cachedStyles());
    }

    public void testLoadLayerGroup() {
        LayerGroupInfoImpl lg = mockGroup("tesGroup", mockLayer("L1"), mockLayer("L2"));

        assertNull(LegacyTileLayerInfoLoader.load(lg));
        GeoServerTileLayerInfo info = defaultVectorInfo;
        info.getMimeFormats().clear();
        info.getMimeFormats().addAll(defaults.getDefaultOtherCacheFormats());

        LegacyTileLayerInfoLoader.save(info, lg.getMetadata());

        GeoServerTileLayerInfo actual;
        actual = LegacyTileLayerInfoLoader.load(lg);

        info.setId(lg.getId());
        info.setName(lg.getName());
        assertEquals(info, actual);
    }

    public void testClear() {
        LayerGroupInfoImpl lg = mockGroup("tesGroup", mockLayer("L1"), mockLayer("L2"));

        assertNull(LegacyTileLayerInfoLoader.load(lg));
        GeoServerTileLayerInfo info = defaultVectorInfo;
        info.getMimeFormats().clear();
        info.getMimeFormats().addAll(defaults.getDefaultOtherCacheFormats());

        LegacyTileLayerInfoLoader.save(info, lg.getMetadata());

        GeoServerTileLayerInfo actual;
        actual = LegacyTileLayerInfoLoader.load(lg);
        assertNotNull(actual);

        LegacyTileLayerInfoLoader.clear(lg.getMetadata());
        assertNull(LegacyTileLayerInfoLoader.load(lg));
    }

}
