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

import java.util.ArrayList;
import java.util.List;

import org.geotools.styling.StyledLayerDescriptor;
import org.opengis.feature.type.FeatureType;
import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Property;
import org.polymap.p4.style.entities.AbstractSLDModel;
import org.polymap.p4.style.entities.StyleFeature;
import org.polymap.p4.style.entities.StyleIdent;

/**
 * Configuration of simple stylers.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SimpleStyler
        extends AbstractStyler {

    public static SimpleStyler TYPE;

    public Property<StyleIdent>             styleIdent;

    public CollectionProperty<StyleFeature> styleFeatures;

    private List<AbstractSLDModel>          fragments = null;


    @Override
    public void init( FeatureType schema ) {
        // TODO Auto-generated method stub

    }


    @Override
    public void fillSLD( SLDBuilder builder, List<AbstractStyler> children ) {
        for (AbstractSLDModel sldFragment : getSldFragments()) {
            sldFragment.fillSLD( builder );

            // XXX mit result vereinen

        }
    }


    public List<? extends AbstractSLDModel> getSldFragments() {
        if (fragments == null) {
            fragments = new ArrayList<AbstractSLDModel>();
            fragments.add( styleIdent.get() );
            styleFeatures.forEach( styleFeature -> fragments.add( styleFeature ) );
        }
        return fragments;
    }


    @Override
    public void fromSLD( StyledLayerDescriptor sld ) {
        getSldFragments().stream().forEach( prop -> prop.fromSLD( sld ) );
    }
}
