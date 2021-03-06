package com.syftapp.codetest.posts

import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.data.repository.BlogDataProvider
import io.reactivex.Observable
import org.koin.core.KoinComponent

class GetPostsUseCase(private val repository: BlogDataProvider) : KoinComponent {

    fun execute(page: Int): Observable<List<Post>> {
        // users must be available for the blog posts
        return repository.getUsers()
            .ignoreElement()
            .andThen(repository.getPosts(page))
    }

}