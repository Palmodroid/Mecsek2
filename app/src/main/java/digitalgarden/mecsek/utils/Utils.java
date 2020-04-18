package digitalgarden.mecsek.utils;

import digitalgarden.mecsek.scribe.Scribe;

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


    private static final int DIGITS_FOR_LONG = 16;

    /**
     * Converts long values to string format. (Hexadecimal value, invers direction, digits are represented by ascii
     * letters, starting from '@' (0x40) or (0xE0 just for fun) as 0. Leading 0-s are omitted.
     */
    public static String convertLongToString( long number )
        {
        char[] digits = new char[DIGITS_FOR_LONG];
        int n = 0;

        //Scribe.debug( "CONVERT: " + Long.toHexString(number) + " = ");

        do  {                           // IF n < digits.length - BUT long cannot be longer than 16 digits!
            digits[n] = (char)((number & 0xFL) | 0xE0L );

            // HEX DIGITS - still backwards, and reconversation still missing
            // digits[n] = (char)(number & 0xFL);
            // digits[n] += ( digits[n] < 10 ) ? '0' : ('a'-10);

            number = number >>> 4;      // Hex values from '@' - 0x40L
            n++;                        // Length of already written digits
            } while ( number != 0L );   // Breaks when no more digits are needed


        //Scribe.debug( "CONVERT: = >" + String.valueOf( digits, 0, n ) + "<");

        return String.valueOf( digits, 0, n );
        }

    /**
     * Converts long values to string format. (Hexadecimal value, digits are represented by ascii letters, starting
     * from '@' as 0.)
     * <p>Important! There is NO error check. Any string in any length can be converted. Last 16 characters, and
     * lower half of each ascii character will be used. </p>
     */
    public static long convertStringToLong( String digits )
        {
        long number = 0L;
        int n = digits.length();

        //Scribe.debug( "CONVERT: >" + digits + "< = ");

        while( n > 0 )
            {
            n--; // index decreases at the beginning, because it should go from len-1 to 0
            number = number << 4;
            number += digits.charAt(n) & 0xF;
            }

        //Scribe.debug( "CONVERT: = " + Long.toHexString(number) );

        return number;
        }
    }
