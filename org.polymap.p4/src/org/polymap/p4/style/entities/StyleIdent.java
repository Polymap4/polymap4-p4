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
import org.polymap.model2.DefaultValue;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.p4.style.SLDBuilder;
import org.polymap.p4.style.SimpleStyler;
import org.polymap.p4.style.sld.from.StyleIdentFromSLDVisitor;
import org.polymap.p4.style.sld.to.StyleIdentToSLDVisitor;

/**
 * Assumes combination of NamedLayer and UserStyle, where name is mapped to
 * NamedLayer.name and title/description to the UserStyle attributes title resp.
 * abstract.
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 * @see http://docs.geoserver.org/stable/en/user/styling/sld-reference/layers.html
 */
public class StyleIdent
        extends AbstractSLDModel {

    public static StyleIdent TYPE;


    @DefaultValue("MyStyle")
    public Property<String>      name;

    @Nullable
    public Property<String>      title;

    @Nullable
    public Property<String>      description;

    public Property<FeatureType> featureType;


    @Override
    public void fromSLD( StyledLayerDescriptor style ) {
        style.accept( new StyleIdentFromSLDVisitor( this ) );
    }


    @Override
    public void fillSLD( SLDBuilder builder ) {
        new StyleIdentToSLDVisitor( this ).fillSLD( builder );
    }
}
