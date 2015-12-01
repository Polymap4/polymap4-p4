/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.style.entities;

import org.geotools.styling.StyledLayerDescriptor;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.p4.style.SLDBuilder;
import org.polymap.p4.style.sld.from.StyleLabelFromSLDVisitor;
import org.polymap.p4.style.sld.to.StyleLabelToSLDVisitor;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 * @see http
 *      ://docs.geoserver.org/stable/en/user/styling/sld-reference/textsymbolizer.
 *      html#sld-reference-textsymbolizer
 * @see http
 *      ://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#
 *      labeling -priority
 */
public class StyleLabel
        extends AbstractSLDModel {

    @Nullable
    public Property<String>                   labelText;

    @Nullable
    public Property<StyleFont>                labelFont;
    
    @Nullable
    public Property<StyleColor>               labelFontColor;

    @Nullable
    public Property<StyleLabelPointPlacement> pointPlacement;

    @Nullable
    public Property<StyleLabelLinePlacement>  linePlacement;

    @Nullable
    public Property<Double>                   haloRadius;

    @Nullable
    public Property<StyleColor>               haloFill;

    // GeoServer vendor options: please note 
    // that the attribute name must match the name in the vendor options
    // as this is assumed in org.polymap.p4.style.sld.from.AbstractStyleFromSLDVisitor.
    // handleVendorOption(TextSymbolizer, Property<T>, Function<String, T>)

    @Nullable
    public Property<Double>                   autoWrap;

    
    /* 
     *  @formatter:off
     *  
     *  Left off geo server extensions:
     *   
     *  - http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#priority-labeling
     *  - http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#grouping-features-group
     *  - http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#labelallgroup
     *  - http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#overlapping-and-separating-labels-spacearound
     *  - http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#forcelefttoright
     *  - http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#conflictresolution
     *  - http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#goodnessoffit
     *  - http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#polygonalign
     *  - http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#graphic-resize
     *  - http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#graphic-margin
     *  - http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#partials
     *  
     *  @formatter:on
     */
    
    @Override
    public void fromSLD( StyledLayerDescriptor style ) {
        new StyleLabelFromSLDVisitor( this, FeatureType.TEXT );
    }

    @Override
    public void fillSLD( SLDBuilder builder ) {
        new StyleLabelToSLDVisitor( this ).fillSLD( builder );
    }    
}
