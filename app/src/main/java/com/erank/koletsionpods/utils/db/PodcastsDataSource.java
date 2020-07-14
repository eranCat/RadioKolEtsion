package com.erank.koletsionpods.utils.db;

import androidx.annotation.NonNull;

import com.erank.koletsionpods.utils.db.models.Comment;
import com.erank.koletsionpods.utils.db.models.Podcast;
import com.erank.koletsionpods.utils.db.models.User;
import com.erank.koletsionpods.utils.listeners.FBPodcastsCallback;
import com.erank.koletsionpods.utils.listeners.PodcastsLoadingListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PodcastsDataSource {

    private static final String PODCASTS_TABLE_NAME = "podcasts";
    private static final String TAG = PodcastsDataSource.class.getName();
    private static PodcastsDataSource instance;

    private List<Podcast> podcasts;
    private Map<String, Integer> podsOriginalIndices;

    private PodcastsDataSource() {
        podcasts = new ArrayList<>();
        podsOriginalIndices = new HashMap<>();
    }

    public static PodcastsDataSource getInstance() {
        if (instance == null)
            instance = new PodcastsDataSource();

        return instance;
    }

    @NotNull
    private DatabaseReference getPodsReference() {
        return FirebaseDatabase.getInstance().getReference(PODCASTS_TABLE_NAME);
    }

    public Task<Void> updateLike(boolean hasLike, Podcast podcast, FirebaseUser user) {
        //TODO optional add listener for like updates
        String uid = user.getUid();
        return getPodsReference().child(podcast.getId())
                .child("likes")
                .child(uid).setValue(hasLike ? true : null)
                .addOnSuccessListener(command -> {
                    if (hasLike) {
                        podcast.addLike(uid);
                    } else {
                        podcast.removeLike(uid);
                    }
                });
    }

    public void loadPodcastsFromFB(FBPodcastsCallback listener) {
        Query query = getPodsReference().orderByChild("description");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                convertSnapshotValues(dataSnapshot, listener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onCancelled(error.toException());
            }
        });
    }

    private void convertSnapshotValues(@NonNull DataSnapshot snapshot, FBPodcastsCallback callback) {
        if (!snapshot.exists() || !snapshot.hasChildren()) {
            return;
        }

        long childrenCount = snapshot.getChildrenCount();
        callback.onLoading(childrenCount);

        podcasts.clear();
        podsOriginalIndices.clear();
        int i = 0;
        for (DataSnapshot child : snapshot.getChildren()) {
            Podcast podcast = child.getValue(Podcast.class);
            if (podcast == null) continue;

            podcasts.add(podcast);
            podsOriginalIndices.put(podcast.getId(), i++);
            callback.onItemLoaded();
        }
        callback.onLoaded(podcasts);
    }

    public void setPodcastsEditable(String uid) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user.isAnonymous()) return;

        boolean isAdmin = Objects.requireNonNull(user.getEmail()).equals("eranka12@gmail.com");
        for (Podcast podcast : podcasts) {
            for (Comment comment : podcast.getCommentsList()) {
                comment.setEditable(isAdmin || comment.getUid().equals(uid));
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

                getPodsReference().updateChildren(podcastsToMap(podcasts))
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
    private Map<String, Object> podcastsToMap(List<Podcast> podcasts) {
        Map<String, Object> podcastsMap = new HashMap<>();
        for (Podcast p : podcasts) {
//            podcastsMap.put(p.getId(), p);
            podcastsMap.put(p.getId(), p.toMap());
        }
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
        return getPodsReference().child(podcast.getId()).child("comments");
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
