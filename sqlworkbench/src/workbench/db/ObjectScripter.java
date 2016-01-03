/*
 * ObjectScripter.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2016, Thomas Kellerer
 *
 * Licensed under a modified Apache License, Version 2.0
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://sql-workbench.net/manual/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import workbench.interfaces.ScriptGenerationMonitor;
import workbench.interfaces.Scripter;
import workbench.interfaces.TextOutput;
import workbench.log.LogMgr;
import workbench.resource.DbExplorerSettings;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

import workbench.sql.DelimiterDefinition;

import workbench.util.CollectionUtil;
import workbench.util.ExceptionUtil;
import workbench.util.StringBuilderOutput;
import workbench.util.StringUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class ObjectScripter
	implements Scripter
{
	public static final String TYPE_TABLE = "table";
	public static final String TYPE_TYPE = "type";
	public static final String TYPE_RULE = "rule";
	public static final String TYPE_VIEW = "view";
	public static final String TYPE_SYNONYM = "synonym";
	public static final String TYPE_INSERT = "insert";
	public static final String TYPE_UPDATE = "update";
	public static final String TYPE_SELECT = "select";
	public static final String TYPE_PROC = "procedure";
	public static final String TYPE_PACKAGE = "package";
	public static final String TYPE_FUNC = "function";
	public static final String TYPE_TRG = "trigger";
	public static final String TYPE_DOMAIN = "domain";
	public static final String TYPE_ENUM = "enum";
	public static final String TYPE_MVIEW = DbMetadata.MVIEW_NAME.toLowerCase();

	private List<? extends DbObject> objectList;
	private ScriptGenerationMonitor progressMonitor;
	private WbConnection dbConnection;
	private boolean cancel;
	private String nl = Settings.getInstance().getInternalEditorLineEnding();
	private Collection<String> commitTypes;
	private boolean appendCommit;
	private boolean useSeparator;
	private boolean includeDrop;
	private boolean includeGrants = true;
	private Collection<String> typesWithoutSeparator;
	private String sequenceType;
	private String synonymType = TYPE_SYNONYM;
	private GenericObjectDropper dropper;
  private boolean extractPackageProcedure;
  private boolean needsAlternateDelimiter;
  private Set<String> additionalTableTypes = CollectionUtil.caseInsensitiveSet();
  private Set<String> additionalViewTypes = CollectionUtil.caseInsensitiveSet();
  private Set<String> typesToGenerate = CollectionUtil.caseInsensitiveSet();
  private int currentObject;
  private int totalObjects;
  private TextOutput output;

	public ObjectScripter(List<? extends DbObject> objects, WbConnection aConnection)
	{
		this.objectList = objects;
		this.dbConnection = aConnection;
    totalObjects = objectList.size();

		SequenceReader reader = aConnection.getMetadata().getSequenceReader();
		if (reader != null)
		{
			sequenceType = reader.getSequenceTypeName();
		}

    SynonymReader synReader = aConnection.getMetadata().getSynonymReader();
    if (synReader != null)
    {
      synonymType = synReader.getSynonymTypeName();
    }

    additionalTableTypes.addAll(dbConnection.getMetadata().getTableTypes());
    additionalViewTypes.addAll(dbConnection.getMetadata().getViewTypes());

    additionalViewTypes.remove(TYPE_VIEW);
    additionalTableTypes.remove(TYPE_TABLE);

		commitTypes = CollectionUtil.caseInsensitiveSet(TYPE_TABLE, TYPE_VIEW, TYPE_PACKAGE, TYPE_PROC, TYPE_FUNC, TYPE_TRG, TYPE_DOMAIN, TYPE_ENUM, TYPE_TYPE, TYPE_RULE);
    commitTypes.addAll(additionalTableTypes);

		if (sequenceType != null)
		{
			commitTypes.add(sequenceType.toLowerCase());
		}

    if (synonymType != null)
		{
			commitTypes.add(synonymType.toLowerCase());
		}

    useSeparator = DbExplorerSettings.getGenerateScriptSeparator();

		typesWithoutSeparator = CollectionUtil.caseInsensitiveSet(TYPE_SELECT, TYPE_INSERT, TYPE_UPDATE);
		dropper = new GenericObjectDropper();
		dropper.setConnection(dbConnection);
		dropper.setCascade(true);

    for (DbObject dbo : objects)
    {
      typesToGenerate.add(dbo.getObjectType());
    }

    Set<String> altDelimTypes = aConnection.getDbSettings().getTypesRequiringAlternateDelimiter();
    for (String type : altDelimTypes)
    {
      if (typesToGenerate.contains(type))
      {
        needsAlternateDelimiter = true;
        break;
      }
    }
	}

	@Override
	public WbConnection getCurrentConnection()
	{
		return dbConnection;
	}

	public void setIncludeGrants(boolean flag)
	{
		this.includeGrants = flag;
	}

	public void setIncludeDrop(boolean flag)
	{
		includeDrop = flag;
	}

	public void setUseSeparator(boolean flag)
	{
		this.useSeparator = flag;
	}

	@Override
	public void setProgressMonitor(ScriptGenerationMonitor aMonitor)
	{
		this.progressMonitor = aMonitor;
	}

	@Override
	public String getScript()
	{
    output = new StringBuilderOutput(totalObjects * 50);
		generateScript();
		return output.toString();
	}

  @Override
  public void setTextOutput(TextOutput out)
  {
    output = out;
  }

	@Override
	public boolean isCancelled()
	{
		return this.cancel;
	}

	@Override
	public void generateScript()
	{
		try
		{
      currentObject = 1;

			this.dbConnection.setBusy(true);
			this.cancel = false;

			if (!cancel && sequenceType != null)
      {
        appendObjectType(sequenceType);
        typesToGenerate.remove(sequenceType);
      }

			if (!cancel) this.appendObjectType(TYPE_ENUM);
      typesToGenerate.remove(TYPE_ENUM);

			if (!cancel) this.appendObjectType(TYPE_TYPE);
      typesToGenerate.remove(TYPE_TYPE);

			if (!cancel) this.appendObjectType(TYPE_DOMAIN);
			typesToGenerate.remove(TYPE_DOMAIN);

			if (!cancel) this.appendObjectType(TYPE_TABLE);
      typesToGenerate.remove(TYPE_TABLE);

      for (String type : additionalTableTypes)
      {
        if (cancel) break;
        appendObjectType(type);
        typesToGenerate.remove(type);
      }

			if (!cancel) this.appendForeignKeys();

			if (!cancel) this.appendObjectType(TYPE_VIEW);
      typesToGenerate.remove(TYPE_VIEW);

      for (String type : additionalViewTypes)
      {
        if (cancel) break;
        this.appendObjectType(type);
        typesToGenerate.remove(type);
      }

			if (!cancel && synonymType != null)
      {
        appendObjectType(synonymType);
        typesToGenerate.remove(synonymType);
      }

			if (!cancel) this.appendObjectType(TYPE_MVIEW);
      typesToGenerate.remove(TYPE_MVIEW);

			if (!cancel) this.appendObjectType(TYPE_INSERT);
      typesToGenerate.remove(TYPE_INSERT);

			if (!cancel) this.appendObjectType(TYPE_UPDATE);
      typesToGenerate.remove(TYPE_UPDATE);

			if (!cancel) this.appendObjectType(TYPE_SELECT);
      typesToGenerate.remove(TYPE_SELECT);

			if (!cancel) this.appendObjectType(TYPE_FUNC);
      typesToGenerate.remove(TYPE_FUNC);

			if (!cancel) this.appendObjectType(TYPE_PROC);
      typesToGenerate.remove(TYPE_PROC);

			if (!cancel) this.appendObjectType(TYPE_PACKAGE);
      typesToGenerate.remove(TYPE_PACKAGE);

			if (!cancel) this.appendObjectType(TYPE_TRG);
      typesToGenerate.remove(TYPE_TRG);

			if (!cancel) this.appendObjectType(TYPE_RULE);
      typesToGenerate.remove(TYPE_RULE);

      // everything else
      for (String type : typesToGenerate)
      {
        this.appendObjectType(type);
      }
		}
    catch (Exception ex)
    {
      LogMgr.logError("ObjectScript.generateScript()", "Could not generate script", ex);
    }
		finally
		{
			this.dbConnection.setBusy(false);
		}
		if (appendCommit && this.dbConnection.getDbSettings().ddlNeedsCommit())
		{
			output.append("\nCOMMIT");
      DelimiterDefinition delim = getDelimiter();
      output.append(delim.getScriptText());
		}
	}

  private DelimiterDefinition getDelimiter()
  {
    if (needsAlternateDelimiter)
    {
      return Settings.getInstance().getAlternateDelimiter(dbConnection, DelimiterDefinition.STANDARD_DELIMITER);
    }
    return DelimiterDefinition.STANDARD_DELIMITER;
  }

  public void setShowPackageProcedureOnly(boolean flag)
  {
    this.extractPackageProcedure = flag;
  }

	@Override
	public void cancel()
	{
		this.cancel = true;
	}

  private boolean isTable(DbObject dbo)
  {
    return dbo.getObjectType().equalsIgnoreCase(TYPE_TABLE) || additionalTableTypes.contains(dbo.getObjectType());
  }

	private void appendForeignKeys()
	{

    List<DbObject> toProcess = objectList.stream().filter(dbo -> isTable(dbo)).collect(Collectors.toList());

    if (toProcess.isEmpty()) return;

		if (this.progressMonitor != null)
		{
			this.progressMonitor.setCurrentObject(ResourceMgr.getString("TxtScriptProcessFk"), -1, -1);
		}

    if (useSeparator)
    {
      output.append("-- BEGIN FOREIGN KEYS --");
    }

		TableSourceBuilder builder = TableSourceBuilderFactory.getBuilder(dbConnection);
		for (DbObject dbo : toProcess)
		{
			if (cancel) break;

			TableIdentifier tbl = (TableIdentifier)dbo;
			tbl.adjustCase(this.dbConnection);
			StringBuilder source = builder.getFkSource(tbl);
			if (source != null && source.length() > 0)
			{
				output.append(nl);
				output.append(source);
			}
		}

    if (useSeparator)
    {
      output.append("-- END FOREIGN KEYS --");
      output.append(nl);
    }
    output.append(nl);
	}

	private void appendObjectType(String typeToShow)
	{
    List<DbObject> toProcess = objectList.stream().filter(dbo -> dbo.getObjectType().equalsIgnoreCase(typeToShow)).collect(Collectors.toList());

		for (DbObject dbo : toProcess)
		{
			if (cancel) break;

      String type = dbo.getObjectType();

			CharSequence source = null;

			if (this.progressMonitor != null)
			{
				this.progressMonitor.setCurrentObject(dbo.getObjectName(), currentObject++, totalObjects);
			}

      boolean isCompleteProcedure = true;
			try
			{
				if (dbo instanceof TableIdentifier)
				{
					// do not generate foreign keys now, they should be generated at the end after all tables
					source = ((TableIdentifier)dbo).getSource(dbConnection, false, includeGrants);
				}
				else
				{
					source = dbo.getSource(dbConnection);
          if (dbo instanceof ProcedureDefinition && extractPackageProcedure)
          {
            ProcedureDefinition def = (ProcedureDefinition)dbo;
            if (def.isPackageProcedure())
            {
              ProcedureReader reader = ReaderFactory.getProcedureReader(dbConnection.getMetadata());
              CharSequence procSrc = reader.getPackageProcedureSource(def);
              if (procSrc != null)
              {
                source = procSrc;
                isCompleteProcedure = false;
              }
            }
          }
				}

        // if this is a procedure that is part of a package
        // and only the procedure is shown, the script isn't useful anyway
        // so it makes no sense to append a commit
				if (isCompleteProcedure)
				{
					appendCommit = appendCommit || commitTypes.contains(type.toLowerCase());
				}
			}
			catch (Exception e)
			{
				output.append("\nError creating script for ");
				output.append(dbo.getObjectName());
				output.append(" ");
				output.append(ExceptionUtil.getDisplay(e));
			}

			if (source != null && source.length() > 0)
			{
				boolean writeSeparator = useSeparator && !typesWithoutSeparator.contains(type) && this.objectList.size() > 1;
				if (writeSeparator)
				{
					output.append("-- BEGIN " + type + " " + dbo.getObjectName() + nl);
				}

				if (includeDrop)
				{
					CharSequence drop = dropper.getDropForObject(dbo);
					if (drop != null && drop.length() > 0)
					{
						output.append(drop);
            DelimiterDefinition delim = getDelimiter();
            output.append(delim.getScriptText());
						output.append(nl);
					}
				}
				output.append(source);

				if (!StringUtil.endsWith(source, nl))
				{
					output.append(nl);
				}

				if (writeSeparator)
				{
					output.append("-- END " + type + " " + dbo.getObjectName() + nl);
				}
				output.append(nl);
			}
		}
	}

}
