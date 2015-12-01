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
package org.polymap.p4.style.icon;

import java.util.List;
import java.util.SortedMap;

import org.apache.commons.lang3.tuple.Pair;
import org.polymap.p4.style.IFormFieldInfo;
import org.polymap.rhei.field.ImageDescription;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public interface IImageInfo
        extends IFormFieldInfo {

    /**
     * @return
     */
    SortedMap<Pair<String,String>,List<ImageDescription>> getImageLibrary();

    ImageDescription getImageDescriptionByPath(String path);

    /**
     * @param imageDescription
     */
    void setImageDescription( ImageDescription imageDescription );


    ImageDescription getImageDescription();
}
