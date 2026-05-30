package cn.edu.app.douyu.core.network

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.core.DataStore
import cn.edu.app.douyu.core.model.AuthSession
import cn.edu.app.douyu.core.model.TokenPair
import kotlinx.coroutines.flow.first

interface AccessTokenProvider {
    fun accessToken(): String?
}

interface RefreshTokenProvider {
    fun refreshToken(): String?
}

interface TokenStore : AccessTokenProvider, RefreshTokenProvider {
    suspend fun save(session: AuthSession)
    suspend fun save(pair: TokenPair)
    suspend fun clear()
}

class InMemoryTokenStore : TokenStore {
    @Volatile
    private var tokens: TokenPair? = null

    override fun accessToken(): String? = tokens?.accessToken

    override fun refreshToken(): String? = tokens?.refreshToken

    override suspend fun save(session: AuthSession) {
        tokens = TokenPair(session.accessToken, session.refreshToken, session.expiresIn)
    }

    override suspend fun save(pair: TokenPair) {
        tokens = pair
    }

    override suspend fun clear() {
        tokens = null
    }
}

class SwitchableTokenStore(
    initialTokenStore: TokenStore = InMemoryTokenStore()
) : TokenStore {
    @Volatile
    private var delegate: TokenStore = initialTokenStore

    fun switchTo(tokenStore: TokenStore) {
        delegate = tokenStore
    }

    override fun accessToken(): String? = delegate.accessToken()

    override fun refreshToken(): String? = delegate.refreshToken()

    override suspend fun save(session: AuthSession) {
        delegate.save(session)
    }

    override suspend fun save(pair: TokenPair) {
        delegate.save(pair)
    }

    override suspend fun clear() {
        delegate.clear()
    }
}

class DataStoreTokenStore(
    private val dataStore: DataStore<Preferences>
) : TokenStore {
    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var cachedRefreshToken: String? = null

    override fun accessToken(): String? = cachedAccessToken

    override fun refreshToken(): String? = cachedRefreshToken

    suspend fun hydrate() {
        val snapshot = dataStore.data.first()
        cachedAccessToken = snapshot[Keys.ACCESS_TOKEN]
        cachedRefreshToken = snapshot[Keys.REFRESH_TOKEN]
    }

    override suspend fun save(session: AuthSession) {
        save(TokenPair(session.accessToken, session.refreshToken, session.expiresIn))
    }

    override suspend fun save(pair: TokenPair) {
        cachedAccessToken = pair.accessToken
        cachedRefreshToken = pair.refreshToken
        dataStore.edit {
            it[Keys.ACCESS_TOKEN] = pair.accessToken
            it[Keys.REFRESH_TOKEN] = pair.refreshToken
            it[Keys.EXPIRES_IN] = pair.expiresIn
        }
    }

    override suspend fun clear() {
        cachedAccessToken = null
        cachedRefreshToken = null
        dataStore.edit {
            it.remove(Keys.ACCESS_TOKEN)
            it.remove(Keys.REFRESH_TOKEN)
            it.remove(Keys.EXPIRES_IN)
        }
    }

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val EXPIRES_IN = longPreferencesKey("expires_in")
    }
}
