package com.erank.koletsionpods.viewmodels;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.erank.koletsionpods.adapters.PodcastAdapter;
import com.erank.koletsionpods.db.UserDataSource;
import com.erank.koletsionpods.utils.listeners.OnPodcastClickListener;
import com.google.android.gms.tasks.Task;

public class ProfileFragmentViewModel extends AndroidViewModel {

    private UserDataSource usersDS;

    public ProfileFragmentViewModel(@NonNull Application application) {
        super(application);
        usersDS = UserDataSource.getInstance();
    }

    public static ProfileFragmentViewModel newInstance(ViewModelStoreOwner owner) {
        return new ViewModelProvider(owner).get(ProfileFragmentViewModel.class);
    }

    public void setSwipeToRemove(RecyclerView favsRv, PodcastAdapter adapter) {
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0
                , ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                remove(viewHolder.getAdapterPosition(), adapter, favsRv.getContext());
            }
        });
        itemTouchHelper.attachToRecyclerView(favsRv);
    }

    public Task<Void> remove(int pos, PodcastAdapter adapter, Context ctx) {

        ProgressDialog dialog = new ProgressDialog(ctx);
        dialog.setTitle("Updating favorites");
        dialog.setMessage("Removing from favorites, please wait...");
        dialog.setCancelable(false);
        dialog.show();

        return usersDS.removeUserPodcastFromFavorites(pos)
                .addOnCompleteListener(task -> dialog.dismiss())
                .addOnSuccessListener(aVoid -> adapter.notifyItemRemoved(pos));
    }

    public PodcastAdapter getNewPodcastAdapter(OnPodcastClickListener listener) {
        return new PodcastAdapter(usersDS.getFavorites(), listener, true);
    }

    public boolean hasFavorites() {
        return !usersDS.getFavorites().isEmpty();
    }
}
