/*
 * HtmlExportWriter.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015, Thomas Kellerer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
package workbench.db.exporter;

/**
 * An ExportWriter to generate HTML output.
 *
 * @author  Thomas Kellerer
 * @see HtmlRowDataConverter
 */
public class HtmlExportWriter
	extends ExportWriter
{

	public HtmlExportWriter(DataExporter exp)
	{
		super(exp);
	}

	@Override
	public RowDataConverter createConverter()
	{
		return new HtmlRowDataConverter();
	}

	@Override
	public void configureConverter()
	{
		super.configureConverter();
		HtmlRowDataConverter conv = (HtmlRowDataConverter)this.converter;
		conv.setCreateFullPage(exporter.getCreateFullHtmlPage());
		conv.setEscapeHtml(exporter.getEscapeHtml());
		conv.setHeading(exporter.getHtmlHeading());
		conv.setTrailer(exporter.getHtmlTrailer());
		conv.setNullString(exporter.getNullString());
	}
}
