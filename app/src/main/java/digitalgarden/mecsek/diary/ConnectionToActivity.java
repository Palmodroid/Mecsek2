package digitalgarden.mecsek.diary;

// The container Activity must implement this interface so the frag can deliver messages
public interface ConnectionToActivity
    {
    public void onReady(DailyData data);
    public void onLongClickDetected(DailyData data);
    public DataStore getDataStore();
    public void onItemEditing(long id);
    }

