package digitalgarden.mecsek.utils;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 *  How to work with bitmaps?
 *
 *
 * Intent.ACTION_PICK
 *      Input: getData() is URI containing a directory of data (vnd.android.cursor.dir/*) from which to pick an item.
 *      (Uri can be: MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
 *      Output: The URI of the item that was picked.
 *
 * Intent.ACTION_GET_CONTENT
 *      D.Hackborn recommends it (https://stackoverflow.com/a/6486827)
 *      Input: getType() is the desired MIME type to retrieve.
 *      Note that no URI is supplied in the intent, as there are no constraints on where the returned data originally
 *      comes from.
 *      You may also include the CATEGORY_OPENABLE if you can only accept data that can be opened as a stream.
 *      You may use EXTRA_LOCAL_ONLY to limit content selection to local data.
 *      You may use EXTRA_ALLOW_MULTIPLE to allow the user to select multiple items.
 *      Output: The URI of the item that was picked. This must be a content: URI so that any receiver can access it.
 *
 *      https://developer.android.com/training/sharing/send - Sending simple data to other apps
 *      Intent.createChooser(Intent, text) can be used to open Android Sharesheet, if it is not clear what to do with
 *     Uri
 *

 * Set image in ImageView from Uri. (Get bitmap from ImageView is not easy, it should be drawn on a canvas.)
 *
 * ImageView.setImageUri(Uri uri)
 *      Note that you use this method to load images from a local Uri only.
 *      This does Bitmap reading and decoding on the UI thread, which can cause a latency hiccup.
 *
 * How to make BITMAP from URI?
 *
 *      MediaStore.Images -     Collection of all media with MIME type of image/*
 *      MediaStore.Images.Media.getBitmap (ContentResolver cr, Uri url) - deprecated in API level 29!
 *      ImageDecoder.Source createSource (ContentResolver cr, Uri uri) - could be used, but it was added in API 28!
 *
 *      bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
 *      imageView.setImageBitmap(bitmap);
 *

 *
 * ContentProvider can provide an input stream for the given uri, which can be decoded (see later)
 *
 final Uri imageUri = data.getData();
 final InputStream imageStream = getContentResolver().openInputStream(imageUri);
 final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
 image_view.setImageBitmap(selectedImage);

 *
 * Loading Large Bitmaps Efficiently
 * https://developer.android.com/topic/performance/graphics/load-bitmap
 *

 BitmapFactory.Options options = new BitmapFactory.Options();
 options.inJustDecodeBounds = true;
 BitmapFactory.decodeResource(getResources(), R.id.myimage, options);
 int imageHeight = options.outHeight;
 int imageWidth = options.outWidth;
 String imageType = options.outMimeType;

 public static int calculateInSampleSize(
 BitmapFactory.Options options, int reqWidth, int reqHeight) {
 // Raw height and width of image
 final int height = options.outHeight;
 final int width = options.outWidth;
 int inSampleSize = 1;
 if (height > reqHeight || width > reqWidth) {
 final int halfHeight = height / 2;
 final int halfWidth = width / 2;
 // Calculate the largest inSampleSize value that is a power of 2 and keeps both
 // height and width larger than the requested height and width.
 while ((halfHeight / inSampleSize) >= reqHeight
 && (halfWidth / inSampleSize) >= reqWidth) {
 inSampleSize *= 2;
 }
 }
 return inSampleSize;
 }

 public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
 int reqWidth, int reqHeight) {

 // First decode with inJustDecodeBounds=true to check dimensions
 final BitmapFactory.Options options = new BitmapFactory.Options();
 options.inJustDecodeBounds = true;
 BitmapFactory.decodeResource(res, resId, options);

 // Calculate inSampleSize
 options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

 // Decode bitmap with inSampleSize set
 options.inJustDecodeBounds = false;
 return BitmapFactory.decodeResource(res, resId, options);
 }

 imageView.setImageBitmap(
 decodeSampledBitmapFromResource(getResources(), R.id.myimage, 100, 100));

 *
 * END OF SAMLE CODE
 * Loading Large Bitmaps Efficiently
 *

 *
 * How to convert to byte[]? And send it to another activity?
 *
 *      ByteArrayOutputStream baos = new ByteArrayOutputStream();
 *      bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
 *      byte[] b = baos.toByteArray();
 *      Intent intent=new Intent(Passimage.this,myclass.class);
 *      intent.putExtra("picture", b);
 *      startActivity(intent);
 *
 *      then:
 *      Bundle extras = getIntent().getExtras();
 *      byte[] b = extras.getByteArray("picture");
 *      Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
 *      ImageView image = (ImageView) findViewById(R.id.imageView1);
 *      image.setImageBitmap(bmp);


 * HOW TO LOAD IMAGE ON A BACKGROUND THREAD? (Asynctask should be better)
 * http://www.coderzheaven.com/2012/12/23/store-image-android-sqlite-retrieve-it/

 *
 * Complete media-store can be queried against a specific uri.
 * Problem DATA is NOT supported above 'compileSdkVersion 29'  https://stackoverflow.com/a/58009636
 *
 * ??? Two different queries (and merge cursor) is needed for external and internal store
 * I left it here as a sample, how to query mediastore
 *
 Uri pickedImage = data.getData();
 // Let's read picked image path using content resolver
 String[] filePath = { MediaStore.Images.Media.DATA };
 Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
 cursor.moveToFirst();
 String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
 BitmapFactory.Options options = new BitmapFactory.Options();
 options.inPreferredConfig = Bitmap.Config.ARGB_8888;
 Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
 // Do something with the bitmap
 // At the end remember to close the cursor or you will end with the RuntimeException!
 cursor.close();
 */
public class BitmapUtils
    {

    /**
     * Picks an image from the local device. Returns data in onActivityResult()
     * @param fragment     where to return image
     * @param selectorCode to be checked inside
     */
    public static void pickImage(Fragment fragment, int selectorCode)
        {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        // fragment.startActivityForResult(Intent.createChooser(intent, "Select Picture"), selectorCode);
        fragment.startActivityForResult(intent, selectorCode);
        }


    /**
     * Capture image by camera app. https://developer.android.com/training/camera/photobasics
     * Android manefest contains (not sure if needed)
     *      <uses-feature
     *         android:name="android.hardware.camera"
     *         android:required="false" />
     * It works without extra camera permission, is it needed?
     * @param fragment
     * @param selectorCode
     */
    public static void captureImage(Fragment fragment, int selectorCode)
        {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity( fragment.getContext().getPackageManager()) != null)
            {
            fragment.startActivityForResult(intent, selectorCode);
            }
        }


    /**
     * Decodes bitmap from the Uri of an image on the local device.
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


    /** Decode a downsampled bitmap from inpt stream, to be as near as possible to required size */
    private static Bitmap decodeSampledBitmap(InputStream imageStream1, InputStream imageStream2, int reqWidth,
                                              int reqHeight) throws IOException
        {
        final BitmapFactory.Options options = new BitmapFactory.Options();

        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;
        // imageStream.mark( imageStream.available() );
        BitmapFactory.decodeStream(imageStream1, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        // imageStream.reset();
        return BitmapFactory.decodeStream(imageStream2, null, options);

        // IMPORTANT!! https://stackoverflow.com/questions/2503628/bitmapfactory-decodestream-returning-null-when-options-are-set
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
