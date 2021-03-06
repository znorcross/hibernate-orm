package org.hibernate.hql.spi.id.inline;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.CompositeType;
import org.hibernate.type.LiteralType;
import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;

/**
 * Builds the where clause that wraps the identifiers to be updated/deleted.
 *
 * @author Vlad Mihalcea
 */
public abstract class IdsClauseBuilder {

	private final Dialect dialect;

	private final Type identifierType;

	private final TypeResolver typeResolver;

	private final String[] columns;

	private final List<Object[]> ids;

	protected IdsClauseBuilder(
			Dialect dialect,
			Type identifierType,
			TypeResolver typeResolver,
			String[] columns,
			List<Object[]> ids) {
		this.dialect = dialect;
		this.identifierType = identifierType;
		this.typeResolver = typeResolver;
		this.columns = columns;
		this.ids = ids;
	}

	public Type getIdentifierType() {
		return identifierType;
	}

	public TypeResolver getTypeResolver() {
		return typeResolver;
	}

	protected String[] getColumns() {
		return columns;
	}

	public List<Object[]> getIds() {
		return ids;
	}

	public abstract String toStatement();

	protected String quoteIdentifier(Object... value) {
		if ( value.length == 1 ) {
			return quoteIdentifier( value[0], identifierType );
		}
		else {
			if ( identifierType instanceof CompositeType ) {
				CompositeType compositeType = (CompositeType) identifierType;
				List<String> quotedIdentifiers = new ArrayList<>();

				for ( int i = 0; i < value.length; i++ ) {
					quotedIdentifiers.add(quoteIdentifier( value[i], compositeType.getSubtypes()[i] ));
				}
				return String.join( ",", quotedIdentifiers );
			}
			else {
				throw new IllegalArgumentException("Composite identifier does not implement CompositeType");
			}
		}
	}

	private String quoteIdentifier(Object value, Type type) {
		Type resolvedType = ( !type.getReturnedClass().equals( value.getClass() ) ) ?
			typeResolver.heuristicType( value.getClass().getName() ) : type;

		if ( resolvedType instanceof LiteralType ) {
			LiteralType literalType = (LiteralType) resolvedType;
			try {
				return literalType.objectToSQLString( value, dialect );
			}
			catch ( Exception e ) {
				throw new IllegalArgumentException( e );
			}
		}
		return String.valueOf( value );
	}
}
