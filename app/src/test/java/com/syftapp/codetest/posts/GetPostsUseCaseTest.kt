package com.syftapp.codetest.posts

import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.data.model.domain.User
import com.syftapp.codetest.data.repository.BlogRepository
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test

class GetPostsUseCaseTest {

    private val repository = mockk<BlogRepository>()
    private val sut = GetPostsUseCase(repository)

    @Test
    fun `execute Should return correct posts`() {
        val user1 = mockkClass(User::class)
        val user2 = mockkClass(User::class)
        every { repository.getUsers() } returns Single.just(listOf(user1, user2))

        val page = 0
        val post1 = mockkClass(Post::class)
        val post2 = mockkClass(Post::class)
        every { repository.getPosts(page) } returns Observable.just(listOf(post1, post2))

        val testObserver = sut.execute(page).test()

        testObserver.assertValue(listOf(post1, post2))

        coVerifyOrder {
            repository.getUsers()
            repository.getPosts(page)
        }
    }
}