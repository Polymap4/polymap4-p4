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

import org.geotools.styling.builder.FeatureTypeStyleBuilder;
import org.geotools.styling.builder.RuleBuilder;
import org.polymap.p4.style.SLDBuilder;
import org.polymap.p4.style.entities.StyleConfiguration;
import org.polymap.p4.style.entities.StyleZoomConfiguration;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleConfigurationToSLDHelper {

    public void handleStyleConfiguration( StyleConfiguration styleConfiguration, SLDBuilder builder,
            FeatureTypeStyleBuilder featureTypeStyleBuilder ) {
        RuleBuilder ruleBuilder = featureTypeStyleBuilder.rule();
        if (styleConfiguration.configurationName.get() != null) {
            ruleBuilder.name( styleConfiguration.configurationName.get() );
            if (styleConfiguration.configurationTitle.get() != null) {
                ruleBuilder.title( styleConfiguration.configurationTitle.get() );
            }
        }
        if (styleConfiguration.styleZoomConfiguration.get() != null) {
            handleZoomConfiguration( styleConfiguration.styleZoomConfiguration.get(), ruleBuilder );
        }
        if (styleConfiguration.styleFilterConfiguration.get() != null) {
            new StyleFilterToSLDHelper().handleFilterConfiguration( styleConfiguration.styleFilterConfiguration.get(), ruleBuilder );
        }
        new StyleCompositeToSLDHelper( styleConfiguration.styleComposite.get() ).fillSLD( builder, ( ) -> ruleBuilder );
    }
    
    private void handleZoomConfiguration( StyleZoomConfiguration zoomConfiguration, RuleBuilder ruleBuilder ) {
        if (zoomConfiguration.minScaleDenominator.get() != null) {
            ruleBuilder.min( zoomConfiguration.minScaleDenominator.get() );
        }
        if (zoomConfiguration.maxScaleDenominator.get() != null) {
            ruleBuilder.max( zoomConfiguration.maxScaleDenominator.get() );
        }
    }
}
