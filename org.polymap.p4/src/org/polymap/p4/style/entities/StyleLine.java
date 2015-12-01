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
import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.p4.style.SLDBuilder;
import org.polymap.p4.style.sld.from.StyleLineFromSLDVisitor;
import org.polymap.p4.style.sld.to.StyleLineToSLDVisitor;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleLine
        extends AbstractStyleSymbolizer {

    @Nullable
    public Property<Integer>             lineWidth;

    @Nullable
    public Property<StyleColor>          lineColor;

    @Nullable
    public Property<StyleLabel>          lineLabel;

    @Nullable
    public Property<StylePoint>          lineSymbol;

    @Nullable
    public Property<LineCapType>         lineCap;

    @Nullable
    public Property<String>              lineDashPattern;

    @Nullable
    public Property<Double>              lineDashOffset;

    @Nullable
    public Property<Double>              lineTransparency;

    @Nullable
    public Property<LineJoinType>        lineJoin;

    @Nullable
    public Property<StyleLine>           border;

    public CollectionProperty<StyleLine> alternatingLineStyles;

    public CollectionProperty<StyleLine> additionalLineStyles;

    // GeoServer vendor options

    @Nullable
    public Property<Boolean>             followLine;

    @Nullable
    public Property<Double>              maxAngleDelta;        // angle -360° - 360°

    @Nullable
    public Property<Double>              maxDisplacement;

    @Nullable
    public Property<Double>              repeat;               // integer


    @Override
    public void fromSLD( StyledLayerDescriptor style ) {
        style.accept( new StyleLineFromSLDVisitor( this ) );
    }


    @Override
    public void fillSLD( SLDBuilder builder ) {
        new StyleLineToSLDVisitor( this ).fillSLD( builder );
    }
}
