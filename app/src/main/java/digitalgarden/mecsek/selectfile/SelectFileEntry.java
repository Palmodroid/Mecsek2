package digitalgarden.mecsek.selectfile;

import java.io.File;
import java.util.Locale;


class SelectFileEntry implements Comparable<SelectFileEntry>
	{
	static enum Type
		{
		NEW,
		FILE,
		DIR,
		PARENT_DIR,
		HEADER
		}
		
	private File file;
	private Type type;
     
    SelectFileEntry( File file, Type type )
    	{
  		this.file = file;
		this.type = type;
    	}

	File getFile()
		{
		return file;
		}

    Type getType()
		{
        return type;
		}

	// Erre a sorbarendezeshez lesz szukseg
    // NULL-t úgy tekinti, mint üres stringet
    @Override
    public int compareTo( SelectFileEntry thatCsvEntry ) 
		{
		String thisString;
			
		if ( getFile() == null )
			thisString = "";
		else 
			thisString = getFile().getName().toLowerCase( Locale.getDefault() );
		
		String thatString;
		
		if ( thatCsvEntry == null || thatCsvEntry.getFile() == null )
			thatString = "";
		else
			thatString = thatCsvEntry.getFile().getName().toLowerCase( Locale.getDefault() );
			
        return thisString.compareTo( thatString );
    	}
	}

