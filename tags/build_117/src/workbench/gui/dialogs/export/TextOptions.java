/*
 * TextOptions.java
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
package workbench.gui.dialogs.export;

import java.util.Set;

import workbench.db.exporter.BlobMode;
import workbench.db.exporter.ControlFileFormat;
import workbench.util.CharacterEscapeType;

import workbench.util.CharacterRange;
import workbench.util.QuoteEscapeType;

/**
 *
 * @author Thomas Kellerer
 */
public interface TextOptions
{
	String getTextDelimiter();
	void setTextDelimiter(String delim);
	boolean getExportHeaders();
	void setExportHeaders(boolean flag);
	String getTextQuoteChar();
	void setTextQuoteChar(String quote);
	void setQuoteAlways(boolean flag);
	boolean getQuoteAlways();
	void setEscapeRange(CharacterRange range);
	CharacterRange getEscapeRange();
	void setEscapeType(CharacterEscapeType type);
	CharacterEscapeType getEscapeType();
	void setLineEnding(String ending);
	String getLineEnding();
	void setDecimalSymbol(String decimal);
	String getDecimalSymbol();
	QuoteEscapeType getQuoteEscaping();
	Set<ControlFileFormat> getControlFiles();
	BlobMode getBlobMode();
}