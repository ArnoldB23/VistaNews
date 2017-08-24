package roca.bajet.com.vistanews.data;

import io.realm.RealmObject;

/**
 * Created by Arnold on 8/23/2017.
 */

public class RealmString extends RealmObject {
    public String string;

    public RealmString(String str)
    {
        string = str;
    }

    public RealmString()
    {
        super();
    }

}
