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
package org.polymap.p4.style;

import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.builder.StyledLayerDescriptorBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.polymap.p4.style.entities.FeatureType;
import org.polymap.p4.style.entities.StyleComposite;
import org.polymap.p4.style.entities.StyleFeature;
import org.polymap.p4.style.entities.StyleFigure;
import org.polymap.p4.style.entities.StyleIdent;
import org.polymap.p4.style.entities.StylePoint;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class SimpleStylerTest
        extends AbstractSLDTest {

    @Test
    public void testStyleDAO() throws Exception {
        SimpleStyler simpleStyler = createEmptySimpleStyler();
        StyleIdent ident = createStyleIdent( simpleStyler );
        ident.name.set( "MeinStyle" );
        ident.featureType.set( FeatureType.POINT );

        StyleFeature feature = createStyleFeature( simpleStyler );
        StyleComposite composite = feature.styleComposite.createValue( null );
        StylePoint point = composite.stylePoints.createElement( null );
        StyleFigure styleFigure = createStyleFigure( point );
        styleFigure.markerWellKnownName.set( "Circle" );
        point.markerSize.set( 12d );
        styleFigure.markerFill.createValue( col -> initStyleColor( col, 255, 255, 255 ) );
        styleFigure.markerStrokeColor.createValue( col -> initStyleColor( col, 0, 0, 0 ) );
        styleFigure.markerStrokeSize.set( 3d );

        StyledLayerDescriptorBuilder wrappedBuilder = new StyledLayerDescriptorBuilder();
        SLDBuilder builder = new SLDBuilder( wrappedBuilder );
        ident.fillSLD( builder );
        point.fillSLD( builder );

        assertWrittenSLD( builder, getClass().getResource( "simple_sld.xml" ) );
    }


    @Test
    public void testFromStyleDAO() throws Exception {
        StyledLayerDescriptor sld = getSLD( getClass().getResourceAsStream( "simple_sld.xml" ) );

        SimpleStyler simpleStyler = createEmptySimpleStyler();
        StyleIdent ident = createStyleIdent( simpleStyler );
        ident.fromSLD( sld );
        Assert.assertEquals( "MeinStyle", ident.name.get() );
        Assert.assertEquals( FeatureType.POINT, ident.featureType.get() );

        StyleFeature feature = createStyleFeature( simpleStyler );
        StyleComposite composite = feature.styleComposite.createValue( null );
        StylePoint point = composite.stylePoints.createElement( null );
        point.fromSLD( sld );
        Assert.assertNotNull( point.markerFigure.get() );
        StyleFigure styleFigure = point.markerFigure.get();
        Assert.assertEquals( "Circle", styleFigure.markerWellKnownName.get() );
        Assert.assertEquals( 12, point.markerSize.get().intValue() );
        Assert.assertEquals( 255, styleFigure.markerFill.get().red.get().intValue() );
        Assert.assertEquals( 255, styleFigure.markerFill.get().green.get().intValue() );
        Assert.assertEquals( 255, styleFigure.markerFill.get().blue.get().intValue() );
        Assert.assertEquals( 0, styleFigure.markerStrokeColor.get().red.get().intValue() );
        Assert.assertEquals( 0, styleFigure.markerStrokeColor.get().green.get().intValue() );
        Assert.assertEquals( 0, styleFigure.markerStrokeColor.get().blue.get().intValue() );
        Assert.assertEquals( 3, styleFigure.markerStrokeSize.get().intValue() );
    }
}
