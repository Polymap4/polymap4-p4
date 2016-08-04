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
package org.polymap.p4.data.importer.kml;

import java.util.NoSuchElementException;
import java.util.zip.ZipFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.referencing.CRS;
import org.geotools.xml.PullParser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;

import org.apache.commons.io.FilenameUtils;

/**
 * Read a KML or a KMZ file directly.
 *
 * @author Steffen Stundzig
 */
public class KMLFeatureIterator
        implements FeatureIterator<SimpleFeature> {

    final SimpleFeatureType   type;

    private SimpleFeature     f = null;

    private PullParser        parser;

    private final InputStream fis;

    private final ZipFile     zip;


    public KMLFeatureIterator( final File file, final String schemaName ) throws IOException {

        if (FilenameUtils.getExtension( file.getName() ).equals( "kmz" )) {
            zip = new ZipFile( file );
            // we use only the first element, but thekmz can contain more elements
            fis = zip.getInputStream( zip.entries().nextElement() );
        }
        else {
            zip = null;
            fis = new FileInputStream( file );
        }
        try {
            parser = new PullParser( new org.geotools.kml.v22.KMLConfiguration(), fis, org.geotools.kml.v22.KML.Placemark );
            f = (SimpleFeature)parser.parse();
            if (f == null) {
                // try older KML 2.1
                parser = new PullParser( new org.geotools.kml.KMLConfiguration(), fis, org.geotools.kml.KML.Placemark );
                f = (SimpleFeature)parser.parse();
            }

            SimpleFeatureType originalSchema = f != null ? f.getType() : null;
            if (originalSchema != null) {
//                originalSchema = SimpleFeatureTypeBuilder.retype( originalSchema, CRS.decode( "EPSG:4326" ) );
                GeometryDescriptor originalGeom = originalSchema.getGeometryDescriptor();
                GeometryType geomType = originalGeom.getType();
                if (f.getDefaultGeometryProperty() != null) {
                    geomType = new GeometryTypeImpl( f.getDefaultGeometryProperty().getName(), f.getDefaultGeometryProperty().getValue().getClass(), CRS.decode( "EPSG:4326" ), geomType.isIdentified(), geomType.isAbstract(), geomType.getRestrictions(), geomType.getSuper(), geomType.getDescription() );
                }
                GeometryDescriptor geom = new GeometryDescriptorImpl( geomType, geomType.getName(), originalGeom.getMinOccurs(), originalGeom.getMaxOccurs(), originalGeom.isNillable(), originalGeom.getDefaultValue() );
                type = new SimpleFeatureTypeImpl( new NameImpl( schemaName ), originalSchema.getAttributeDescriptors(), geom, geomType.isAbstract(), geomType.getRestrictions(), geomType.getSuper(), geomType.getDescription() );
            }
            else {
                type = null;
            }
        }
        catch (Exception e) {
            throw new IOException( "Error processing KML file", e );
        }
    }


    public SimpleFeatureType getFeatureType() {
        return type;
    }


    /**
     * Grab the next feature from the file.
     * 
     * @return feature
     * 
     * @throws IOException
     * @throws NoSuchElementException Check hasNext() to avoid reading off the end of
     *         the file
     */
    public SimpleFeature next() throws NoSuchElementException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final SimpleFeature next = new SimpleFeatureImpl( f.getAttributes(), type, null );
        forward();
        return next;
    }


    private void forward() {
        try {
            f = (SimpleFeature)parser.parse();
        }
        catch (Exception e) {
            throw new RuntimeException( "Error processing KML file", e );
        }
    }


    public boolean hasNext() {
        return f != null;
    }


    /**
     * Be sure to call close when you are finished with this reader; as it must close
     * the file it has open.
     * 
     * @throws IOException
     */
    public void close() {
        try {
            fis.close();
            if (zip != null) {
                zip.close();
            }
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
}
