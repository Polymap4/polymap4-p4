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
package org.polymap.p4.style.sld.from;

import java.util.List;

import org.opengis.filter.And;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNil;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.opengis.filter.temporal.After;
import org.opengis.filter.temporal.AnyInteracts;
import org.opengis.filter.temporal.Before;
import org.opengis.filter.temporal.Begins;
import org.opengis.filter.temporal.BegunBy;
import org.opengis.filter.temporal.During;
import org.opengis.filter.temporal.EndedBy;
import org.opengis.filter.temporal.Ends;
import org.opengis.filter.temporal.Meets;
import org.opengis.filter.temporal.MetBy;
import org.opengis.filter.temporal.OverlappedBy;
import org.opengis.filter.temporal.TContains;
import org.opengis.filter.temporal.TEquals;
import org.opengis.filter.temporal.TOverlaps;
import org.polymap.p4.style.entities.StyleCompositeFilter;
import org.polymap.p4.style.entities.StyleFilter;
import org.polymap.p4.style.entities.StyleFilterConfiguration;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleFilterFromSLDVisitor
        implements FilterVisitor {

    private final StyleFilterConfiguration styleFilterConfiguration;

    /**
     * @param styleFilterConfiguration
     */
    public StyleFilterFromSLDVisitor( StyleFilterConfiguration styleFilterConfiguration ) {
        this.styleFilterConfiguration = styleFilterConfiguration;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visitNullFilter(java.lang.Object)
     */
    @Override
    public Object visitNullFilter( Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.ExcludeFilter,
     * java.lang.Object)
     */
    @Override
    public Object visit( ExcludeFilter filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.IncludeFilter,
     * java.lang.Object)
     */
    @Override
    public Object visit( IncludeFilter filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.And,
     * java.lang.Object)
     */
    @Override
    public Object visit( And filter, Object extraData ) {
        handleJoinFilter( And.class, filter.getChildren() );
        return null;
    }


    private void handleJoinFilter( Class<?> filter, List<Filter> children ) {
        StyleCompositeFilter complexFilter = styleFilterConfiguration.complexFilter.createValue( f -> {
            f.predicate.set( filter.getSimpleName() );
            return f;
        } );
        StyleComplexFilterFromSLDVisitor styleComplexFilterFromSLDVisitor = new StyleComplexFilterFromSLDVisitor(complexFilter);
        children.stream().forEach( f -> f.accept( styleComplexFilterFromSLDVisitor, null ) );
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.Id,
     * java.lang.Object)
     */
    @Override
    public Object visit( Id filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.Not,
     * java.lang.Object)
     */
    @Override
    public Object visit( Not filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.Or,
     * java.lang.Object)
     */
    @Override
    public Object visit( Or filter, Object extraData ) {
        handleJoinFilter( Or.class, filter.getChildren() );
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.PropertyIsBetween,
     * java.lang.Object)
     */
    @Override
    public Object visit( PropertyIsBetween filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.PropertyIsEqualTo,
     * java.lang.Object)
     */
    @Override
    public Object visit( PropertyIsEqualTo filter, Object extraData ) {
        handleBinaryFilter( filter, PropertyIsEqualTo.NAME );
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.PropertyIsNotEqualTo
     * , java.lang.Object)
     */
    @Override
    public Object visit( PropertyIsNotEqualTo filter, Object extraData ) {
        handleBinaryFilter( filter, PropertyIsNotEqualTo.NAME );
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.PropertyIsGreaterThan
     * , java.lang.Object)
     */
    @Override
    public Object visit( PropertyIsGreaterThan filter, Object extraData ) {
        handleBinaryFilter( filter, PropertyIsGreaterThan.NAME );
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.
     * PropertyIsGreaterThanOrEqualTo, java.lang.Object)
     */
    @Override
    public Object visit( PropertyIsGreaterThanOrEqualTo filter, Object extraData ) {
        handleBinaryFilter( filter, PropertyIsGreaterThanOrEqualTo.NAME );
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.PropertyIsLessThan,
     * java.lang.Object)
     */
    @Override
    public Object visit( PropertyIsLessThan filter, Object extraData ) {
        handleBinaryFilter( filter, PropertyIsLessThan.NAME );
        return null;
    }


    private void handleBinaryFilter( BinaryComparisonOperator filter, String opName ) {
        String propertyName = (filter.getExpression1() instanceof PropertyName) ?
                ((PropertyName)filter.getExpression1()).getPropertyName() : "unknown";
        Object value = (filter.getExpression2() instanceof Literal) ? ((Literal)filter.getExpression2()).getValue()
                : null;
        StyleFilter simpleFilter = styleFilterConfiguration.simpleFilter.createValue( f -> {
            f.propertyName.set( propertyName );
            f.predicate.set( opName );
            return f;
        } );
        simpleFilter.value.set( value );
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.
     * PropertyIsLessThanOrEqualTo, java.lang.Object)
     */
    @Override
    public Object visit( PropertyIsLessThanOrEqualTo filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.PropertyIsLike,
     * java.lang.Object)
     */
    @Override
    public Object visit( PropertyIsLike filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.PropertyIsNull,
     * java.lang.Object)
     */
    @Override
    public Object visit( PropertyIsNull filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.PropertyIsNil,
     * java.lang.Object)
     */
    @Override
    public Object visit( PropertyIsNil filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.spatial.BBOX,
     * java.lang.Object)
     */
    @Override
    public Object visit( BBOX filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.spatial.Beyond,
     * java.lang.Object)
     */
    @Override
    public Object visit( Beyond filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.spatial.Contains,
     * java.lang.Object)
     */
    @Override
    public Object visit( Contains filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.spatial.Crosses,
     * java.lang.Object)
     */
    @Override
    public Object visit( Crosses filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.spatial.Disjoint,
     * java.lang.Object)
     */
    @Override
    public Object visit( Disjoint filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.spatial.DWithin,
     * java.lang.Object)
     */
    @Override
    public Object visit( DWithin filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.spatial.Equals,
     * java.lang.Object)
     */
    @Override
    public Object visit( Equals filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.spatial.Intersects,
     * java.lang.Object)
     */
    @Override
    public Object visit( Intersects filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.spatial.Overlaps,
     * java.lang.Object)
     */
    @Override
    public Object visit( Overlaps filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.spatial.Touches,
     * java.lang.Object)
     */
    @Override
    public Object visit( Touches filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.spatial.Within,
     * java.lang.Object)
     */
    @Override
    public Object visit( Within filter, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.temporal.After,
     * java.lang.Object)
     */
    @Override
    public Object visit( After after, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.temporal.AnyInteracts
     * , java.lang.Object)
     */
    @Override
    public Object visit( AnyInteracts anyInteracts, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.temporal.Before,
     * java.lang.Object)
     */
    @Override
    public Object visit( Before before, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.temporal.Begins,
     * java.lang.Object)
     */
    @Override
    public Object visit( Begins begins, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.temporal.BegunBy,
     * java.lang.Object)
     */
    @Override
    public Object visit( BegunBy begunBy, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.temporal.During,
     * java.lang.Object)
     */
    @Override
    public Object visit( During during, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.temporal.EndedBy,
     * java.lang.Object)
     */
    @Override
    public Object visit( EndedBy endedBy, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.temporal.Ends,
     * java.lang.Object)
     */
    @Override
    public Object visit( Ends ends, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.temporal.Meets,
     * java.lang.Object)
     */
    @Override
    public Object visit( Meets meets, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.filter.FilterVisitor#visit(org.opengis.filter.temporal.MetBy,
     * java.lang.Object)
     */
    @Override
    public Object visit( MetBy metBy, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.temporal.OverlappedBy
     * , java.lang.Object)
     */
    @Override
    public Object visit( OverlappedBy overlappedBy, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.temporal.TContains,
     * java.lang.Object)
     */
    @Override
    public Object visit( TContains contains, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.temporal.TEquals,
     * java.lang.Object)
     */
    @Override
    public Object visit( TEquals equals, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.filter.FilterVisitor#visit(org.opengis.filter.temporal.TOverlaps,
     * java.lang.Object)
     */
    @Override
    public Object visit( TOverlaps contains, Object extraData ) {
        // TODO Auto-generated method stub
        return null;
    }

}
