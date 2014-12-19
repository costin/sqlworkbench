/*
 * SequenceReader.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import java.util.List;
import workbench.storage.DataStore;

/**
 * Read the definition of sequences from the database
 * @author support@sql-workbench.net
 */
public interface SequenceReader
{
	/**
	 *	Return a SQL String to recreate the given sequence
	 */
	String getSequenceSource(String owner, String sequence);

	/**
	 * 	Get a list of sequences for the given owner. The 
	 *  contains objects of type String.
	 */
	public List getSequenceList(String owner);
	
	public DataStore getSequenceDefinition(String owner, String sequence);
	
}