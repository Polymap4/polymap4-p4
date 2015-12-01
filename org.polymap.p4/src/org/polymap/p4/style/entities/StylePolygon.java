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
import org.polymap.p4.style.sld.from.StylePolygonFromSLDVisitor;
import org.polymap.p4.style.sld.to.StylePolygonToSLDVisitor;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StylePolygon
        extends AbstractStyleSymbolizer {

    @Nullable
    public Property<StyleLabel>  polygonLabel;

    @Nullable
    public Property<StyleColor>  fill;
    
    @Nullable
    public Property<Double>      fillOpacity;

    @Nullable
    public Property<Double>      strokeWidth;

    @Nullable
    public Property<StyleColor>  strokeColor;

    @Nullable
    public Property<StyleImage>  image;

    @Nullable
    public Property<StyleFigure> figure;

    @Nullable
    public Property<Double>      symbolSize;


    @Override
    public void fromSLD( StyledLayerDescriptor style ) {
        style.accept( new StylePolygonFromSLDVisitor( this ) );
    }


    @Override
    public void fillSLD( SLDBuilder builder ) {
        new StylePolygonToSLDVisitor( this ).fillSLD( builder );
    }
}
