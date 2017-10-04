package roca.bajet.com.vistanews;

import android.app.SharedElementCallback;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Arnold on 9/27/2017.
 */

public class SourceSharedElementCallback extends SharedElementCallback {

    private ImageView mImageView;
    public String LOG_TAG;


    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
        removeObsoleteElements(names, sharedElements, mapObsoleteElements(names));
        mapSharedElement(names, sharedElements, mImageView);
    }

    public void setImageView(@NonNull ImageView imageView) {
        mImageView = imageView;
    }


    /**
     * Maps all views that don't start with "android" namespace.
     *
     * @param names All shared element names.
     * @return The obsolete shared element names.
     */
    @NonNull
    private List<String> mapObsoleteElements(List<String> names) {
        List<String> elementsToRemove = new ArrayList<>(names.size());
        for (String name : names) {
            if (name.startsWith("android")) continue;
            //Log.d(LOG_TAG, "SourceSharedElementCallback, mapObsoleteElements: deleting " + name);
            elementsToRemove.add(name);

        }
        return elementsToRemove;
    }

    /**
     * Removes obsolete elements from names and shared elements.
     *
     * @param names Shared element names.
     * @param sharedElements Shared elements.
     * @param elementsToRemove The elements that should be removed.
     */
    private void removeObsoleteElements(List<String> names,
                                        Map<String, View> sharedElements,
                                        List<String> elementsToRemove) {
        if (elementsToRemove.size() > 0) {
            names.removeAll(elementsToRemove);
            for (String elementToRemove : elementsToRemove) {
                sharedElements.remove(elementToRemove);
            }
        }
    }

    /**
     * Puts a shared element to transitions and names.
     *
     * @param names The names for this transition.
     * @param sharedElements The elements for this transition.
     * @param view The view to add.
     */
    private void mapSharedElement(List<String> names, Map<String, View> sharedElements, View view) {

        //Log.d(LOG_TAG, "SourceSharedElementCallback, mapSharedElement adding name: " + view.getTransitionName());
        String transitionName = view.getTransitionName();
        names.add(transitionName);
        sharedElements.put(transitionName, view);
    }
}
