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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.builder.FeatureTypeStyleBuilder;
import org.geotools.styling.builder.FillBuilder;
import org.geotools.styling.builder.GraphicBuilder;
import org.geotools.styling.builder.LineSymbolizerBuilder;
import org.geotools.styling.builder.NamedLayerBuilder;
import org.geotools.styling.builder.PointSymbolizerBuilder;
import org.geotools.styling.builder.PolygonSymbolizerBuilder;
import org.geotools.styling.builder.RuleBuilder;
import org.geotools.styling.builder.StyleBuilder;
import org.geotools.styling.builder.StyledLayerDescriptorBuilder;
import org.geotools.styling.builder.TextSymbolizerBuilder;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class SLDBuilder {

    private final StyledLayerDescriptorBuilder           wrappedBuilder;

    private NamedLayerBuilder                            namedLayerBuilder        = null;

    private Map<NamedLayerBuilder,StyleBuilder>          namedLayerStyleBuilders  = new HashMap<NamedLayerBuilder,StyleBuilder>();

    private Map<StyleBuilder,FeatureTypeStyleBuilder>    featureTypeStyleBuilders = new HashMap<StyleBuilder,FeatureTypeStyleBuilder>();

    private Map<FeatureTypeStyleBuilder,RuleBuilder>     ruleBuilders             = new HashMap<FeatureTypeStyleBuilder,RuleBuilder>();

    private Map<RuleBuilder,TextSymbolizerBuilder>       textSymbolizerBuilders   = new HashMap<RuleBuilder,TextSymbolizerBuilder>();

    private Map<RuleBuilder,PointSymbolizerBuilder>      pointSymbolizerBuilders  = new HashMap<RuleBuilder,PointSymbolizerBuilder>();

    private Map<TextSymbolizerBuilder,FillBuilder>       textFillBuilders         = new HashMap<TextSymbolizerBuilder,FillBuilder>();

    private Map<PointSymbolizerBuilder,GraphicBuilder>   pointGraphicBuilders     = new HashMap<PointSymbolizerBuilder,GraphicBuilder>();

    private Map<LineSymbolizerBuilder,GraphicBuilder>    lineGraphicBuilders      = new HashMap<LineSymbolizerBuilder,GraphicBuilder>();

    private Map<PolygonSymbolizerBuilder,FillBuilder>    polygonFillBuilders      = new HashMap<PolygonSymbolizerBuilder,FillBuilder>();

    private Map<PolygonSymbolizerBuilder,GraphicBuilder> polygonGraphicBuilders   = new HashMap<PolygonSymbolizerBuilder,GraphicBuilder>();


    public SLDBuilder( StyledLayerDescriptorBuilder wrappedBuilder ) {
        this.wrappedBuilder = wrappedBuilder;
    }


    /**
     * @return
     */
    public NamedLayerBuilder namedLayer() {
        if (namedLayerBuilder == null) {
            namedLayerBuilder = wrappedBuilder.namedLayer();
        }
        return namedLayerBuilder;
    }


    public StyledLayerDescriptor build() {
        return wrappedBuilder.build();
    }


    public StyleBuilder style( NamedLayerBuilder namedLayer ) {
        return mapEntry( namedLayerStyleBuilders, namedLayer, ( ) -> namedLayer.style() );
    }


    public FeatureTypeStyleBuilder featureTypeStyle( StyleBuilder style ) {
        return mapEntry( featureTypeStyleBuilders, style, ( ) -> style.featureTypeStyle() );
    }


    public RuleBuilder rule( FeatureTypeStyleBuilder featureTypeStyle ) {
        return mapEntry( ruleBuilders, featureTypeStyle, ( ) -> featureTypeStyle.rule() );
    }


    public TextSymbolizerBuilder text( RuleBuilder ruleBuilder ) {
        return mapEntry( textSymbolizerBuilders, ruleBuilder, ( ) -> ruleBuilder.text() );
    }


    public FillBuilder textColor( TextSymbolizerBuilder textSymbolizerBuilder ) {
        return mapEntry( textFillBuilders, textSymbolizerBuilder, ( ) -> textSymbolizerBuilder.fill() );
    }


    public PointSymbolizerBuilder point( RuleBuilder ruleBuilder ) {
        return mapEntry( pointSymbolizerBuilders, ruleBuilder, ( ) -> ruleBuilder.point() );
    }


    public GraphicBuilder pointGraphicBuilder( PointSymbolizerBuilder pointSymbolizerBuilder ) {
        return mapEntry( pointGraphicBuilders, pointSymbolizerBuilder, ( ) -> pointSymbolizerBuilder.graphic() );
    }


    public GraphicBuilder lineGraphicBuilder( LineSymbolizerBuilder lineSymbolizerBuilder ) {
        return mapEntry( lineGraphicBuilders, lineSymbolizerBuilder, ( ) -> lineSymbolizerBuilder.stroke()
                .graphicStroke() );
    }


    public FillBuilder polygonFill( PolygonSymbolizerBuilder polygonSymbolizerBuilder ) {
        return mapEntry( polygonFillBuilders, polygonSymbolizerBuilder, ( ) -> polygonSymbolizerBuilder.fill() );
    }


    public GraphicBuilder polygonGraphicBuilder( PolygonSymbolizerBuilder polygonSymbolizerBuilder ) {
        return mapEntry( polygonGraphicBuilders, polygonSymbolizerBuilder,
                ( ) -> polygonFill( polygonSymbolizerBuilder ).graphicFill() );
    }


    private <K, V> V mapEntry( Map<K,V> map, K key, Supplier<V> calc ) {
        V value = map.get( key );
        if (value == null) {
            value = calc.get();
            map.put( key, value );
        }
        return value;
    }
}
