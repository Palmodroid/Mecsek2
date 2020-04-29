package digitalgarden.mecsek.generic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import digitalgarden.mecsek.fieldtypes.FieldImage;

import java.util.HashMap;
import java.util.Map;

/**
 * GenericStorageFragment is used to store values (like blob and bitmap) during config changes. Values are stored
 * inside a hash-map identified by String key.
 * <p>This storage is created (and returned) by {@link GenericControllActivity#getStorage()}. It is used by
 * {@link GenericEditFragment} / {@link FieldImage}</p>
 */
public class GenericStorageFragment extends Fragment
    {
    /** map to store data */
    private Map<String, Object> map = new HashMap<>();

    /** during creation setRetainInstance should set to TRUE, to retain fragment */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);
        setRetainInstance( true );
        }

    /** Any type can be stored (value is Object). Key could be the ID of the View, or selectorCode - both are uniqe
     * for one {@link GenericEditFragment} */
    public void put(String key, Object value)
        {
        map.put(key, value);
        }

    /** Returns previously stored data */
    public Object get(String key)
        {
        return map.get(key);
        }
    }
