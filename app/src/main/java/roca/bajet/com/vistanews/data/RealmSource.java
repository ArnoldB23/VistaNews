package roca.bajet.com.vistanews.data;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Arnold on 8/23/2017.
 */

public class RealmSource extends RealmObject {

    @PrimaryKey
    public String id;
    public String name;
    public String description;
    public String url;
    public String category;
    public String language;
    public String country;


    public RealmList<RealmString> sortBysAvailable = null;

}


