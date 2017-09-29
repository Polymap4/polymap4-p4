/* 
 * polymap.org
 * Copyright (C) 2013-2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.p4.data.importer.archive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.DefaultInt;
import org.polymap.core.runtime.config.Mandatory;

import org.polymap.p4.P4Plugin;

/**
 * Copy files into a (temporary) directory. Handles archives down to
 * {@link #maxArchiveLevel}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ArchiveReader
        extends Configurable {

    private static final Log log = LogFactory.getLog( ArchiveReader.class );
    
    public static final List<String>        EXTS = Arrays.asList( "zip", "jar", "kmz", 
            "tar",
            "gz", "gzip", "tgz",
            "bz2", "bzip2", "tbz" );
    
    /** Defaults to a automatically created temp dir. */
    @Mandatory
    public Config2<ArchiveReader,File>      targetDir;
    
    @Mandatory
    @DefaultBoolean( false )
    public Config2<ArchiveReader,Boolean>   overwrite;
    
    @Mandatory
    @DefaultInt( 1 )
    public Config2<ArchiveReader,Integer>   maxArchiveLevel;
    
    /** Charset for ZIP. Defaults to UTF8. */
    @Mandatory
    public Config2<ArchiveReader,Charset>   charset;
    
    private IProgressMonitor                monitor;
    
    private List<File>                      results = new ArrayList();
    
    
    public ArchiveReader() {
        charset.set( Charset.forName( "UTF8" ) );
        try {
            targetDir.set( Files.createTempDirectory( P4Plugin.ID + "-" ).toFile() );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
    

    public boolean canHandle( File f, @SuppressWarnings("hiding") IProgressMonitor monitor ) {
        String ext = FilenameUtils.getExtension( f.getName() ).toLowerCase();
        return EXTS.contains( ext );
        
        // XXX check content type / magic number if extension failed
    }
    
    
    /**
     * 
     *
     * @return List of read files.
     * @throws RuntimeException
     */
    public List<File> run( File f, @SuppressWarnings("hiding") IProgressMonitor monitor ) throws Exception {
        this.monitor = monitor;
        try (
            InputStream in = new BufferedInputStream( new FileInputStream( f ) ); 
        ){
            handle( targetDir.get(), f.getName(), null, in, 0 );
            return results;
        }
        finally {
            this.monitor = null;
        }
    }


    protected void handle( File dir, String name, String contentType, InputStream in, int archiveLevel ) throws Exception {
        if (monitor.isCanceled()) {
            return;
        }
        monitor.subTask( name );
        
        contentType = contentType == null ? "" : contentType;
        String lcname = name.toLowerCase();
        
        // stop flattening if maxArchiveLevel is reached
        if (archiveLevel >= maxArchiveLevel.get()) {
            handleFile( dir, name, in );            
        }
        else if (lcname.endsWith( ".zip" ) 
                || lcname.endsWith( ".jar" ) 
                || lcname.endsWith( ".kmz" ) 
                || contentType.equalsIgnoreCase( "application/zip" )) {
            handleZip( dir, name, in, archiveLevel );
        }
        else if (lcname.endsWith( ".tar" ) || contentType.equalsIgnoreCase( "application/tar" )) {
            handleTar( dir, name, in, archiveLevel );
        }
        else if (lcname.endsWith( ".gz" ) || lcname.endsWith( ".gzip" ) || lcname.endsWith( ".tgz" ) 
                || contentType.equalsIgnoreCase( "application/gzip" )) {
            handleGzip( dir, name, in, archiveLevel );
        }
        else if (lcname.endsWith( ".bz2" ) || lcname.endsWith( ".bzip2" ) || lcname.endsWith( ".tbz" ) 
                || contentType.equalsIgnoreCase( "application/bzip2" )) {
            handleBzip2( dir, name, in, archiveLevel );
        }
        else {
            handleFile( dir, name, in );
        }
        monitor.worked( 1 );
    }
    
    
    protected void handleGzip( File dir, String name, InputStream in, int archiveLevel ) throws Exception {
        log.info( "    GZIP: " + name );
        try (GZIPInputStream gzip = new GZIPInputStream( in )) {
            String nextName = null;
            if (name.toLowerCase().endsWith( ".gz" )) {
                nextName = name.substring( 0, name.length() - 3 );
            }
            else if (name.toLowerCase().endsWith( ".tgz" )) {
                nextName = name.substring( 0, name.length() - 3 ) + "tar";
            }
            else {
                nextName = name.substring( 0, name.length() - 2 );            
            }
            handle( dir, nextName, null, gzip, archiveLevel );
        }
    }


    protected void handleBzip2( File dir, String name, InputStream in, int archiveLevel ) throws Exception {
        log.info( "    BZIP2: " + name );
        try (
            BZip2CompressorInputStream bzip2 = new BZip2CompressorInputStream( in, true );
        ){
            String nextName = null;
            if (name.toLowerCase().endsWith( ".bz2" )) {
                nextName = name.substring( 0, name.length() - 4 );
            }
            else if (name.toLowerCase().endsWith( ".tbz" )) {
                nextName = name.substring( 0, name.length() - 3 ) + "tar";
            }
            else {
                nextName = name.substring( 0, name.length() - 3 );            
            }
            handle( dir, nextName, null, bzip2, archiveLevel );
        }
    }


    protected void handleFile( File dir, String name, InputStream in ) throws Exception {
        log.info( "    FILE: " + dir.getName() + " / "+ name );
        File target = new File( dir, FilenameUtils.getName( name ) );
        
        if (!overwrite.get() && target.exists()) {
            throw new RuntimeException( "Unable to flatten contents of archive. File already exists: " + target );
        }
        try (
            OutputStream out = new FileOutputStream( target );
        ){
            IOUtils.copy( in, out );
        }
        results.add( target );
    }
    
    
    protected void handleZip( File dir, String name, InputStream in, int archiveLevel ) throws Exception {
        log.info( "    ZIP: " + name );
        try {
            ZipInputStream zip = new ZipInputStream( in, charset.get() );
            ZipEntry entry = null;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String path = FilenameUtils.getPath( entry.getName() );
                    File subdir = new File( dir, path );  // XXX check separator?
                    subdir.mkdirs();

                    handle( subdir, FilenameUtils.getName( entry.getName() ), null, zip, archiveLevel+1 );
                }
            }
        }
        catch (Exception e) {
            if (e instanceof IllegalArgumentException || "MALFORMED".equals( e.getMessage() )) {
                throw new IOException( "Wrong charset: " + charset.get().displayName(), e );
            }
            else {
                throw e;
            }
        }
    }


    protected void handleTar( File dir, String name, InputStream in, int archiveLevel ) throws Exception {
        log.info( "    TAR: " + name );
        try (
            TarArchiveInputStream tar = new TarArchiveInputStream( in, charset.get().name() )
        ){
            ArchiveEntry entry = null;
            while ((entry = tar.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    // skip dirs
//                    subdir = new File( subdir, entry.getName() );
//                    subdir.mkdir();                    
                }
                else {
                    String path = FilenameUtils.getPath( entry.getName() );
                    File subdir = new File( dir, path );  // XXX check separator?
                    subdir.mkdirs();
                    
                    handle( subdir, FilenameUtils.getName( entry.getName() ), null, tar, archiveLevel+1 );
                }
            }
        }
    }

}
