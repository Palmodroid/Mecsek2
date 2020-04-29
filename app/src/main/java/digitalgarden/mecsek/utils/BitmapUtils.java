package digitalgarden.mecsek.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import digitalgarden.mecsek.R;
import digitalgarden.mecsek.scribe.Scribe;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Static methods to manage pictures
 */
public class BitmapUtils
    {
    /**
     * Decodes a downsampled bitmap from a file (defined by path) to be as near as possible to required size
     * Required size could be set to roughly downsample image.
     * @param path path of the local file
     * @param reqWidth required width
     * @param reqHeight required height
     * @return Decoded bitmap or null.
     */
    public static Bitmap pathToBitmap(String path, int reqWidth, int reqHeight)
        {
        Bitmap bitmap;

        BitmapFactory.Options options = new BitmapFactory.Options();

        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile( path, options );

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile( path );

        return bitmap;
        }


    /**
     * Decodes a downsampled bitmap from the Uri of an image on the local device, to be as near as possible to required size
     * Required size could be set to roughly downsample image.
     * @param context Context to reach content resolver
     * @param imageUri Uri of the local image
     * @param reqWidth required width
     * @param reqHeight required height
     * @return Decoded bitmap or null.
     */
    public static Bitmap uriToBitmap(Context context, Uri imageUri, int reqWidth, int reqHeight)
        {
        InputStream imageStream1 = null, imageStream2 = null;
        Bitmap bitmap = null;

        try
            {
            BitmapFactory.Options options = new BitmapFactory.Options();

            // First decode with inJustDecodeBounds=true to check dimensions
            options.inJustDecodeBounds = true;
            imageStream1 = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(imageStream1, null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            imageStream2 = context.getContentResolver().openInputStream(imageUri);
            bitmap= BitmapFactory.decodeStream(imageStream2, null, options);

            // IMPORTANT!! https://stackoverflow.com/questions/2503628/bitmapfactory-decodestream-returning-null-when-options-are-set
            }
        catch (FileNotFoundException e)
            {
            ;
            }
        finally
            {
            try
                {
                if ( imageStream1 != null )     imageStream1.close();
                }
            catch (IOException e)
                {
                e.printStackTrace();
                }
            try
                {
                if ( imageStream2 != null )     imageStream2.close();
                }
            catch (IOException e)
                {
                e.printStackTrace();
                }
            }

        return bitmap;
        }


    /** Calculates inSampleSize from a previously measured image to downsample it to required size */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
        {
        // Raw height and width of image
        final int halfHeight = options.outHeight / 2;
        final int halfWidth = options.outWidth / 2;
        int inSampleSize = 1;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth)
            {
            inSampleSize *= 2;
            }
        return inSampleSize;
        }


    /** Converts bitmap to byte-array using PNG type */
    public static byte[] bitmapToByteArrayPng(Bitmap bitmap)
        {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
        }


    /** Convert byte-array data to bitmap */
    public static Bitmap byteArrayToBitmap(byte[] byteArray)
        {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        }

    }
