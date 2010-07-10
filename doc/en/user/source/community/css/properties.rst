.. highlight:: css

Property Listing
================

This page lists the supported rendering properties.  See :doc:`values` for more
information about the value types for each.

Point Symbology
---------------

.. list-table::

    - * **Property**
      * **Type**
      * **Meaning**
      * **Accepts Expression?**
    - * ``mark``     
      * url,symbol
      * The image or well-known shape to render for points
      * yes
    - * ``mark-geometry`` 
      * expression
      * An expression to use for the geometry when rendering features
      * yes
    - * ``mark-size`` 
      * length   
      * The width to assume for the provided image.  The height will be
        adjusted to preserve the source aspect ratio. 
      * yes
    - * ``mark-rotation``
      * angle 
      * A rotation to be applied (clockwise) to the mark image.
      * yes

Line Symbology
--------------

.. list-table:: 

    - * **Property** 
      * **Type**
      * **Meaning**
      * **Accepts Expression?**
    - * ``stroke``
      * color, url, symbol
      * The color, graphic, or well-known shape to use to stroke lines or outlines
      * yes
    - * ``stroke-geometry``
      * expression
      * An expression to use for the geometry when rendering features. 
      * yes
    - * ``stroke-mime``      
      * string           
      * The mime-type of the external graphic provided.  This is **required**
        when using external graphics
      * yes
    - * ``stroke-opacity``   
      * percentage       
      * A value in the range of 0 (fully transparent) to 1.0 (fully opaque)  
      * yes
    - * ``stroke-width``     
      * length           
      * The width to use for stroking the line.
      * yes
    - * ``stroke-size``     
      * length           
      * An image or symbol used for the stroke pattern will be stretched or
        squashed to this size before rendering.  If this value differs from the
        stroke-width, the graphic will be repeated or clipped as needed.
      * yes
    - * ``stroke-rotation``  
      * angle            
      * A rotation to be applied (clockwise) to the stroke image. See also the
        ``stroke-repeat`` property.
      * yes
    - * ``stroke-linecap``   
      * keyword: miter, butt, square
      * The style to apply to the ends of lines drawn 
      * yes
    - * ``stroke-linejoin``  
      * keyword: miter, butt, square
      * The style to apply to the "elbows" where segments of multi-line features meet. 
      * yes
    - * ``stroke-dasharray`` 
      * list of lengths  
      * The lengths of segments to use in a dashed line. 
      * no
    - * ``stroke-dashoffset``
      * length           
      * How far to offset the dash pattern from the ends of the lines.  
      * yes|
    - * ``stroke-repeat``
      * keyword: repeat, stipple
      * How to use the provided graphic to paint the line.  If repeat, then the
        graphic is repeatedly painted along the length of the line (rotated
        appropriately to match the line's direction).  If stipple, then the line
        is treated as a polygon to be filled.
      * yes

Polygon Symbology
-----------------

.. list-table:: 

    - * **Property** 
      * **Type**
      * **Meaning**
      * **Accepts Expression?**
    - * ``fill``         
      * color, url, symbol 
      * The color, graphic, or well-known shape to use to stroke lines or outlines 
      * yes
    - * ``fill-geometry``
      * expression 
      * An expression to use for the geometry when rendering features. 
      * yes
    - * ``fill-mime``    
      * string            
      * The mime-type of the external graphic provided.  This is *required*
        when using external graphics 
      * yes
    - * ``fill-opacity`` 
      * percentage        
      * A value in the range of 0 (fully transparent) to 1.0 (fully opaque) 
      * yes
    - * ``fill-size``    
      * length            
      * The width to assume for the image or graphic provided. 
      * yes
    - * ``fill-rotation``
      * angle             
      * A rotation to be applied (clockwise) to the fill image. 
      * yes

Text Symbology (Labeling)
-------------------------

.. list-table:: 

    - * **Property** 
      * **Type**
      * **Meaning**
      * **Accepts Expression?**
    - * ``label``      
      * string
      * The text to display as labels for features
      * yes
    - * ``label-geometry``
      * expression 
      * An expression to use for the geometry when rendering features. 
      * yes
    - * ``font-family``
      * string
      * The name of the font or font family to use for labels
      * yes
    - * ``font-fill``
      * fill
      * The fill to use when rendering fonts
      * yes
    - * ``font-style`` 
      * keyword: normal, italic, oblique
      * The style for the lettering 
      * yes
    - * ``font-weight``
      * keyword: normal, bold
      * The weight for the lettering 
      * yes
    - * ``font-size``  
      * length
      * The size for the font to display. 
      * yes
    - * ``halo-radius``  
      * length
      * The size of a halo to display around the lettering (to enhance
        readability). This is *required* to activate the halo feature. 
      * yes
    - * ``halo-color`` 
      * color 
      * The color for the halo 
      * yes
    - * ``halo-opacity``
      * percentage
      * The opacity of the halo, from 0 (fully transparent) to 1.0 (fully opaque). 
      * yes
    - * ``-gt-label-padding``
      * length
      * The amount of 'padding' space to provide around labels.  Labels will
        not be rendered closer together than this threshold.  This is
        equivalent to the :ref:`spaceAround<labeling_space_around>` vendor parameter.
      * no
    - * ``-gt-label-group``
      * one of: ``true`` or ``false``
      * If true, the render will treat features with the same label text as a
        single feature for the purpose of labeling.  This is equivalent to the 
        :ref:`group<labeling_group>` vendor parameter.
      * no
    - * ``-gt-label-max-displacement``
      * length
      * If set, this is the maximum displacement that the renderer will apply
        to a label.  Labels that need larger displacements to avoid collisions
        will simply be omitted.  This is equivalent to the
        :ref:`maxDisplacement<labeling_max_displacement>` vendor parameter.
      * no
    - * ``-gt-label-min-group-distance``
      * length
      * This is equivalent to the minGroupDistance vendor parameter in SLD.
      * no
    - * ``-gt-label-repeat``
      * length
      * If set, the renderer will repeat labels at this interval along a line.
        This is equivalent to the :ref:`repeat<labeling_repeat>` vendor parameter.
      * no
    - * ``-gt-label-all-group``
      * one of ``true`` or ``false``
      * when using grouping, whether to label only the longest line that could
        be built by merging the lines forming the group, or also the other
        ones.  This is equivalent to the :ref:`allGroup<labeling_all_group>`
        vendor parameter.
      * no
    - * ``-gt-label-remove-overlaps``
      * one of ``true`` or ``false``
      * If enabled, the renderer will remove overlapping lines within a group
        to avoid duplicate labels.  This is equivalent to the
        removeOverlaps vendor parameter.
      * no
    - * ``-gt-label-allow-overruns``
      * one of ``true`` or ``false``
      * Determines whether the renderer will show labels that are longer than
        the lines being labelled.  This is equivalent to the allowOverrun
        vendor parameter.
      * no
    - * ``-gt-label-follow-line``
      * one of ``true`` or ``false``
      * If enabled, the render will curve labels to follow the lines being
        labelled.  This is equivalent to the
        :ref:`followLine<labeling_follow_line>` vendor parameter.
      * no
    - * ``-gt-label-max-angle-delta``
      * one of ``true`` or ``false``
      * The maximum amount of curve allowed between two characters of a label;
        only applies when '-gt-follow-line: true' is set.  This is equivalent
        to the :ref:`maxAngleDelta<labeling_max_angle_delta>` vendor parameter.
      * no
    - * ``-gt-label-auto-wrap``
      * length
      * Labels will be wrapped to multiple lines if they exceed this length in
        pixels.  This is equivalent to the :ref:`autoWrap<labeling_autowrap>`
        vendor parameter.
      * no
    - * ``-gt-label-force-ltr``
      * one of ``true`` or ``false``
      * By default, the renderer will flip labels whose normal orientation
        would cause them to be upside-down. Set this parameter to false if you
        are using some icon character label like an arrow to show a line's
        direction.  This is equivalent to the
        :ref:`forceLeftToRight<labeling_force_left_to_right>` vendor parameter.
      * no
    - * ``-gt-label-conflict-resolution``
      * one of ``true`` or ``false``
      * Set this to false to disable label conflict resolution, allowing
        overlapping labels to be rendered.  This is equivalent to the
        :ref:`conflictResolution<labeling_conflict_resolution>` vendor
        parameter.
      * no
    - * ``-gt-label-fit-goodness``
      * scale
      * The renderer will omit labels that fall below this "match quality"
        score.  The scoring rules differ for each geometry type.  This is
        equivalent to the :ref:`goodnessOfFit<labeling_goodness_of_fit>` vendor
        parameter.
      * no
    - * ``-gt-label-priority``
      * expression
      * This option specifies an expression to use in determining which
        features to prefer if there are labeling conflicts.  This is equivalent
        to the :ref:`Priority<labeling_priority>` SLD extension.
      * yes
  
Shared
------

.. list-table:: 

    - * **Property** 
      * **Type**
      * **Meaning**
      * **Accepts Expression?**
    - * ``geometry``
      * expression 
      * An expression to use for the geometry when rendering features. This
        provides a geometry for all types of symbology, but can be overridden
        by the symbol-specific geometry properties. 
      * yes

Symbol Properties
-----------------

These properties are applied only when styling built-in symbols.  See
:doc:`/community/css/styled-marks` for details.

.. list-table::

    - * **Property** 
      * **Type**
      * **Meaning**
      * **Accepts Expression?**
    - * ``size``
      * length
      * The size at which to render the symbol. 
      * yes
    - * ``rotation``
      * angle
      * An angle through which to rotate the symbol. 
      * yes
