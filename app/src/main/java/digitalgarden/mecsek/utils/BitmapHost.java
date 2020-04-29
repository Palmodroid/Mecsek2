package digitalgarden.mecsek.utils;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.widget.ImageView;
import digitalgarden.mecsek.R;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.generic.GenericStorageFragment;
import digitalgarden.mecsek.scribe.Scribe;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static digitalgarden.mecsek.Debug.IMAGE;


/*
TODO!!! Bitmap and blob storage doesn't work without Image ID!!!
TODO!!! Bitmap and blob data can be moved directly to GenericStorageFragment!!!

TODO!!! Picture file should should use better names!!!
TODO!!! Picture file should be deleted if action retuns CANCEL!!!
TODO!!! Original picture file should be renamed, if overwritten by other picture!!!

TODO!!! NULL pictures should use an Empty picture instead of no picture (Solid background? Picture from res? - better)

TODO!!! Export and Import!!!
*/



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
public class BitmapHost
    {
    /** Fragment which starts activities and gets the returning results
     *  (Alternatively Activity can also be used eg. with an other constructor, and common context variable) */
    private final GenericEditFragment editFragment;

    /** Storage is used during config changes. New fragment cannot be created during saveInstanceState process, so
     * storage is get by constructor */
    private final GenericStorageFragment storage;

    /** Request code provided by the Fragment for PICK PICTURE */
    private final int selectorCodeForPick;

    /** Request code provided by the Fragment for CAPTURE IMAGE */
    private final int selectorCodeForCapture;


    /* I M A G E */

    /* BITMAP BLOB URI PATH
     * BITMAP - Low resolution photo was captured (URI, PATH are NULL. BITMAP can be converted to BLOB)
     * BLOB   - Low resolution was loaded from database (URI, PATH are NULL. BLOB can be concerted to BITMAP)
     * URI    - HIGH resolution photo was picked (PATH is NULL. URI can be converted to downsampled BITMAP -> BLOB)
     * PATH   - HIGH resolution photo was captured (URI is NULL. PATH can be converted to downsampled BITMAP -> BLOB)
     *
     * If BLOB is empty - convert from BITMAP
     * If BITMAP is empty - convert (and downsample) from URI or PATH (which is NOT null)
     *
     * ??? GetHighRes() returns URI or PATH (which is not null) or NULL if both is NULL.
     *
     * Pick full res - URI    \
     *                         - ->          (downsampled) BITMAP <- -> BLOB
     * Capture full - PATH   /          Capture thumbnail /
     *
     * BITMAP and BLOB can be converted to each other. URI or PATH cannot be converted back from BITMAP (or BLOB)
     *
     * TODO Picked full resolution photo should be copied to own data storage!
     * TODO Sign deleted (orphan) photos!
     */


    /** Path/String (full resolution) needed by Database */
    private String path = null;

    /** Uri (full resolution - picture is in gallery) needed by Database */
    private Uri uri = null;

    /** Bitmap (reduced resolution) needed by ImageView */
    private Bitmap bitmap = null;

    /** Blob/byte-array (reduced resolution) needed by Database */
    private byte[] blob = null;

    /** ImageVIew to show image can also be connected (not obligatory)
     *  ImageView is always filled up with reduced resolution bitmap */
    private ImageView imageView = null;

    /** Required size for BITMAP and BLOB reduced resolution */
    private int width = 100;
    private int height = 100;

    /** Temporary store created file */
    private String temporaryPhotoFile = null;


    /**
     * BitmapHost to handle images - without connected ImageView
     * @param editFragment provide context and selector codes
     */
    public BitmapHost( GenericEditFragment editFragment )
        {
        this.editFragment = editFragment;
        this.storage = editFragment.onFinishedListener.getStorage();

        // Two different selector codes are generated for different extern activities
        this.selectorCodeForPick = editFragment.getCode();
        this.selectorCodeForCapture = editFragment.getCode();
        }

    /**
     * BitmapHost to handle images - with connected ImageView
     * width and height for reduced resolution are provided externally
     * @param editFragment provide context and selector codes
     * @param imageView to show bitmap
     * @param width width of reduced resolution (in pixels)
     * @param height height of reduced resolution (in pixels)
     */
    public BitmapHost( GenericEditFragment editFragment, ImageView imageView,
                       int width, int height )
        {
        this( editFragment );

        Scribe.locus();

        this.imageView = imageView;
        this.width = width;
        this.height = height;
        }


    /** If (and only if) ImageView was set by constructor, setImage() will show reduced resolution bitmap in
     * ImageView.  This method is called by each set... methods. */
    private void setImage()
        {
        if ( imageView != null )
            {
            // TODO If bitmap is null, then "empty" picture should be set from res
            imageView.setImageBitmap( getBitmap() );
            }
        }

    /** Clears all types of bitmap before setting a new bitmap */
    private void clearAll()
        {
        path = null;
        uri = null;
        bitmap = null;
        blob = null;
        }


    /** PATH is set - when full resolution image was saved */
    public void setPath( String path )
        {
        clearAll();
        this.path = path;
        setImage();
        }

    /** URI is set - when full resolution image was selected from gallery */
    public void setUri( Uri uri )
        {
        clearAll();
        this.uri = uri;
        setImage();
        }

    /** BITMAP is set - reduced resolution image eg. after selecting thumbnail from photo */
    public void setBitmap( Bitmap bitmap )
        {
        clearAll();
        this.bitmap = bitmap;
        setImage();
        }

    /** BLOB is set - reduced resolution image eg. loaded from database */
    public void setBlob( byte[] blob )
        {
        clearAll();
        this.blob = blob;
        setImage();
        }

    /** @return PATH of full resolution image file - or NULL if no file was saved */
    public String getPath()
        {
        return path;
        }

    /** @return URI of full resolution image file - or NULL if no image was selected from gallery */
    public Uri getUri()
        {
        return uri;
        }

    /** @return BITMAP of reduced resolution image file - or NULL if no image was set */
    public Bitmap getBitmap()
        {
        if ( bitmap == null )
            {
            if ( blob != null )
                {
                bitmap = BitmapUtils.byteArrayToBitmap( blob );
                }

            // Both URI and PATH cannot contain image, only one of them can be non-null.

            else if ( uri != null )
                {
                bitmap = BitmapUtils.uriToBitmap( editFragment.getContext(), uri, width, height);
                }
            else if ( path != null )
                {
                Scribe.debug("Image path is: " + path );
                bitmap = BitmapUtils.pathToBitmap( path, width, height);
                }
            }
        return bitmap;
        }

    /** @return BLOB of reduced resolution image file - or NULL if no image was set
     *  BLOB is always converted from BITMAP */
    public byte[] getBlob()
        {
        if ( blob == null )
            {
            if ( bitmap == null )
                {
                getBitmap();
                }
            blob = BitmapUtils.bitmapToByteArrayPng( bitmap );
            }
        return blob;
        }

    /** Saves value during config changes */
    public void saveData(Bundle data)
        {
        Scribe.locus(IMAGE);

        storage.put( "Bitmap_" + imageView.getId(), bitmap );
        storage.put( "Blog_" + imageView.getId(), blob );

        data.putString( "Temp_" + selectorCodeForCapture, temporaryPhotoFile );
        data.putString( "Path_" + selectorCodeForCapture, path );
        data.putParcelable( "Uri_" + selectorCodeForPick, uri );
        // Other method: save as string, and parse back

        Scribe.debug( IMAGE, "Saving temp path: " + temporaryPhotoFile);
        Scribe.debug( IMAGE, "Saving path: " + path);
        Scribe.debug( IMAGE, "Saving uri: " + uri);
        Scribe.debug( IMAGE, "Saving bitmap: " + (bitmap != null));
        Scribe.debug( IMAGE, "Saving blog: " + (blob != null));
        }

    /** Retrieve value AND sets image from value after config changes */
    public void retrieveData(Bundle data)
        {
        Scribe.locus(IMAGE);
        // value.setBlob( data.getByteArray( column(columnIndex) ));

        bitmap = (Bitmap)storage.get( "Bitmap_" + imageView.getId());
        blob = (byte[])storage.get( "Blog_" + imageView.getId());

        temporaryPhotoFile = data.getString( "Temp_" + selectorCodeForCapture );
        path = data.getString( "Path_" + selectorCodeForCapture );
        uri = data.getParcelable("Uri_" + selectorCodeForPick);

        Scribe.debug( IMAGE, "Retrieving temp path: " + temporaryPhotoFile);
        Scribe.debug( IMAGE, "Retrieving path: " + path);
        Scribe.debug( IMAGE, "Retrieving uri: " + uri);
        Scribe.debug( IMAGE, "Retrieving bitmap: " + (bitmap != null));
        Scribe.debug( IMAGE, "Retrieving blog: " + (blob != null));

        setImage();
        }

    /**
     * Picks an image from the local device.
     * Returns data in onActivityResult() ({@link #checkReturningSelector(int, Intent)}  as Uri uri = data.getData();
     */
    public void pickImage()
        {
        temporaryPhotoFile = null;

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        // How to import from any galLery app.
        // READ THIS!! https://stackoverflow.com/questions/5309190/android-pick-images-from-gallery

        editFragment.startActivityForResult(intent, selectorCodeForPick);
        }



    /**
     * Capture image by camera app. https://developer.android.com/training/camera/photobasics
     *
     * Android manifest contains (not sure if needed)
     *      <uses-feature
     *         android:name="android.hardware.camera"
     *         android:required="false" />
     *
     * It works without extra camera permission, is it needed?
     */
    public void captureImage( boolean fullResolution )
        {
        Scribe.locus( IMAGE );

        File photoFile = null;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if ( fullResolution)
            {
            try
                {
                photoFile = createImageFile(editFragment.getContext());
                temporaryPhotoFile = photoFile.getAbsolutePath();
                }
            catch (IOException ex)
                {
                // Error occurred while creating the File
                Scribe.error("ERROR!");
                }

            // Continue only if the File was successfully created

            // Idea comes from: https://android.jlelse.eu/androids-new-image-capture-from-a-camera-using-file-provider-dd178519a954
            // External files through fileProvider:
            // https://stackoverflow.com/questions/37074872/android-fileprovider-on-custom-external-storage-folder
            if (photoFile != null)
                {
                Uri photoURI = FileProvider.getUriForFile(editFragment.getContext(),
                        editFragment.getContext().getPackageName() + ".fileprovider",
                        photoFile);

                Scribe.debug(IMAGE, "PhotoFile was prepared, uri: " + photoURI );

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                }
            }

        if (intent.resolveActivity( editFragment.getContext().getPackageManager()) != null)
            {
            editFragment.startActivityForResult(intent, selectorCodeForCapture);
            }
        }


    private static File createImageFile( Context context ) throws IOException
        {
        File directory = Environment.getExternalStorageDirectory();
        String subPath = context.getString( R.string.directory );

        File temp = new File( directory, subPath);
        // ha nincs ilyen konyvtar, akkor letrehozzuk
        if ( !temp.exists() )
            {
            temp.mkdirs();
            }
        // de csak akkor valasztjuk ki, ha ez egy konyvtar
        if ( temp.isDirectory() )
            {
            directory = temp;
            }

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                directory      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        // currentPhotoPath = image.getAbsolutePath();

        return image;
        }



    public boolean checkReturningSelector(int requestCode, Intent data)
        {
        Scribe.locus( IMAGE );
        // Scribe.debug("Image photofile: " + photoFile.getAbsolutePath());
        // Result code already checked by {@link GenericEditFragment#onActivityCreated(Bundle)}

        // Picture was picked from gallery
        if (selectorCodeForPick == requestCode)
            {
            Scribe.debug( IMAGE, "Image was picked from gallery, uri: " + data.getData());
            setUri( data.getData());
            return true;
            }

        // Image was captured
        else if (selectorCodeForCapture == requestCode)
            {
            Scribe.debug( IMAGE, "Image was captured by camera" );
            if ( temporaryPhotoFile != null ) // photoFile was created; extras == null (should be)
                {
                Scribe.debug( IMAGE, "Prepared PhotoFile was captured, path: " + temporaryPhotoFile);
                setPath( temporaryPhotoFile );
                return true;
                }
            else if ( data != null ) // thumbnail was recorded
                {
                Bundle extras = data.getExtras();
                if ( extras != null )
                    {
                    Scribe.debug( IMAGE, "Thumbnail was captured, bitmap returned" );
                    setBitmap((Bitmap) extras.get("data"));
                    return true;
                    }
                }
            // if both are null - no photo was taken (not possible, because RESULT_OK was checked
            Scribe.debug( IMAGE, "Prepared PhotoFile was NOT captured, NO FILE, NO THUMBNAIL!!");
            }

        return false;
        }

    }
