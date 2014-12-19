package workbench.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import workbench.log.LogMgr;

public class WbPersistence
{
	
	/** Creates a new instance of Persistence */
	private WbPersistence()
	{
	}
	
	public static void makeTransient( Class clazz, String property )
	{
		try
		{
			BeanInfo info = Introspector.getBeanInfo( clazz );
			PropertyDescriptor propertyDescriptors[] = info.getPropertyDescriptors();
			
			for ( int i = 0; i < propertyDescriptors.length; i++ )
			{
				PropertyDescriptor pd = propertyDescriptors[i];
				if ( pd.getName().equals(property) )
				{
					pd.setValue( "transient", Boolean.TRUE );
				}
			}
		} 
		catch ( IntrospectionException e )
		{ 
		}
	}	
	
	public static Object readObject(String aFilename)
	{
		try
		{
			InputStream in = new BufferedInputStream(new FileInputStream(aFilename));
			return readObject(in);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			return null;
		}
	}
	
	public static Object readObject(InputStream in)
	{
		long start,end;
		try
		{
			start = System.currentTimeMillis();
			// TODO register an ExceptionListener in order to catch
			// the error messages
			XMLDecoder e = new XMLDecoder(in);
			Object result = e.readObject();
			e.close();
			end = System.currentTimeMillis();
			return result;
		}
		catch (Throwable e)
		{
			return null;
		}
	}
	
	public static void writeObject(Object aValue, String aFilename)
	{
		if (aValue == null) return;
		long start,end;
		
		try
		{
			start = System.currentTimeMillis();
			XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(aFilename)));
			e.writeObject(aValue);
			e.close();
			end = System.currentTimeMillis();
		}
		catch (Throwable e)
		{
			LogMgr.logError("WbPersistence.writeObject()", "Error writing " + aFilename, e);
		}
	}
	
}