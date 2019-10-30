package digitalgarden.mecsek.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


public class Utils
    {
    public static int[] convertToIntArray(List<Integer> integerList)
        {
        int[] intArray = new int[integerList.size()];
        int i = 0;
        for ( Integer integer : integerList )
            {
            intArray[i++] = integer;
            }

        // Iterator<Integer> iterator = integerList.iterator();
        // for (int i = 0; i < intArray.length; i++)
        //     {
        //     intArray[i] = iterator.next();
        //     }

        return intArray;
        }


    public static int[] splitInts( String string )
        {
        boolean numeric = false;
        int integer = 0;
        int character = 0;
        ArrayList<Integer> integers = new ArrayList<>();

        // Kihagyjuk az összetett ellenőrzéseket, itt nem történhet IO exception, mivel a String létezik
        StringReader reader = new StringReader( string );

        while (true)
            {
            try
                {
                character = reader.read();
                }
            catch (IOException e)
                {
                } // Ez lehetetlen!

            if (character >= '0' && character <= '9')
                {
                if (numeric)
                    {
                    integer *= 10;
                    }
                else
                    {
                    integer = 0;
                    numeric = true;
                    }
                integer += character - '0';
                }
            else
                {
                if (numeric)
                    {
                    integers.add( integer );
                    numeric = false;
                    }

                if ( character == -1)
                    break;
                }
            }

        reader.close();

        return convertToIntArray(integers);
        }


    }
