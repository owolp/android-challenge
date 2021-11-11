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
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Ignore
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
    private val anyPost = Post(1, 1, "title", "body")

    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun `get users returns cached values if available`() {
        every { userDao.getAll() } returns Single.just(listOf(anyUser))

        val observer = sut.getUsers().test()
        observer.assertValue(listOf(anyUser))
        verify(exactly = 0) { blogApi.getUsers() }
    }

    @Ignore("Fix by testing fetchDataObservable")
    @Test
    fun `get posts returns cached values if available`() {
        every { postDao.getAll() } returns Single.just(listOf(anyPost))

        val observer = sut.getPosts(INITIAL_PAGE).test()
        observer.assertValue(listOf(anyPost))
        verify(exactly = 0) { blogApi.getPosts(INITIAL_PAGE) }
    }

    @Test
    fun `posts value fetched from api is inserted to the cache`() {
        every { postDao.getAll() } returns Single.just(listOf())
        every { blogApi.getPosts(INITIAL_PAGE) } returns Observable.just(listOf(anyPost))

        sut.getPosts(INITIAL_PAGE).test()

        verify {
            blogApi.getPosts(INITIAL_PAGE)
            postDao.insertAll(*listOf(anyPost).toTypedArray())
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

    @Ignore("Fix by testing fetchDataObservable")
    @Test
    fun `value from api is returned to caller`() {
        every { userDao.getAll() } returns Single.just(listOf())
        every { postDao.getAll() } returns Single.just(listOf())
        every { blogApi.getPosts(INITIAL_PAGE) } returns Observable.just(listOf(anyPost))
        every { blogApi.getUsers() } returns Single.just(listOf(anyUser))

        val postObserver = sut.getPosts(INITIAL_PAGE).test()
        val userObserver = sut.getUsers().test()

        postObserver.assertValue(listOf(anyPost))
        userObserver.assertValue(listOf(anyUser))
    }

    @Ignore("Fix by testing fetchDataObservable")
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