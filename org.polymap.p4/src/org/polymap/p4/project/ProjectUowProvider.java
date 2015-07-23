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
package org.polymap.p4.project;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;

import org.polymap.rhei.batik.tx.TxProvider;

import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.store.recordstore.RecordStoreAdapter;
import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * Holds the global {@link EntityRepository} of {@link org.polymap.core.project}
 * entities, initializes the repo and provides {@link UnitOfWork} instances to the
 * panels.
 *
 * @see TxProvider
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectUowProvider
        extends TxProvider<UnitOfWork> {

    private static Log log = LogFactory.getLog( ProjectUowProvider.class );
    
    private EntityRepository    repo;

    
    public ProjectUowProvider() throws IOException {
        //File workspace;
        LuceneRecordStore store = new LuceneRecordStore();
        repo = EntityRepository.newConfiguration()
                .entities.set( new Class[] {ILayer.class, IMap.class} )
                .store.set( new RecordStoreAdapter( store ) )
                .create();
        
        initRepo();
    }
    
    protected void initRepo() {
        try (UnitOfWork uow = repo.newUnitOfWork()) {
            if (uow.entity( IMap.class, "root" ) == null) {
                uow.createEntity( IMap.class, "root", (IMap prototype) -> {
                    prototype.label.set( "The Map" );
                    prototype.visible.set( true );
                    return prototype;
                });
                uow.commit();
            }
        }
    }
    
    @Override
    protected UnitOfWork newTx( UnitOfWork parent ) {
        return parent != null ? parent.newUnitOfWork() : repo.newUnitOfWork();
    }

    @Override
    protected void commitTx( UnitOfWork uow ) {
        uow.commit();
    }

    @Override
    protected void rollbackTx( UnitOfWork uow ) {
        uow.rollback();
    }

    @Override
    protected void closeTx( UnitOfWork uow ) {
        uow.close();
    }
    
}
