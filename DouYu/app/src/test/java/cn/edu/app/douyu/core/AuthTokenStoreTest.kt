package cn.edu.app.douyu.core

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import cn.edu.app.douyu.core.model.TokenPair
import cn.edu.app.douyu.core.network.DataStoreTokenStore
import cn.edu.app.douyu.core.network.InMemoryTokenStore
import cn.edu.app.douyu.core.network.SwitchableTokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AuthTokenStoreTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun dataStoreTokenStoreHydratesSavedTokens() = runBlocking {
        val storeFile = temporaryFolder.newFile("auth_tokens.preferences_pb")
        val firstScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val firstStore = dataStoreTokenStore(storeFile, firstScope)

        firstStore.save(TokenPair("access_saved", "refresh_saved", 3600))
        firstScope.cancel()

        val secondScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val secondStore = dataStoreTokenStore(storeFile, secondScope)
        secondStore.hydrate()

        assertEquals("access_saved", secondStore.accessToken())
        assertEquals("refresh_saved", secondStore.refreshToken())

        secondScope.cancel()
    }

    @Test
    fun switchableTokenStoreRoutesOperationsToCurrentStore() = runBlocking {
        val switchableTokenStore = SwitchableTokenStore()
        val persistentStore = InMemoryTokenStore()

        switchableTokenStore.save(TokenPair("access_memory", "refresh_memory", 3600))
        persistentStore.save(TokenPair("access_persistent", "refresh_persistent", 3600))
        switchableTokenStore.switchTo(persistentStore)

        assertEquals("access_persistent", switchableTokenStore.accessToken())
        assertEquals("refresh_persistent", switchableTokenStore.refreshToken())

        switchableTokenStore.clear()
        assertNull(persistentStore.accessToken())
        assertNull(persistentStore.refreshToken())
    }

    private fun dataStoreTokenStore(file: File, scope: CoroutineScope): DataStoreTokenStore {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file }
        )
        return DataStoreTokenStore(dataStore)
    }
}
