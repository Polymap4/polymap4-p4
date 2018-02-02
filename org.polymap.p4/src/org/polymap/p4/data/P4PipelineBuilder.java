/* 
 * polymap.org
 * Copyright (C) 2015-2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4.data;

import org.polymap.core.data.feature.DataSourceProcessor;
import org.polymap.core.data.feature.FeatureRenderProcessor2;
import org.polymap.core.data.image.ImageDecodeProcessor;
import org.polymap.core.data.image.ImageEncodeProcessor;
import org.polymap.core.data.pipeline.AutoWirePipelineBuilder;
import org.polymap.core.data.pipeline.Param.ParamsHolder;
import org.polymap.core.data.pipeline.PipelineBuilder;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.PipelineProcessorSite.Params;
import org.polymap.core.data.pipeline.ProcessorDescriptor;
import org.polymap.core.data.raster.RasterRenderProcessor;
import org.polymap.core.data.wms.WmsRenderProcessor;
import org.polymap.core.project.ILayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class P4PipelineBuilder
        extends AutoWirePipelineBuilder
        implements PipelineBuilder, ParamsHolder {

    /** Terminal and transformer processors. */
    private static final Class<PipelineProcessor>[] defaultProcTypes = new Class[] {
        ImageEncodeProcessor.class,
        ImageDecodeProcessor.class,
        FeatureRenderProcessor2.class,
        DataSourceProcessor.class,
        WmsRenderProcessor.class,
        RasterRenderProcessor.class,
    };

    
    public static P4PipelineBuilder forLayer( ILayer layer ) {
        return new P4PipelineBuilder();
    }
    
    
    // instance *******************************************
    
    private Params          params = new Params();
    
    
    public P4PipelineBuilder() {
        super( defaultProcTypes );
    }
    
    @Override
    public Params params() {
        return params;
    }
    
    @Override
    protected PipelineProcessorSite createProcessorSite( ProcessorDescriptor procDesc ) {
        Params all = new Params();
        all.putAll( params );
        all.putAll( procDesc.params() );
        return new PipelineProcessorSite( all );
    }
    
}
