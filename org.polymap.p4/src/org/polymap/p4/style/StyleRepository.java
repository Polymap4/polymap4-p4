/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4.style;

import java.io.File;
import java.io.IOException;

import org.polymap.core.CorePlugin;
import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.store.recordstore.RecordStoreAdapter;
import org.polymap.p4.P4Plugin;
import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("unchecked")
public class StyleRepository {

    private static EntityRepository repo;

    static {
        try {
            File dir = new File( CorePlugin.getDataLocation( P4Plugin.instance() ), "style" );
            dir.mkdirs();
            LuceneRecordStore store = new LuceneRecordStore( dir, false );
            repo = EntityRepository.newConfiguration().entities.set( new Class[] { SimpleStyler.class } ).store.set(
                    new RecordStoreAdapter( store ) ).create();
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }


    public static UnitOfWork newUnitOfWork() {
        return repo.newUnitOfWork();
    }
}
