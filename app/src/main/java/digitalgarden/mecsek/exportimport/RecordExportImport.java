package digitalgarden.mecsek.exportimport;

import android.content.ContentValues;
import android.database.Cursor;
import digitalgarden.mecsek.utils.Longtime;
import digitalgarden.mecsek.utils.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.columnType;
import static digitalgarden.mecsek.generic.GenericTable.TYPE_COLOR;
import static digitalgarden.mecsek.generic.GenericTable.TYPE_DATE;
import static digitalgarden.mecsek.generic.GenericTable.TYPE_TEXT;

/**
 * Record EXPORT
 * Very similar to the LIST - all (even joined) tables are queried to the columns listed in projection. Value of each 
 * (even foreign or extern) columns containing data can be written to file.
 * 
 * Record IMPORT
 * Very similar to the FORM (EDIT) - Columns of the table are read and will be created (if containing real data). 
 * Foreign keys: record in the referenced table is selected and its row index is stored as key. Extern keys: record 
 * in the referenced table is created and its row index is stored as key. Recursion is needed as extern/foreign 
 * tables can contain further foreign keys.
 * 
 */
public class RecordExportImport
    {
    interface GenericColumn
        {
        void addToProjection(ArrayList<String> projection );
        void addToExport( Cursor cursor, ArrayList<String> data );
        void getFromImport(ContentValues values, Iterator<String> data );
        }


    /**
     * DataColumn refers to a single column (identified by int columnIndex) containing simple data.
     */
    class DataColumn implements GenericColumn
        {
        // index of the data column
        int columnIndex;

        /** Data column is only identified by its index */
        DataColumn( int columnIndex )
            {
            this.columnIndex = columnIndex;
            }

        /** Data column's name should be added to projection */
        @Override
        public void addToProjection(ArrayList<String> projection)
            {
            projection.add( column(columnIndex) );
            }

        /**
         * Cursor should be set to the current record.
         * Columns value (from the record) is added to export (strings)
         */
        @Override
        public void addToExport(Cursor cursor, ArrayList<String> data)
            {
            if ( columnType( columnIndex ) == TYPE_TEXT )
                {
                data.add( cursor.getString(cursor.getColumnIndexOrThrow( column(columnIndex) )) );
                }
            else if ( columnType( columnIndex ) == TYPE_DATE )
                {
                Longtime longtime = new Longtime();
                longtime.set( cursor.getLong(cursor.getColumnIndexOrThrow( column(columnIndex) )));
                data.add( longtime.toString(false));
                // Ha hibás, akkor hol lesz jelzés??
                }
            else if ( columnType( columnIndex ) == TYPE_COLOR )
                {
                data.add( Long.toString(cursor.getLong(cursor.getColumnIndexOrThrow( column(columnIndex) ))) );
                // Ha hibás, akkor hol lesz jelzés??
                }
            }

        /**
         * Exported value is read from the next data string and added to values to create the record from these values
         */
        @Override
        public void getFromImport(ContentValues values, Iterator<String> data)
            {
            if ( data.hasNext() )
                {
                // Convert form data.next() --> values.put(column(columnIndex), _CONVERTED DATA COMES HERE_ );
                if (columnType(columnIndex) == TYPE_TEXT)
                    {
                    values.put(column(columnIndex), StringUtils.revertFromEscaped( data.next() ));
                    }
                else if (columnType(columnIndex) == TYPE_DATE)
                    {
                    Longtime longtime = new Longtime();
                    longtime.setDate(  data.next() );
                    values.put(column(columnIndex), longtime.get());
                    }
                else if (columnType(columnIndex) == TYPE_COLOR)
                    {
                    values.put(column(columnIndex), Long.parseLong( data.next() ));
                    }
                }
            }
        }


    class ForeignKey implements GenericColumn
        {

        @Override
        public void addToProjection(ArrayList<String> projection)
            {

            }

        @Override
        public void addToExport(Cursor cursor, ArrayList<String> data)
            {

            }

        @Override
        public void getFromImport(ContentValues values, Iterator<String> data)
            {

            }
        }
    
    class ExternKey implements GenericColumn
        {
        // Extern Key column index - inside MAIN table
        int externKeyIndex;

        // index of EXTERN table
        int externTableIndex;

        // list of columns needed from EXTERN table
        List<GenericColumn> externColumns;

        /**
         *
         * @param externKeyIndex
         * @param externTableIndex
         * @param externColumnIndices only data columns can be added! Foreign columns should be added separately !!
         */
        private ExternKey(int externKeyIndex, int externTableIndex, int... externColumnIndices)
            {
            this.externKeyIndex = externKeyIndex;
            this.externTableIndex = externTableIndex;

            externColumns = new ArrayList<>();
            for ( int index: externColumnIndices )
                {
                this.externColumns.add( new DataColumn( index ) );
                }
            }

        @Override
        public void addToProjection(ArrayList<String> projection)
            {
            for (GenericColumn column : externColumns )
                {
                column.addToProjection( projection );
                }
            }

        @Override
        public void addToExport(Cursor cursor, ArrayList<String> data)
            {
            for (GenericColumn column : externColumns )
                {
                column.addToExport( cursor, data );
                }
            }

        @Override
        public void getFromImport(ContentValues values, Iterator<String> data)
            {

            }
        }
    
    List<GenericColumn> columnList = new ArrayList<>();
    
    
    
    
    
    }
