/*
 * polymap.org Copyright (C) 2015 individual contributors as indicated by the
 * 
 * @authors tag. All rights reserved.
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
package org.polymap.p4.data.imports.osm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.collection.AbstractFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class IterableFeatureCollection
        extends AbstractFeatureCollection {

    private ReferencedEnvelope         env      = null;

    private int                        size     = 0;

    private static String              LAT_KEY  = "LAT";

    private static String              LONG_KEY = "LON";

    private final List<String>         keys;

    private Double                     minLon   = -1d, maxLon = -1d, minLat = -1d, maxLat = -1d;

    private final SimpleFeatureBuilder featureBuilder;

    private final OsmXmlIterator       iterator;

    private final InputStream          input;


    public IterableFeatureCollection( String typeName, File file, List<String> keys ) throws SchemaException,
            FileNotFoundException {
        super( getMemberType( typeName, keys ) );
        input = new FileInputStream( file );
        iterator = new OsmXmlIterator( input, false );
        featureBuilder = new SimpleFeatureBuilder( super.getSchema() );
        this.keys = keys;
    }


    public IterableFeatureCollection( String typeName, URL url, List<String> keys ) throws SchemaException, IOException
    {
        super( getMemberType( typeName, keys ) );
        input = url.openStream();
        iterator = new OsmXmlIterator( input, false );
        featureBuilder = new SimpleFeatureBuilder( super.getSchema() );
        this.keys = keys;
    }


    private static SimpleFeatureType getMemberType( String typeName, List<String> keys ) throws SchemaException {
        StringBuffer typeSpec = new StringBuffer();
        typeSpec.append( "LAT" );
        typeSpec.append( "LONG" );
        keys.stream().forEach( key -> typeSpec.append( key ).append( ":String" ) );
        return DataUtilities.createType( typeName, typeSpec.toString() );
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.collection.AbstractFeatureCollection#openIterator()
     */
    @Override
    protected Iterator<SimpleFeature> openIterator() {
        return new Iterator<SimpleFeature>() {

            private OsmNode currentNode = null;


            @Override
            public boolean hasNext() {
                boolean foundNextEntity = false;
                EntityContainer container;
                while (iterator.hasNext() && !foundNextEntity) {
                    container = iterator.next();
                    if (container.getType() == EntityType.Node) {
                        currentNode = (OsmNode)container.getEntity();
                    }
                }
                return iterator.hasNext();
            }


            @Override
            public SimpleFeature next() {
                double longitude = currentNode.getLongitude();
                double latitude = currentNode.getLatitude();
                Map<String,String> attributes = OsmModelUtil.getTagsAsMap( currentNode );
                attributes.put( LAT_KEY, String.valueOf( latitude ) );
                attributes.put( LONG_KEY, String.valueOf( longitude ) );
                boolean changed = false;
                if (minLon == -1 || minLon > longitude) {
                    minLon = longitude;
                    changed = true;
                }
                if (maxLon == -1 || maxLon < longitude) {
                    maxLon = longitude;
                    changed = true;
                }
                if (minLat == -1 || minLat > latitude) {
                    minLat = latitude;
                    changed = true;
                }
                if (maxLat == -1 || maxLat < latitude) {
                    maxLat = latitude;
                    changed = true;
                }
                featureBuilder.add( attributes.get( LAT_KEY ) );
                featureBuilder.add( attributes.get( LONG_KEY ) );
                for (String key : keys) {
                    featureBuilder.add( attributes.get( key ) );
                }
                size++;
                if (changed) {
                    env = null;
                }
                return featureBuilder.buildFeature( null );
            }
        };
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.collection.AbstractFeatureCollection#size()
     */
    @Override
    public int size() {
        return size;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.collection.AbstractFeatureCollection#getBounds()
     */
    @Override
    public ReferencedEnvelope getBounds() {
        if (env == null) {
            env = new ReferencedEnvelope( minLon, maxLon, minLat, maxLat, schema.getCoordinateReferenceSystem() );
        }
        return env;
    }


    public void complete() throws IOException {
        iterator.complete();
        input.close();
    }
}
