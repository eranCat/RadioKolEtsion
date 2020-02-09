package com.erank.koletsionpods.db;

import androidx.annotation.NonNull;

import com.erank.koletsionpods.db.models.Comment;
import com.erank.koletsionpods.db.models.Podcast;
import com.erank.koletsionpods.db.models.User;
import com.erank.koletsionpods.utils.listeners.FBPodcastsCallback;
import com.erank.koletsionpods.utils.listeners.PodcastsLoadingListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PodcastsDataSource {

    private static final String PODCASTS_TABLE_NAME = "podcasts";
    private static final String TAG = PodcastsDataSource.class.getName();
    private static PodcastsDataSource instance;

    private DatabaseReference podcastsRef;
    private List<Podcast> podcasts;
    private Map<String, Integer> podsOriginalIndices;
    private FirebaseAuth mAuth;

    private PodcastsDataSource() {
        podcasts = new ArrayList<>();
        podsOriginalIndices = new HashMap<>();
        podcastsRef = FirebaseDatabase.getInstance()
                .getReference(PODCASTS_TABLE_NAME);
        mAuth = FirebaseAuth.getInstance();
    }

    public static PodcastsDataSource getInstance() {
        return instance != null ? instance : (instance = new PodcastsDataSource());
    }

    public Task<Void> updateLike(boolean isLiked, Podcast podcast, FirebaseUser user) {
        //TODO optional add listener for like updates
        String uid = user.getUid();
        return podcastsRef.child(podcast.getId())
                .child("likes")
                .child(uid).setValue(isLiked ? true : null)
                .addOnSuccessListener(command -> {
                    if (isLiked) {
                        podcast.addLike(uid);
                    } else {
                        podcast.removeLike(uid);
                    }
                });
    }

    public void loadPodcastsFromFB(FBPodcastsCallback listener) {
        podcastsRef.orderByChild("description")//sort
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists() || !dataSnapshot.hasChildren()) {
                            return;
                        }

                        listener.onLoading(dataSnapshot.getChildrenCount());

                        podcasts.clear();
                        podsOriginalIndices.clear();
                        int i = 0;
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            Podcast podcast = child.getValue(Podcast.class);
                            if (podcast == null) break;

                            podcasts.add(podcast);
                            podsOriginalIndices.put(podcast.getId(), i++);
                            listener.onItemLoaded();
                        }
                        listener.onLoaded(podcasts);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onCancelled(error.toException());
                    }
                });
    }

    public void setPodcastsEditable(String uid) {
        for (Podcast podcast : podcasts) {
            for (Comment comment : podcast.getCommentsList()) {
                comment.setEditable(comment.getUid().equals(uid));
            }
        }
    }


    public void sortByName() {
        Collections.sort(podcasts, new Podcast.NameComparator());
    }

    public void getFromJsonToFirebase(PodcastsLoadingListener listener) {
        listener.onLoading();
        new PodcastsFromJsonAsyncTask(new PodcastsLoadingListener() {
            @Override
            public void onLoaded(List<Podcast> podcasts) {

                PodcastsDataSource.this.podcasts = podcasts;

                podcastsRef.setValue(podcastsToMap(podcasts))
                        .addOnSuccessListener(c -> listener.onLoaded(podcasts))
                        .addOnFailureListener(listener::onCancelled);
            }

            @Override
            public void onCancelled(Exception error) {
                listener.onCancelled(error);
            }
        }).execute();
    }

    @NotNull
    private Map<String, Podcast> podcastsToMap(List<Podcast> podcasts) {
        Map<String, Podcast> podcastsMap = new HashMap<>();
        for (Podcast p : podcasts) podcastsMap.put(p.getId(), p);
        return podcastsMap;
    }

    public void refresh(FBPodcastsCallback listener) {
        loadPodcastsFromFB(listener);
    }

    public int getPodcastsSize() {
        return podcasts.size();
    }


    public List<Podcast> getPodcasts() {
        return podcasts;
    }

    public List<Podcast> getPodcastsFiltered(@NonNull String filter) {
        filter = filter.toLowerCase();

        List<Podcast> afterFiler = new ArrayList<>();

        for (Podcast p : podcasts) {
            String desc = p.getDescription().toLowerCase();
            if (desc.contains(filter)) afterFiler.add(p);
        }

        return afterFiler;
    }

    public Podcast getPodcast(int pos) {
        return podcasts.get(pos);
    }

    public List<Podcast> getFavorites(User user) {
        List<Podcast> favs = new ArrayList<>();
        Set<String> favoritesIds = user.getFavoritesIdsSet();
        for (String k : favoritesIds) {
            Integer index = podsOriginalIndices.get(k);
            Podcast podcast = podcasts.get(index);
            if (podcast != null)
                favs.add(podcast);
        }

        return favs;
    }

    public Comment commentOnPost(String content, Podcast podcast) {
//        TODO do it
        DatabaseReference pushedCommentsRef = getCommentsRef(podcast).push();

        User user = UserDataSource.getInstance().getCurrentUser();

        String cid = pushedCommentsRef.getKey();
        Comment comment = new Comment(cid, content, user);
        pushedCommentsRef.setValue(comment);//return task and add later
        return comment;
    }

    @NotNull
    private DatabaseReference getCommentsRef(Podcast podcast) {
        return podcastsRef.child(podcast.getId()).child("comments");
    }

    @NotNull
    private DatabaseReference getCommentRef(Podcast podcast, Comment comment) {
        return getCommentsRef(podcast).child(comment.getId());
    }

    public Task<Void> removeComment(Podcast podcast, Comment comment) {
        return getCommentRef(podcast, comment).removeValue();
    }

    public Task<Void> updateComment(Podcast podcast, Comment comment, String content) {
//        return getCommentRef(podcast,comment).updateChildren(comment.toMap());
        Map<String, Object> map = new HashMap<>();
        map.put("content", content);
        return getCommentRef(podcast, comment).updateChildren(map);
    }

    public int indexOf(Podcast podcast) {
        return indexOf(podcast.getId());
    }

    public int indexOf(String podId) {
        Integer integer = podsOriginalIndices.get(podId);
        return integer == null ? -1 : integer;
    }
}
