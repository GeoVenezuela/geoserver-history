package org.geoserver.web.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.geoserver.web.GeoServerBasePage;
import org.geoserver.web.data.coverage.RasterCoverageConfiguration;
import org.geoserver.web.data.datastore.DataStoreConfiguration;
import org.geoserver.web.data.tree.DataPage;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataAccessFinder;
import org.opengis.coverage.grid.Format;

/**
 * Page that presents a list of vector and raster store types available in the
 * classpath in order to choose what kind of data source to create.
 * <p>
 * Meant to be called by {@link DataPage} when about to add a new datastore or
 * coverage.
 * </p>
 * 
 * @author Gabriel Roldan
 */
public class NewDataPage extends GeoServerBasePage {

    /**
     * Creates the page components to present the list of available vector and
     * raster data source types
     * 
     * @param workspaceId
     *            the id of the workspace to attach the new resource store to.
     */
    public NewDataPage(final String workspaceId) {

        final Map<String, String> dataStoreNames = getAvailableDataStoreNames();
        final Map<String, String> coverageNames = getAvailableCoverageStoreNames();

        final ArrayList<String> sortedDsNames = new ArrayList<String>(dataStoreNames.keySet());
        Collections.sort(sortedDsNames);

        final ListView dataStoreLinks = new ListView("vectorResources", sortedDsNames) {
            @Override
            protected void populateItem(ListItem item) {
                final String dataStoreFactoryName = item.getModelObjectAsString();
                final String description = dataStoreNames.get(dataStoreFactoryName);
                Link link;
                link = new Link("resourcelink") {
                    @Override
                    public void onClick() {
                        setResponsePage(new DataStoreConfiguration(workspaceId,
                                dataStoreFactoryName));
                    }
                };
                link.add(new Label("resourcelabel", dataStoreFactoryName));
                item.add(link);
                item.add(new Label("resourceDescription", description));
            }
        };

        final List<String> sortedCoverageNames = new ArrayList<String>(coverageNames.keySet());
        Collections.sort(sortedCoverageNames);

        final ListView coverageLinks = new ListView("rasterResources", sortedCoverageNames) {
            @Override
            protected void populateItem(ListItem item) {
                final String coverageFactoryName = item.getModelObjectAsString();
                final String description = coverageNames.get(coverageFactoryName);
                Link link;
                link = new Link("resourcelink") {
                    @Override
                    public void onClick() {
                        setResponsePage(new RasterCoverageConfiguration(workspaceId, coverageFactoryName));
                    }
                };
                link.add(new Label("resourcelabel", coverageFactoryName));
                item.add(link);
                item.add(new Label("resourceDescription", description));
            }
        };

        add(dataStoreLinks);
        add(coverageLinks);
    }

    /**
     * @return the name/description set of available datastore factories
     */
    private Map<String, String> getAvailableDataStoreNames() {
        final Iterator<DataAccessFactory> availableDataStores;
        availableDataStores = DataAccessFinder.getAvailableDataStores();

        Map<String, String> storeNames = new HashMap<String, String>();

        while (availableDataStores.hasNext()) {
            DataAccessFactory factory = availableDataStores.next();
            storeNames.put(factory.getDisplayName(), factory.getDescription());
        }
        return storeNames;
    }

    /**
     * @return the name/description set of available raster formats
     */
    private Map<String, String> getAvailableCoverageStoreNames() {
        Format[] availableFormats = GridFormatFinder.getFormatArray();
        Map<String, String> formatNames = new HashMap<String, String>();
        for (Format format : availableFormats) {
            formatNames.put(format.getName(), format.getDescription());
        }
        return formatNames;
    }

}
