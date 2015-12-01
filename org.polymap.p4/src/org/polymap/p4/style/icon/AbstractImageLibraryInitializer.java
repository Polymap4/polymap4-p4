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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;
import org.polymap.rhei.field.ImageDescription;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public abstract class AbstractImageLibraryInitializer {

    private final ImageHelper                                           imageHelper;

    private final SortedMap<Pair<String,String>,List<ImageDescription>> imageLibrary;

    private final Map<String,ImageDescription>                          pathToImageDescription;


    public AbstractImageLibraryInitializer() {
        imageHelper = new ImageHelper();
        List<String> names = getNames();
        Comparator<Pair<String,String>> comparator = ( Pair<String,String> pair1, Pair<String,String> pair2 ) -> Integer
                .valueOf( names.indexOf( pair1.getLeft() ) ).compareTo( names.indexOf( pair2.getLeft() ) );
        imageLibrary = new TreeMap<Pair<String,String>,List<ImageDescription>>( comparator );
        pathToImageDescription = new HashMap<String,ImageDescription>();

        List<String> paths = new ArrayList<String>();
        fillPaths( names, paths );
        getImageHelper().createImageDescriptorsCalculator( paths );
    }


    protected abstract List<String> getNames();


    protected abstract void fillPaths( List<String> names, List<String> paths );

    protected void addToImageLibrary( String name, String listFile, String markUpLicenceText, List<String> paths ) {
        List<ImageDescription> imageDescriptors = new ArrayList<ImageDescription>();
        try (BufferedReader br = new BufferedReader( new InputStreamReader( getClass().getResourceAsStream( listFile ) ) )) {
            String line;
            while ((line = br.readLine()) != null) {
                final String imagePath = line;
                paths.add( imagePath );
                ImageDescription imageDescription = getImageHelper().createImageDescription( imagePath );
                imageDescriptors.add( imageDescription );
                pathToImageDescription.put( imagePath, imageDescription );
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        imageLibrary.put( Pair.of( name, markUpLicenceText ), imageDescriptors );
    }


    private ImageHelper getImageHelper() {
        return imageHelper;
    }


    public SortedMap<Pair<String,String>,List<ImageDescription>> getImageLibrary() {
        return imageLibrary;
    }


    public Map<String,ImageDescription> getPathToImageDescription() {
        return pathToImageDescription;
    }
}
