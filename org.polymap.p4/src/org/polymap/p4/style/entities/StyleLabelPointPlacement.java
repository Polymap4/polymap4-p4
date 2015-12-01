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

import org.polymap.model2.Nullable;
import org.polymap.model2.Property;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 * @see http
 *      ://docs.geoserver.org/stable/en/user/styling/sld-reference/textsymbolizer.
 *      html#sld-reference-textsymbolizer
 * @see http 
 *      ://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#
 *      labeling -priority
 */
public class StyleLabelPointPlacement
        extends AbstractSLDModelFragment {

    @Nullable
    public Property<StyleCoord> offset;

    @Nullable
    public Property<StyleCoord> anchor;

    @Nullable
    public Property<Double>     rotation;

    // GeoServer vendor options: please note 
    // that the attribute name must match the name in the vendor options
    // as this is assumed in org.polymap.p4.style.sld.from.AbstractStyleFromSLDVisitor.
    // handleVendorOption(TextSymbolizer, Property<T>, Function<String, T>)

    @Nullable
    public Property<Double>     maxDisplacement;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anchor == null) ? 0 : anchor.hashCode());
        result = prime * result + ((maxDisplacement == null) ? 0 : maxDisplacement.hashCode());
        result = prime * result + ((offset == null) ? 0 : offset.hashCode());
        result = prime * result + ((rotation == null) ? 0 : rotation.hashCode());
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if(obj == null) {
            return false;
        }
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        StyleLabelPointPlacement other = (StyleLabelPointPlacement)obj;
        if (anchor == null) {
            if (other.anchor != null)
                return false;
        }
        else if (!anchor.equals( other.anchor ))
            return false;
        if (maxDisplacement == null) {
            if (other.maxDisplacement != null)
                return false;
        }
        else if (!maxDisplacement.equals( other.maxDisplacement ))
            return false;
        if (offset == null) {
            if (other.offset != null)
                return false;
        }
        else if (!offset.equals( other.offset ))
            return false;
        if (rotation == null) {
            if (other.rotation != null)
                return false;
        }
        else if (!rotation.equals( other.rotation ))
            return false;
        return true;
    }
}
