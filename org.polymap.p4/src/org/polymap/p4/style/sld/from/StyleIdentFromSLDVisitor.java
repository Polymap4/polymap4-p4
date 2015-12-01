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
package org.polymap.p4.style.sld.from;

import java.util.Arrays;

import org.geotools.styling.NamedLayer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyledLayerDescriptor;
import org.polymap.p4.style.entities.FeatureType;
import org.polymap.p4.style.entities.StyleIdent;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleIdentFromSLDVisitor
        extends AbstractStyleFromSLDVisitor {

    private final StyleIdent styleIdent;


    public StyleIdentFromSLDVisitor( StyleIdent styleIdent ) {
        this.styleIdent = styleIdent;
    }


    public void visit( StyledLayerDescriptor sld ) {
        if(sld.getName() != null) {
            styleIdent.name.set( sld.getName() );
        }
        styleIdent.title.set( sld.getTitle() );
        super.visit( sld );
    }


    @Override
    public void visit( NamedLayer layer ) {
        if(layer.getName() != null) {
            styleIdent.name.set( layer.getName() );
        }
        Arrays.asList( layer.getStyles() ).stream().forEach( style -> style.accept( this ) );
        super.visit( layer );
    }


    @Override
    public void visit( Style style ) {
        if (styleIdent.name.get() == null && style.getName() != null) {
            styleIdent.name.set( style.getName() );
        }
        if (style.getDescription() != null) {
            if (style.getDescription().getTitle() != null) {
                styleIdent.title.set( style.getDescription().getTitle().toString() );
            }
            if (style.getDescription().getAbstract() != null) {
                styleIdent.description.set( style.getDescription().getAbstract().toString() );
            }
        }
        super.visit( style );
    }


    @Override
    public void visit( org.geotools.styling.TextSymbolizer line ) {
        styleIdent.featureType.set( FeatureType.TEXT );
    }


    @Override
    public void visit( org.geotools.styling.PointSymbolizer line ) {
        styleIdent.featureType.set( FeatureType.POINT );
    }


    @Override
    public void visit( org.geotools.styling.LineSymbolizer line ) {
        styleIdent.featureType.set( FeatureType.LINE_STRING );
    }


    @Override
    public void visit( org.geotools.styling.PolygonSymbolizer poly ) {
        styleIdent.featureType.set( FeatureType.POLYGON );
    }


    @Override
    public void visit( RasterSymbolizer raster ) {
        styleIdent.featureType.set( FeatureType.RASTER );
    }
}
