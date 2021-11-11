package com.syftapp.codetest.data.repository

import android.annotation.SuppressLint
import com.syftapp.codetest.data.api.BlogApi
import com.syftapp.codetest.data.dao.CommentDao
import com.syftapp.codetest.data.dao.PostDao
import com.syftapp.codetest.data.dao.UserDao
import com.syftapp.codetest.data.model.domain.Comment
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.data.model.domain.User
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.koin.core.KoinComponent

class BlogRepository(
    private val postDao: PostDao,
    private val commentDao: CommentDao,
    private val userDao: UserDao,
    private val blogApi: BlogApi
) : KoinComponent, BlogDataProvider {

    override fun getUsers(): Single<List<User>> {
        return fetchData(
            local = { userDao.getAll() },
            remote = { blogApi.getUsers() },
            insert = { value -> userDao.insertAll(*value.toTypedArray()) }
        )
    }

    override fun getComments(): Single<List<Comment>> {
        return fetchData(
            local = { commentDao.getAll() },
            remote = { blogApi.getComments() },
            insert = { value -> commentDao.insertAll(*value.toTypedArray()) }
        )
    }

    override fun getPosts(page: Int): Observable<List<Post>> {
        return fetchDataObservable(
            local = { postDao.getAll().toObservable() },
            remote = { blogApi.getPosts(page) },
            insert = { value -> postDao.insertAll(*value.toTypedArray()) }
        )
    }

    fun getPost(postId: Int): Maybe<Post> {
        return postDao.get(postId)
    }

    private fun <T> fetchData(
        local: () -> Single<List<T>>,
        remote: () -> Single<List<T>>,
        insert: (insertValue: List<T>) -> Completable
    ): Single<List<T>> {

        return local.invoke()
            .flatMap {
                if (it.isNotEmpty()) {
                    Single.just(it)
                } else {
                    remote.invoke()
                        .map { value ->
                            insert.invoke(value).subscribe();
                            value
                        }
                }
            }
    }

    /**
     * Single source of truth is always local
     *
     * Upon subscribe emit data from local, retrieve data from remote and insert it, and then emit
     * data from local again
     *
     * In case of exception during any of the data retrieval emit error
     */
    @SuppressLint("CheckResult")
    private fun <T> fetchDataObservable(
        local: () -> Observable<List<T>>,
        remote: () -> Observable<List<T>>,
        insert: (insertValue: List<T>) -> Completable
    ): Observable<List<T>> {

        return Observable.create { emitter ->
            local.invoke()
                .flatMap { offlineList ->
                    if (!emitter.isDisposed) {
                        emitter.onNext(offlineList)
                    }
                    remote.invoke()
                }.flatMapCompletable { remoteList ->
                    insert.invoke(remoteList)
                }.andThen(local.invoke()).map { updatedList ->
                    if (!emitter.isDisposed) {
                        emitter.onNext(updatedList)
                        emitter.onComplete()
                    }
                }.onExceptionResumeNext {
                    emitter.onError(Exception())
                }.subscribe({}, { emitter.onError(it) })
        }
    }
}
