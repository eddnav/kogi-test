package com.nav.kogi.test.gallery;

import com.nav.kogi.test.shared.annotation.Activities;
import com.nav.kogi.test.shared.api.Api;
import com.nav.kogi.test.shared.api.PostsResponse;
import com.nav.kogi.test.shared.cache.Cache;
import com.nav.kogi.test.shared.models.Post;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Eduardo Naveda
 */
@Activities
public class GalleryPresenter {

    public final String CLIENT_ID = "05132c49e9f148ec9b8282af33f88ac7";

    private Api api;

    private GalleryView galleryView;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private List<Post> posts = new ArrayList<>();

    @Inject
    public GalleryPresenter(Api api) {
        this.api = api;
    }

    public void takeView(GalleryView galleryView) {
        this.galleryView = galleryView;
    }

    public void fetchPopular() {
        subscriptions.add(api.getPopularPosts(CLIENT_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PostsResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadCachedPopularPosts();
                        // notify gallery view of network error.
                    }

                    @Override
                    public void onNext(PostsResponse postsResponse) {
                        Cache.putPopularPostsResponse(postsResponse);
                        GalleryPresenter.this.posts.clear();
                        GalleryPresenter.this.posts.addAll(postsResponse.getPosts());
                        if (galleryView != null) {
                            galleryView.refresh();
                            galleryView.setSelectedPost(0);
                        }
                    }
                }));
    }

    /**
     * Returns true if loading from the cache was successful (posts returned non null and at least 1).
     *
     * @return true if successful, false otherwise.
     */
    public boolean loadCachedPopularPosts() { // TODO test this.
        PostsResponse cachedPosts = Cache.getPopularPostsResponse();
        if (cachedPosts != null && cachedPosts.getPosts().size() > 0) {
            posts.addAll(cachedPosts.getPosts());
            galleryView.refresh();
            return true;
        } else
            return false;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void dropView() {
        this.galleryView = null;
        this.subscriptions.unsubscribe();
    }


    public void selectPost(int position) {
        galleryView.setSelectedPost(position);
    }
}
