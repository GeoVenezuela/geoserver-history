.. _production_java:

Java Considerations
===================

Use Sun's JDK
-------------

GeoServer speed depends a lot on the chosen Java Development Kit (JDK).  **For best performance, use Sun JDK 1.6** (also known as JDK 6).  If this is not possible, use Sun JDK 1.5.  Non-Sun JDKs may work, but are generally not tested or supported. OpenJDK currently does not work with GeoServer, as it lacks sufficient support for 2D rendering.

Install native JAI and JAI Image I/O extensions
-----------------------------------------------

The `Java Advanced Imaging API <http://java.sun.com/javase/technologies/desktop/media/>`_ (JAI) is an advanced image manipulation library built by Sun.  GeoServer requires JAI to work with coverages and leverages it for WMS output generation. By default, GeoServer ships with the pure Java version of JAI, but **for best performance, install the native JAI version in your JDK**.

In particular, installing the native JAI is important for all raster processing, which is used heavily in both WMS and WCS to rescale, cut and reproject rasters. Installing the native JAI is also important for all raster reading and writing, which affects both WMS and WCS.  Finally, native JAI is very useful even if there is no raster data involved, as WMS output encoding requires writing PNG/GIF/JPEG images, which are themselves rasters.

Native extensions are available for Windows, Linux and Solaris (32 and 64 bit systems).  They are, however, not available for OS X.

.. note:: These installers are limited to allow adding native extensions to just one version of the JDK on your system.  If native extensions are needed on multiple versions, manually unpacking the extensions will be necessary.  See the section on :ref:`native_JAI_manual_install`.

.. note:: These installers are also only able to apply the extensions to the currently used JDK.  If native extensions are needed on a different JDK than that which is currently used, it will be necessary to uninstall the current one first, then run the setup program against the remaining JDK.

Installing native JAI on Windows
````````````````````````````````

#. Go to the `JAI download page <https://jai.dev.java.net/binary-builds.html>`_ and download the Windows installer for version 1.1.3. At the time of writing only the 32 bit version of the installer is available, so you will want to download `jai-1_1_3-lib-windows-i586-jdk.exe <http://download.java.net/media/jai/builds/release/1_1_3/jai-1_1_3-lib-windows-i586-jdk.exe>`_
#. Run the installer and point it to the JDK install that GeoServer will use to run. 
#. Go to the `JAI Image I/O download page <https://jai-imageio.dev.java.net/binary-builds.html>`_ and download the Windows installer for version 1.1. At the time of writing only the 32 bit version of the installer is available, so you will want to download `jai_imageio-1_1-lib-windows-i586-jdk.exe <http://download.java.net/media/jai-imageio/builds/release/1.1/jai_imageio-1_1-lib-windows-i586-jdk.exe>`_.
#. Run the installer and point it to the JDK install that GeoServer will use to run.

Installing native JAI on Linux
``````````````````````````````

#. Go to the `JAI download page <https://jai.dev.java.net/binary-builds.html>`_ and download the Linux installer for version 1.1.3, choosing the appropriate architecture:

   * `i586` for the 32 bit systems
   * `amd64` for the 64 bit ones (even if using Intel processors)

#. Copy the file into the directory containing the JDK and then run it.  For example, on an Ubuntu 32 bit system::
  
    $ sudo cp jai-1_1_3-lib-linux-i586-jdk.bin /usr/lib/jvm/java-6-sun
    $ cd /usr/lib/jvm/java-6-sun
    $ sudo sh jai-1_1_3-lib-linux-i586-jdk.bin
    # accept license 
    $ sudo rm jai-1_1_3-lib-linux-i586-jdk.bin
  
#. Go to the `JAI Image I/O download page <https://jai-imageio.dev.java.net/binary-builds.html>`_ and download the Linux installer for version 1.1, choosing the appropriate architecture:

   * `i586` for the 32 bit systems
   * `amd64` for the 64 bit ones (even if using Intel processors)

#. Copy the file into the directory containing the JDK and then run it.  If you encounter difficulties, you may need to export the environment variable ``_POSIX2_VERSION=199209``. For example, on a Ubuntu 32 bit Linux system::
  
    $ sudo cp jai_imageio-1_1-lib-linux-i586-jdk.bin /usr/lib/jvm/java-6-sun
    $ cd /usr/lib/jvm/java-6-sun
    $ sudo su
    $ export _POSIX2_VERSION=199209
    $ sh jai_imageio-1_1-lib-linux-i586-jdk.bin
    # accept license
    $ rm ./jai_imageio-1_1-lib-linux-i586-jdk.bin
    $ exit

.. _native_JAI_manual_install:

Installing native JAI manually
``````````````````````````````

You can install the native JAI manually if you encounter problems using the above installers, or if you wish to install the native JAI for more than one JDK.

Please refer to the `GeoTools page on JAI installation <http://docs.codehaus.org/display/GEOT/Manual+JAI+Installation>`_ for details.

 
GeoServer cleanup
`````````````````

Once the installation is complete, you may optionally remove the original JAI files from the GeoServer instance::

   jai_core-x.y.z.jar
   jai_imageio-x.y.jar 
   jai_codec-x.y.z.jar
   
where ``x``, ``y``, and ``z`` refer to specific version numbers.