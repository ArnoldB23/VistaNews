
package roca.bajet.com.vistanews.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetSourceResponse {

    @SerializedName("status")
    @Expose
    public String status;
    @SerializedName("sources")
    @Expose
    public List<Source> sources = null;

}
