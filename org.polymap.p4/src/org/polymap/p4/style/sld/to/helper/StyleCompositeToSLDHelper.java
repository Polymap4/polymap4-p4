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
package org.polymap.p4.style.sld.to.helper;

import java.util.function.Supplier;

import org.geotools.styling.builder.RuleBuilder;
import org.polymap.p4.style.SLDBuilder;
import org.polymap.p4.style.entities.StyleComposite;
import org.polymap.p4.style.sld.to.StyleLabelToSLDVisitor;
import org.polymap.p4.style.sld.to.StyleLineToSLDVisitor;
import org.polymap.p4.style.sld.to.StylePointToSLDVisitor;
import org.polymap.p4.style.sld.to.StylePolygonToSLDVisitor;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleCompositeToSLDHelper {

    private final StyleComposite styleComposite;


    public StyleCompositeToSLDHelper( StyleComposite styleComposite ) {
        this.styleComposite = styleComposite;
    }


    public void fillSLD( SLDBuilder builder, Supplier<RuleBuilder> ruleBuilderSupplier ) {
        styleComposite.stylePoints.forEach( stylePoint -> {
            RuleBuilder ruleBuilder = ruleBuilderSupplier.get();
            if (stylePoint.markerLabel.get() != null) {
                new StyleLabelToSLDVisitor( stylePoint.markerLabel.get() ).fillSLD( builder, ruleBuilder );
            }
            new StylePointToSLDVisitor( stylePoint ).fillSLD( builder, ruleBuilder );
        } );
        styleComposite.styleLines.forEach( styleLine -> {
            RuleBuilder ruleBuilder = ruleBuilderSupplier.get();
            if (styleLine.lineLabel.get() != null) {
                new StyleLabelToSLDVisitor( styleLine.lineLabel.get() ).fillSLD( builder, ruleBuilder );
            }
            new StyleLineToSLDVisitor( styleLine ).fillSLD( builder, ruleBuilder );
        } );
        styleComposite.stylePolygons.forEach( stylePolygon -> {
            RuleBuilder ruleBuilder = ruleBuilderSupplier.get();
            if (stylePolygon.polygonLabel.get() != null) {
                new StyleLabelToSLDVisitor( stylePolygon.polygonLabel.get() ).fillSLD( builder, ruleBuilder );
            }
            new StylePolygonToSLDVisitor( stylePolygon ).fillSLD( builder, ruleBuilder );
        } );
    }
}
