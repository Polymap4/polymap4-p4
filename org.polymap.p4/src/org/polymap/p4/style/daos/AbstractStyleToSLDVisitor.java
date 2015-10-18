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
package org.polymap.p4.style.daos;

import org.eclipse.swt.graphics.RGB;
import org.geotools.renderer.lite.gridcoverage2d.StyleVisitorAdapter;
import org.geotools.styling.builder.FeatureTypeStyleBuilder;
import org.geotools.styling.builder.NamedLayerBuilder;
import org.geotools.styling.builder.RuleBuilder;
import org.geotools.styling.builder.StyleBuilder;


/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public abstract class AbstractStyleToSLDVisitor extends StyleVisitorAdapter {
    
    protected java.awt.Color toAwtColor( RGB rgb ) {
        return new java.awt.Color( rgb.red, rgb.green, rgb.blue );
    }
    
    public abstract void fillSLD(SLDBuilder builder);    
    
    protected RuleBuilder getRuleBuilder( SLDBuilder builder ) {
        NamedLayerBuilder namedLayer = builder.namedLayer();
        StyleBuilder userStyle = builder.style(namedLayer);
        FeatureTypeStyleBuilder featureTypeStyle = builder.featureTypeStyle(userStyle);
        RuleBuilder ruleBuilder = builder.rule(featureTypeStyle);
        return ruleBuilder;
    }
    
}
