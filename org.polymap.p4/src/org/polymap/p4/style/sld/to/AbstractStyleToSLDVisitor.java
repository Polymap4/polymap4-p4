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
package org.polymap.p4.style.sld.to;

import org.geotools.renderer.lite.gridcoverage2d.StyleVisitorAdapter;
import org.geotools.styling.builder.FeatureTypeStyleBuilder;
import org.geotools.styling.builder.NamedLayerBuilder;
import org.geotools.styling.builder.RuleBuilder;
import org.geotools.styling.builder.StyleBuilder;
import org.polymap.p4.style.SLDBuilder;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public abstract class AbstractStyleToSLDVisitor
        extends StyleVisitorAdapter {

    public abstract void fillSLD( SLDBuilder builder );


    protected StyleBuilder singletonStyle( SLDBuilder builder ) {
        NamedLayerBuilder namedLayer = builder.namedLayer();
        return builder.style( namedLayer );
    }


    protected FeatureTypeStyleBuilder singletonFeatureTypeStyle( SLDBuilder builder ) {
        StyleBuilder userStyle = singletonStyle( builder );
        return builder.featureTypeStyle( userStyle );
    }


    protected RuleBuilder singletonRule( SLDBuilder builder ) {
        FeatureTypeStyleBuilder featureTypeStyle = singletonFeatureTypeStyle( builder );
        return builder.rule( featureTypeStyle );
    }
}
