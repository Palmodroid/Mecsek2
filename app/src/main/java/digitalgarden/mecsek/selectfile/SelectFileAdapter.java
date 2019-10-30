package digitalgarden.mecsek.selectfile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.scribe.Scribe;


public class SelectFileAdapter extends BaseAdapter implements Filterable
	{
	private LayoutInflater layoutInflater;
	private Context context;
	
	private List<SelectFileEntry> filteredEntries;
	private List<SelectFileEntry> originalEntries;
	
	private entryFilter entryFilter;

	
    public SelectFileAdapter(Context context, List<SelectFileEntry> entries) 
		{
        super();
		
		this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.originalEntries = entries;
		this.filteredEntries = entries;
		this.context = context;
    	}

	@Override
	public int getCount()
		{
		return (filteredEntries == null) ? 0 : filteredEntries.size();
		}

	@Override
	public SelectFileEntry getItem( int position )
		{
		return filteredEntries.get( position );
		}

	@Override
	public long getItemId(int position)
		{
		return position;
		}
	
	// A következő két függvény biztosítja, hogy a HEADER/DIVIDER más view-t használhat
	@Override
	public int getViewTypeCount()
		{
		return 2;
		}

	@Override
	public int getItemViewType(int position)
		{
		if ( getItem(position).getType() != SelectFileEntry.Type.HEADER )
			return 0;
		
		return 1;
		}
	
	// A következő két függvény biztosítja, hogy a HEADER/DIVIDER-t nem lehet kiválasztani
	@Override
	public boolean areAllItemsEnabled()
		{
		return false;
		}
	
	@Override
	public boolean isEnabled(int position)
		{
		return getItem(position).getType() != SelectFileEntry.Type.HEADER;
		}
	
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) 
		{
    	SelectFileEntry entry = getItem(position);
    	View rowView;
    	
    	if ( entry.getType() == SelectFileEntry.Type.HEADER )
			{
			rowView = (convertView == null) ? layoutInflater.inflate( R.layout.file_entry_header_row_view, null) : convertView;
			
			TextView title = (TextView) rowView.findViewById( R.id.divider_title );
			if ( entry.getFile() == null ) // Root-directoryt jelent
				title.setText( R.string.title_external_storage );
			else 
				title.setText( context.getString( R.string.title_directory ) + entry.getFile().getName() );
			}
		else
    		{
    		rowView = (convertView == null) ? layoutInflater.inflate( R.layout.file_entry_row_view, null) : convertView;	
    	
			TextView name = (TextView) rowView.findViewById( R.id.file_entry_name );
			TextView data = (TextView) rowView.findViewById( R.id.file_entry_data );		
			ImageView icon = (ImageView) rowView.findViewById( R.id.file_entry_icon );
			
			File file = entry.getFile();
			SimpleDateFormat sdf=new SimpleDateFormat( context.getString(R.string.simple_date_format), Locale.US );
			
			switch ( entry.getType() )
				{
				case NEW:
					name.setText( R.string.create_new_file_short );
					data.setText( R.string.create_new_file );
					icon.setImageResource( R.drawable.icon_new );
					break;
				case PARENT_DIR:
					name.setText( R.string.parent_directory_short );
					data.setText( R.string.parent_directory );
					icon.setImageResource( R.drawable.icon_back );
					break;
				case DIR:
					name.setText( file.getName() );
					data.setText( sdf.format( new Date( file.lastModified() )) + "; " + file.list().length + " items" );
					icon.setImageResource( R.drawable.icon_folder );
					break;
				case FILE:
					name.setText( file.getName() );
					data.setText( sdf.format(new Date( file.lastModified() )) + "; " + file.length() + " bytes" );
					icon.setImageResource( R.drawable.icon_csv );
					// itt lehet különböző ikonokat beállítani
					// http://stackoverflow.com/questions/4894885/how-to-check-file-extension-in-android
					// - vita az extension kikereséséről
				default:
					break;
				}	
    		}
    	return rowView;
    	}

	// http://stackoverflow.com/a/13514663 - Search and Filter List
	public Filter getFilter()
		{
		if (entryFilter == null)
			entryFilter = new entryFilter();
			
		return entryFilter;
		}	
		
	private class entryFilter extends Filter
		{
		protected FilterResults performFiltering(CharSequence constraint)
			{
			FilterResults filterResults = new FilterResults();
			
			if (constraint != null && constraint.length() > 0 )
				{
	            List<SelectFileEntry> filterList=new ArrayList<SelectFileEntry>();
				constraint = constraint.toString().toLowerCase( Locale.getDefault() );
				
				Scribe.note("Csv Adapter FILTER to [" + constraint + "]");
				
				// dir es file tipust is szukiti
	            for ( int i=0; i < originalEntries.size(); i++ )
	            	{
	                if ( (originalEntries.get(i).getType() != SelectFileEntry.Type.FILE
						&& originalEntries.get(i).getType() != SelectFileEntry.Type.DIR )
						|| originalEntries.get(i).getFile().getName().toLowerCase( Locale.getDefault() ).contains( constraint ) )
	                	{
	                    filterList.add( originalEntries.get(i) );
	                	}
	            	}
	            
	            filterResults.count = filterList.size();
	            filterResults.values = filterList;
				}
			else
				{
				Scribe.note("Csv Adapter FILTER cleared");
				
	            filterResults.count = originalEntries.size();
	            filterResults.values = originalEntries;
				}
			return filterResults;
			}

		// http://stackoverflow.com/a/262416 - Type safety: Unchecked cast
		@SuppressWarnings("unchecked")
		protected void publishResults(CharSequence constraint, FilterResults filterResults)
			{
			filteredEntries = (List<SelectFileEntry>) filterResults.values;
			notifyDataSetChanged();
			}	
		}
	}
