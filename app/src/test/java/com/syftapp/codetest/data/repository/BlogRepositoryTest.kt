package com.syftapp.codetest.data.repository

import com.syftapp.codetest.data.api.BlogApi
import com.syftapp.codetest.data.dao.CommentDao
import com.syftapp.codetest.data.dao.PostDao
import com.syftapp.codetest.data.dao.UserDao
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.data.model.domain.User
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class BlogRepositoryTest {

    companion object {
        private const val INITIAL_PAGE = 0
    }

    @RelaxedMockK
    lateinit var postDao: PostDao
    @RelaxedMockK
    lateinit var commentDao: CommentDao
    @RelaxedMockK
    lateinit var userDao: UserDao
    @MockK
    lateinit var blogApi: BlogApi

    private val sut by lazy {
        BlogRepository(postDao, commentDao, userDao, blogApi)
    }

    private val anyUser = User(1, "name", "username", "email")
    private val anyPost1 = Post(1, 1, "title1", "body1")
    private val anyPost2 = Post(2, 2, "title2", "body2")

    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun `get users returns cached values if available`() {
        every { userDao.getAll() } returns Single.just(listOf(anyUser))

        val observer = sut.getUsers().test()
        observer.assertValue(listOf(anyUser))
        verify(exactly = 0) { blogApi.getUsers() }
    }

    @Test
    fun `get posts emits cached values, retrieves remote values, saves remote values and emits new cached values`() {
        every { postDao.getAll() } returnsMany listOf(
            Single.just(listOf(anyPost1)),
            Single.just(listOf(anyPost2))
        )
        every { postDao.insertAll(anyPost1) } returns Completable.complete()
        every { postDao.insertAll(anyPost2) } returns Completable.complete()
        every { blogApi.getPosts(INITIAL_PAGE) } returns Observable.just(listOf(anyPost2))

        val testObserver = sut.getPosts(INITIAL_PAGE).test()

        testObserver.assertValues(listOf(anyPost1), listOf(anyPost2))
        verify(exactly = 2) { postDao.getAll() }
        verify(exactly = 1) { blogApi.getPosts(INITIAL_PAGE) }
    }

    @Test
    fun `posts value fetched from api is inserted to the cache`() {
        every { postDao.getAll() } returns Single.just(listOf())
        every { blogApi.getPosts(INITIAL_PAGE) } returns Observable.just(listOf(anyPost1))

        sut.getPosts(INITIAL_PAGE).test()

        verify {
            blogApi.getPosts(INITIAL_PAGE)
            postDao.insertAll(*listOf(anyPost1).toTypedArray())
        }
    }

    @Test
    fun `users fetched from api are inserted in to the cache`() {
        every { userDao.getAll() } returns Single.just(listOf())
        every { blogApi.getUsers() } returns Single.just(listOf(anyUser))

        sut.getUsers().test()

        verify {
            blogApi.getUsers()
            userDao.insertAll(*listOf(anyUser).toTypedArray())
        }
    }

    @Test
    fun `api failing returns reactive error on chain`() {
        val page = 0
        every { postDao.getAll() } returns Single.just(listOf())
        val error = Throwable()
        every { blogApi.getPosts(page) } throws error

        val observer = sut.getPosts(page).test()

        observer.assertError(error)
    }
}