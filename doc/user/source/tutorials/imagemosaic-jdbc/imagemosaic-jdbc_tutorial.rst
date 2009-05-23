..  _imagemosaic-jdbc_tutorial:

Imagemosiac-JDBC
================

Introduction
------------

This tutorial describes the process of storing a coverage along with its pyramids in a jdbc database. The ImageMosaic JDBC plugin is authored by Christian Mueller and is part of the geotools library.

The full documentation is available here:`<http://docs.codehaus.org/display/GEOTDOC/Image+Mosaicing+Pyramidal+JDBC+Plugin>`_

This tutorial will show one possible scenario, explaining step by step what to do for using this module in GeoServer (since Version 1.7.2)

Getting Started
---------------

We use postgis/postgres as database engine, a database named "gis" and start with an image from openstreetmap. We also need an installation from `<http://www.gdal.org>`_

.. image:: start.png


Create a working directory, lets call it **working** ,download this image with a right mouse click (Image save as ...) and save it as **start_rgb.png**

Check your image with::
  
  gdalinfo start_rgb.png


This image has 4 Bands (Red,Green,Blue,Alpha) and needs much memory. As a rule, it is better to use images with a color table. We can transform with **rgb2pct** (**rgb2pct.py** on Unix).::

  rgb2pct -of png start_rgb.png start.png

Compare the sizes of the 2 files.

Afterwards, create a world file **start.wld** in the **working** directory with the following content.::

  0.0075471698
  0.0000000000
  0.0000000000
  -0.0051020408
  8.9999995849
  48.9999999796

Preparing the pyramids and the tiles
------------------------------------


If you are new to tiles and pyramids, take a quick look here `<http://star.pst.qub.ac.uk/idl/Image_Tiling.html>`_

.. note::

  Who many pyramids are needed ?

  Lets do a simple example. Given an image with 1024x1024 pixels and a tile size with 256x256 pixels.We can calculate in our brain that we need 16 tiles. Each pyramid reduces the number of tiles by a factor of 4. The first pyramid has 16/4 = 4 tiles, the second pyramid has only 4/4 = 1 tile.

  Solution: The second pyramid fits on one tile, we are finished and we need 2 pyramids.

  The formula for this:

  number of pyramids = log(pixelsize of image) / log(2) - log (pixelsize of tile) / log(2).

  Try it: Go to Google and enter as search term "log(1024)/log(2) - log(256)/log(2)" and look at the result.

  If your image is 16384 pixels , and your tile size is 512 pixels, it is

  log(16384)/log(2) - log(512)/log(2) = 5

  If your image is 18000 pixels, the result = 5.13570929. Thake the floor and use 5 pyramids. Remember, the last pyramid reduces 4 tiles to 1 tile, so this pyramid is not important.

  If your image is 18000x12000 pixel, use the bigger dimension (18000) for the formula.


For creating pyramids and tiles, use `<http://www.gdal.org/gdal_retile.html>`_ from the gdal project.

The executeable for Windows users is **gdal_retile.bat** or only **gdal_retile**, Unix users call **gdal_retile.py**

Create a subdirectory **tiles** in your **working** directory and execute within the **working** directory::

  gdal_retile -co "WORLDFILE=YES"  -r bilinear -ps 128 128 -of PNG -levels 2 -targetDir tiles start.png

What is happening ? We tell gdal_retile to create world files for our tiles (-co "WORLDFILE=YES"), use bilinear interpolation (-r bilinear), the tiles are 128x128 pixels in size (-ps 128 128) , the image format should be PNG (-of PNG), we need 2 pyramid levels (-levels 2) ,the directory for the result is **tiles** (-targetDir tiles) and the source image is **start.png**.

.. note::

  A few words about the tile size. 128x128 pixel is proper for this example. Do not use such small sizes in a production environment. A size of 256x256 will reduce the number of tiles by a factor of 4, 512x512 by a factor of 16 and so on. Producing too much tiles will degrade performance on the database side (large tables) and will also raise cpu usage on the client side ( more image operations).

Now you should have the following directories

*	**working** containing **start.png** , **start.wld** and a subdirectory **tiles**.
*	**working/tiles** containing many \*.png files and associated \*.wld files representing the tiles of **start.png**
*	**working/tiles/1** containing many \*.png files and associated \*.wld files representing the tiles of the first pyramid
*	**working/tiles/2** containing many \*.png files and associated \*.wld files representing the tiles of the second pyramid 

Configuring the new map
-----------------------

The configuration for a map is done in a xml file. This file has 3 main parts.

#.	The connect info for the jdbc driver
#.	The mapping info for the sql tables
#.	Configuration data for the map

Since the jdbc connect info and the sql mapping may be reused by more than one map, the best practice is to create xml fragments for both of them and to use xml entity references to include them into the map xml.

Put all configuration files into the **coverages** subdirectory of your GeoServer data directory. The standard location is

**<directory of your GeoServer installation>/data_dir/coverages**

1) Create a file **connect.postgis.xml.inc** with the following content::

    <connect>
      <!-- value DBCP or JNDI -->
      <dstype value="DBCP"/>
      <!--   <jndiReferenceName value=""/>  -->
      <username value="postgres" />
      <password value="postgres" />
      <jdbcUrl value="jdbc:postgresql://localhost:5432/gis" />
      <driverClassName value="org.postgresql.Driver"/>
      <maxActive value="10"/>
      <maxIdle value="0"/>
    </connect>
 
The jdbc user is "postgres", the password is "postgres", maxActive and maxIdle are parameters of the apache connection pooling, jdbcUrl and driverClassName are postgres specific. The name of the database is "gis".

If you deploy GeoServer into a J2EE container capable of handling jdbc data sources, a better approach is::

  <connect>
    <!-- value DBCP or JNDI -->
    <dstype value="JNDI"/>
    <jndiReferenceName value="jdbc/mydatasource"/>        
  </connect> 

For this tutorial, we do not use data sources provided by a J2EE container.

2) The next xml fragment to create is **mapping.postgis.xml.inc** 

mapping.postgis.xml.inc::

  <!-- possible values: universal,postgis,db2,mysql,oracle -->
  <spatialExtension name="postgis"/>
  <mapping>
      <masterTable name="mosaic" >
	<coverageNameAttribute name="name"/>
	<maxXAttribute name="maxX"/>
	<maxYAttribute name="maxY"/>
	<minXAttribute name="minX"/>
	<minYAttribute name="minY"/>
	<resXAttribute name="resX"/>
	<resYAttribute name="resY"/>
	<tileTableNameAtribute  name="TileTable" />
	<spatialTableNameAtribute name="SpatialTable" />
      </masterTable>
      <tileTable>
	<blobAttributeName name="data" />
	<keyAttributeName name="location" />
      </tileTable>
      <spatialTable>
	<keyAttributeName name="location" />
	<geomAttributeName name="geom" />
	<tileMaxXAttribute name="maxX"/>
	<tileMaxYAttribute name="maxY"/>
	<tileMinXAttribute name="minX"/>
	<tileMinYAttribute name="minY"/>
      </spatialTable>
  </mapping>
 
The first element ``<spatialExtension>`` specifies which spatial extension the module should use. "universal" means that there is no spatial db extension at all, meaning the tile grid is not stored as a geometry, using simple double values instead.

This xml fragment describes 3 tables, first we need a master table where information for each pyramid level is saved. Second and third, the attribute mappings for storing image data, envelopes and tile names are specified. To keep this tutorial simple, we will not further discuss these xml elements. After creating the sql tables things will become clear.

3) Create the configuration xml **osm.postgis.xml** for the map (osm for "open street map") 

osm.postgis.xml::

  <?xml version="1.0" encoding="UTF-8" standalone="no"?>
  <!DOCTYPE ImageMosaicJDBCConfig [
    <!ENTITY mapping PUBLIC "mapping"  "mapping.postgis.xml.inc">
    <!ENTITY connect PUBLIC "connect"  "connect.postgis.xml.inc">]>
  <config version="1.0">
    <coverageName name="osm"/>
    <coordsys name="EPSG:4326"/>
    <!-- interpolation 1 = nearest neighbour, 2 = bilinear, 3 = bicubic -->
    <scaleop  interpolation="1"/>
    <verify cardinality="false"/>
    &mapping;
    &connect;
  </config>

This is the final xml configuration file, including our mapping and connect xml fragment. The coverage name is "osm", CRS is EPSG:4326. ``<verify cardinality="false">`` means no check if the number of tiles equals the number of rectangles stored in the db. (could be time consuming in case of large tile sets).

This configuration is the hard stuff, now, life becomes easier :-)

Using the java ddl generation utility
-------------------------------------


The full documentation is here: `<http://docs.codehaus.org/display/GEOTDOC/Using+the+java+ddl+generation+utility>`_

To create the proper sql tables, we can use the java ddl generation utility. This utility is included in the gt-imagemosaic-jdbc-<version>.jar. Assure that this jar file is in your **WEB-INF/lib** directory of your GeoServer installation.

Change to your **working** directory and do a first test::
  
  java -jar <your_geoserver_install_dir>/webapps/geoserver/WEB-INF/lib/gt-imagemosaic-jdbc-<version>.jar

The reply should be::

  Missing cmd import | ddl

 
Create a subdirectory **sqlscripts** in your **working** directory. Within the **working** directory, execute::

 java -jar <your_geoserver_install_dir>/webapps/geoserver/WEB-INF/lib/gt-imagemosaic-jdbc-<version>.jar ddl -config <your geoserver data dir >/coverages/osm.postgis.xml -spatialTNPrefix tileosm -pyramids 2 -statementDelim ";" -srs 4326 -targetDir sqlscripts
 
Explanation of parameters

.. list-table::
  :widths: 20 80

  * - **parameter** 
    - **description**
  * - ddl 
    - create ddl statements
  * - -config
    - the file name of our **osm.postgis.xml** file
  * - -pyramids
    - number of pyramids we want
  * - -statementDelim
    - The SQL statement delimiter to use
  * - -srs 
    - The db spatial reference identifier when using a spatial extension
  * - -targetDir
    - output directory for the scripts
  * - -spatialTNPrefix
    - A prefix for tablenames to be created.

In the directory **working/sqlscripts** you will find the following files after execution:

**createmeta.sql dropmeta.sql add_osm.sql remove_osm.sql**

.. note::

  *IMPORTANT:*

  Look into the files **createmeta.sql** and **add_osm.sql** and compare them with the content of **mapping.postgis.xml.inc.** If you understand this relationship, you understand the mapping.

The generated scripts are only templates, it is up to you to modify them for better performance or other reasons. But do not break the relationship to the xml mapping fragment.

Executing the DDL scripts
-------------------------

For user "postgres", databae "gis", execute in the following order::

  psql -U postgres -d gis  -f createmeta.sql
  psql -U postgres -d gis  -f add_osm.sql

To clean your database, you can execute **remove_osm.sql** and **dropmeta.sql** after finishing the tutorial.

Importing the image data
------------------------


The full documentation is here: `<http://docs.codehaus.org/display/GEOTDOC/Using+the+java+import+utility>`_

First, the jdbc jar file has to be in the **lib/ext** directory of your java runtime. In my case I had to copy **postgresql-8.1-407.jdbc3.jar**.

Change to the **working** directory and execute::

  java -jar <your_geoserver_install_dir>/webapps/geoserver/WEB-INF/lib/gt-imagemosaic-jdbc-<version>.jar import  -config <your geoserver data dir>/coverages/osm.postgis.xml -spatialTNPrefix tileosm -tileTNPrefix tileosm -dir tiles -ext png

This statement imports your tiles including all pyramids into your database.


Configuring GeoServer
---------------------


Start GeoServer and log in.Under Config --> WCS -> CoveragePlugins you should see 

.. image:: snapshot1.png


If there is no line starting with "ImageMosaicJDBC", the **gt-imagemosiac-jdbc-<version>.jar** file is not in your **WEB-INF/lib** folder.
Go to Config->Data->CoverageStores->New and fill in the formular

.. image:: snapshot2.png

Press New and fill in the formular

.. image:: snapshot3.png

Press Submit.

Press Apply, then Save to save your changes.

Next select Config->Data->Coverages->New and select "osm".

.. image:: snapshot4.png

Press New and you will enter the Coverage Editor. Press Submit, Apply and Save.

Under Welcome->Demo->Map Preview you will find a new layer "topp:osm". Select it and see the results 

.. image:: snapshot5.png

If you think the image is stretched, you are right. The reason is that the original image is georeferenced with EPSG:900913, but there is no support for this CRS in postigs (at the time of this writing). So I used EPSG:4326. For the purpose of this tutorial, this is ok.


Conclusion
----------

There are a lot of other configuration possibilities for specific databases. This tutorial shows a quick cookbook to demonstrate some of the features of this module. Follow the links to the full documentation to dig deeper, especially if you are concerned about performance and database design.

If there is something which is missing, proposals are welcome.
