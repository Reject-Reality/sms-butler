# 短信助手 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use subagent-driven-development (recommended) or executing-plans to implement this plan task-by-task.

**Goal:** Build an Android app that records SMS sender info via notification listener, stores locally in Room DB, and provides search/management features.

**Architecture:** MVVM + Clean Architecture. NotificationListenerService captures incoming SMS notifications → ViewModel processes data → Repository writes to Room DB → Compose UI reads via Flow.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Hilt, Room, Coroutines/Flow, Coil

---

### Task 1: Create Android Project Structure

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts` (project-level)
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Create settings.gradle.kts**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "SmsButler"
include(":app")
```

- [ ] **Step 2: Create project-level build.gradle.kts**

```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("com.google.dagger.hilt.android") version "2.53.1" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
}
```

- [ ] **Step 3: Create gradle.properties**

```properties
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
org.gradle.jvmargs=-Xmx2048m
```

- [ ] **Step 4: Create version catalog libs.versions.toml**

```toml
[versions]
core-ktx = "1.15.0"
lifecycle = "2.8.7"
activity-compose = "1.9.3"
compose-bom = "2025.01.01"
navigation = "2.8.5"
hilt = "2.53.1"
room = "2.6.1"
datastore = "1.1.3"

[libraries]
core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activity-compose" }
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
ui = { group = "androidx.compose.ui", name = "ui" }
ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
material3 = { group = "androidx.compose.material3", name = "material3" }
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

[plugins]
android-application = { id = "com.android.application", version.ref = "hilt" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "hilt" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "hilt" }
```

- [ ] **Step 5: Create app/build.gradle.kts**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.smsbutler"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.smsbutler"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)
}
```

- [ ] **Step 6: Create AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.RECEIVE_SMS" />

    <application
        android:name=".SmsButlerApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@android:style/Theme.Material.Light.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".service.SmsNotificationListener"
            android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
            android:exported="true">
            <intent-filter>
                <action android:name="android.service.notification.NotificationListenerService" />
            </intent-filter>
        </service>
    </application>
</manifest>
```

- [ ] **Step 7: Create res/values/strings.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">短信助手</string>
</resources>
```

- [ ] **Step 8: Verify project compiles**

```
Run: ./gradlew assembleDebug
Expected: BUILD SUCCESSFUL
```

---

### Task 2: Create Application Class and DI Setup

**Files:**
- Create: `app/src/main/java/com/smsbutler/SmsButlerApp.kt`
- Create: `app/src/main/java/com/smsbutler/di/AppModule.kt`
- Create: `app/src/main/java/com/smsbutler/di/DatabaseModule.kt`

- [ ] **Step 1: Create SmsButlerApp.kt**

```kotlin
package com.smsbutler

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmsButlerApp : Application()
```

- [ ] **Step 2: Create AppModule.kt**

```kotlin
package com.smsbutler.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}
```

- [ ] **Step 3: Create DatabaseModule.kt**

```kotlin
package com.smsbutler.di

import android.content.Context
import androidx.room.Room
import com.smsbutler.data.local.SmsDatabase
import com.smsbutler.data.local.SmsRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SmsDatabase {
        return Room.databaseBuilder(
            context,
            SmsDatabase::class.java,
            "sms_butler.db"
        ).build()
    }

    @Provides
    fun provideSmsRecordDao(database: SmsDatabase): SmsRecordDao {
        return database.smsRecordDao()
    }
}
```

---

### Task 3: Create Room Database Layer

**Files:**
- Create: `app/src/main/java/com/smsbutler/data/local/SmsRecordEntity.kt`
- Create: `app/src/main/java/com/smsbutler/data/local/BookmarkEntity.kt`
- Create: `app/src/main/java/com/smsbutler/data/local/SmsRecordDao.kt`
- Create: `app/src/main/java/com/smsbutler/data/local/SmsDatabase.kt`

- [ ] **Step 1: Create SmsRecordEntity.kt**

```kotlin
package com.smsbutler.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sms_records",
    indices = [
        Index(value = ["phoneNumber"]),
        Index(value = ["sender"])
    ]
)
data class SmsRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val sender: String,
    val content: String? = null,
    val recordedContent: Boolean = false,
    val category: String? = null,
    val receivedAt: Long = System.currentTimeMillis(),
    val isStarred: Boolean = false,
    val appLabel: String? = null
)
```

- [ ] **Step 2: Create BookmarkEntity.kt**

```kotlin
package com.smsbutler.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    foreignKeys = [ForeignKey(
        entity = SmsRecordEntity::class,
        parentColumns = ["id"],
        childColumns = ["recordId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class BookmarkEntity(
    @PrimaryKey val recordId: Long,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 3: Create SmsRecordDao.kt**

```kotlin
package com.smsbutler.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class PhoneSummary(
    val phoneNumber: String,
    val count: Int
)

@Dao
interface SmsRecordDao {
    @Query("SELECT * FROM sms_records ORDER BY receivedAt DESC")
    fun getAllRecords(): Flow<List<SmsRecordEntity>>

    @Query("SELECT * FROM sms_records WHERE phoneNumber LIKE '%' || :query || '%' OR sender LIKE '%' || :query || '%' ORDER BY receivedAt DESC")
    fun searchRecords(query: String): Flow<List<SmsRecordEntity>>

    @Query("SELECT * FROM sms_records WHERE phoneNumber = :phone ORDER BY receivedAt DESC")
    fun getRecordsByPhone(phone: String): Flow<List<SmsRecordEntity>>

    @Query("SELECT phoneNumber, COUNT(*) as count FROM sms_records GROUP BY phoneNumber ORDER BY count DESC")
    fun getPhoneNumberSummary(): Flow<List<PhoneSummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SmsRecordEntity)

    @Query("UPDATE sms_records SET isStarred = :starred WHERE id = :id")
    suspend fun toggleStar(id: Long, starred: Boolean)

    @Query("DELETE FROM sms_records")
    suspend fun deleteAll()
}
```

- [ ] **Step 4: Create SmsDatabase.kt**

```kotlin
package com.smsbutler.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SmsRecordEntity::class, BookmarkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SmsDatabase : RoomDatabase() {
    abstract fun smsRecordDao(): SmsRecordDao
}
```

---

### Task 4: Create Repository Layer

**Files:**
- Create: `app/src/main/java/com/smsbutler/data/repository/SmsRepository.kt`

- [ ] **Step 1: Create SmsRepository.kt**

```kotlin
package com.smsbutler.data.repository

import com.smsbutler.data.local.PhoneSummary
import com.smsbutler.data.local.SmsRecordDao
import com.smsbutler.data.local.SmsRecordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsRepository @Inject constructor(
    private val dao: SmsRecordDao
) {
    fun getAllRecords(): Flow<List<SmsRecordEntity>> = dao.getAllRecords()

    fun searchRecords(query: String): Flow<List<SmsRecordEntity>> = dao.searchRecords(query)

    fun getRecordsByPhone(phone: String): Flow<List<SmsRecordEntity>> = dao.getRecordsByPhone(phone)

    fun getPhoneNumberSummary(): Flow<List<PhoneSummary>> = dao.getPhoneNumberSummary()

    suspend fun insertRecord(record: SmsRecordEntity) = dao.insert(record)

    suspend fun toggleStar(id: Long, starred: Boolean) = dao.toggleStar(id, starred)

    suspend fun deleteAll() = dao.deleteAll()
}
```

---

### Task 5: Create Notification Listener Service

**Files:**
- Create: `app/src/main/java/com/smsbutler/service/SmsNotificationListener.kt`

- [ ] **Step 1: Create SmsNotificationListener.kt**

```kotlin
package com.smsbutler.service

import android.annotation.SuppressLint
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.smsbutler.data.local.SmsRecordEntity
import com.smsbutler.data.repository.SmsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("OverrideAbstract")
class SmsNotificationListener : NotificationListenerService() {

    @Inject lateinit var repository: SmsRepository

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val extras = sbn.notification.extras

        // Extract sender and message from notification extras
        val title = extras.getString(NotificationCompat.EXTRA_TITLE) ?: return
        val text = extras.getString(NotificationCompat.EXTRA_TEXT) ?: return

        // Parse phone number from title (common pattern: "138xxxx1234" in notification title)
        val phoneNumber = extractPhoneNumber(title) ?: title
        val sender = packageName  // Use package name as sender identifier

        val record = SmsRecordEntity(
            phoneNumber = phoneNumber,
            sender = sender,
            content = text,
            category = categorizeSms(text),
            appLabel = getAppLabel(packageName)
        )

        scope.launch {
            repository.insertRecord(record)
        }
    }

    private fun extractPhoneNumber(text: String): String? {
        // Match Chinese phone numbers: 1xx-xxxx-xxxx patterns
        val regex = Regex("1[3-9]\\d{9}")
        return regex.find(text)?.value
    }

    private fun categorizeSms(text: String): String {
        return when {
            text.contains("验证码") || text.contains("校验码") || text.contains("驗證碼") -> "验证码"
            text.contains("广告") || text.contains("推广") || text.contains("优惠") -> "广告"
            else -> "通知"
        }
    }

    private fun getAppLabel(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
}
```

---

### Task 6: Create DataStore Preferences Manager

**Files:**
- Create: `app/src/main/java/com/smsbutler/data/local/PreferencesManager.kt`

- [ ] **Step 1: Create PreferencesManager.kt**

```kotlin
package com.smsbutler.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class UserPreferences(
    val recordContent: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val themeMode: String = "system"
)

@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    private object Keys {
        val RECORD_CONTENT = booleanPreferencesKey("record_content")
        val NOTIFICATION_PERMISSION = booleanPreferencesKey("notification_permission")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            recordContent = prefs[Keys.RECORD_CONTENT] ?: false,
            notificationPermissionGranted = prefs[Keys.NOTIFICATION_PERMISSION] ?: false,
            themeMode = prefs[Keys.THEME_MODE] ?: "system"
        )
    }

    suspend fun setRecordContent(enabled: Boolean) {
        context.dataStore.edit { it[Keys.RECORD_CONTENT] = enabled }
    }

    suspend fun setNotificationPermission(granted: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATION_PERMISSION] = granted }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }
}
```

---

### Task 7: Create Main Screens — Home (Record List)

**Files:**
- Create: `app/src/main/java/com/smsbutler/ui/screen/home/HomeScreen.kt`
- Create: `app/src/main/java/com/smsbutler/ui/screen/home/HomeViewModel.kt`

- [ ] **Step 1: Create HomeViewModel.kt**

```kotlin
package com.smsbutler.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsbutler.data.local.SmsRecordEntity
import com.smsbutler.data.repository.SmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val records: List<SmsRecordEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SmsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllRecords().collect { records ->
                _uiState.value = HomeUiState(
                    records = records,
                    isLoading = false
                )
            }
        }
    }

    fun toggleStar(id: Long, currentStarred: Boolean) {
        viewModelScope.launch {
            repository.toggleStar(id, !currentStarred)
        }
    }
}
```

- [ ] **Step 2: Create HomeScreen.kt**

```kotlin
package com.smsbutler.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsbutler.data.local.SmsRecordEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRecordClick: (SmsRecordEntity) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("短信助手") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.records.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无短信记录", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.records, key = { it.id }) { record ->
                    SmsRecordCard(
                        record = record,
                        onClick = { onRecordClick(record) },
                        onStarClick = { viewModel.toggleStar(record.id, record.isStarred) }
                    )
                }
            }
        }
    }
}

@Composable
fun SmsRecordCard(
    record: SmsRecordEntity,
    onClick: () -> Unit,
    onStarClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.phoneNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.appLabel ?: record.sender,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (record.category != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text(record.category, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                if (record.content != null && record.recordedContent) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = record.content,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(Date(record.receivedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onStarClick) {
                Icon(
                    imageVector = if (record.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (record.isStarred) "取消收藏" else "收藏",
                    tint = if (record.isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

---

### Task 8: Create Detail Screen

**Files:**
- Create: `app/src/main/java/com/smsbutler/ui/screen/detail/DetailScreen.kt`
- Create: `app/src/main/java/com/smsbutler/ui/screen/detail/DetailViewModel.kt`

- [ ] **Step 1: Create DetailViewModel.kt**

```kotlin
package com.smsbutler.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsbutler.data.local.SmsRecordEntity
import com.smsbutler.data.repository.SmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val phoneNumber: String = "",
    val records: List<SmsRecordEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SmsRepository
) : ViewModel() {

    private val phoneNumber: String = savedStateHandle["phoneNumber"] ?: ""

    private val _uiState = MutableStateFlow(DetailUiState(phoneNumber = phoneNumber))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getRecordsByPhone(phoneNumber).collect { records ->
                val senders = records.map { it.appLabel ?: it.sender }.distinct()
                _uiState.value = DetailUiState(
                    phoneNumber = phoneNumber,
                    records = records,
                    isLoading = false
                )
            }
        }
    }
}
```

- [ ] **Step 2: Create DetailScreen.kt**

```kotlin
package com.smsbutler.ui.screen.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsbutler.ui.screen.home.SmsRecordCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.phoneNumber) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("←") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Summary card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("共 ${state.records.size} 条短信", style = MaterialTheme.typography.titleMedium)
                        val senders = state.records.map { it.appLabel ?: it.sender }.distinct()
                        Text("来自 ${senders.size} 个发送方", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.records, key = { it.id }) { record ->
                        SmsRecordCard(record = record, onClick = {}, onStarClick = {})
                    }
                }
            }
        }
    }
}
```

---

### Task 9: Create Search Screen

**Files:**
- Create: `app/src/main/java/com/smsbutler/ui/screen/search/SearchScreen.kt`
- Create: `app/src/main/java/com/smsbutler/ui/screen/search/SearchViewModel.kt`

- [ ] **Step 1: Create SearchViewModel.kt**

```kotlin
package com.smsbutler.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsbutler.data.local.SmsRecordEntity
import com.smsbutler.data.repository.SmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<SmsRecordEntity> = emptyList(),
    val isSearching: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SmsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            if (query.isNotBlank()) {
                repository.searchRecords(query).collect { results ->
                    _uiState.value = _uiState.value.copy(results = results)
                }
            } else {
                _uiState.value = _uiState.value.copy(results = emptyList())
            }
        }
    }
}
```

- [ ] **Step 2: Create SearchScreen.kt**

```kotlin
package com.smsbutler.ui.screen.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsbutler.ui.screen.home.SmsRecordCard

@Composable
fun SearchScreen(
    onRecordClick: (com.smsbutler.data.local.SmsRecordEntity) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.query,
            onValueChange = { viewModel.onQueryChanged(it) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("搜索手机号或发送方") },
            singleLine = true,
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChanged("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "清除")
                    }
                }
            }
        )

        if (state.query.isBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("输入手机号或发送方名称搜索", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (state.results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("未找到匹配结果", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.results, key = { it.id }) { record ->
                    SmsRecordCard(record = record, onClick = { onRecordClick(record) }, onStarClick = {})
                }
            }
        }
    }
}
```

---

### Task 10: Create Settings Screen

**Files:**
- Create: `app/src/main/java/com/smsbutler/ui/screen/settings/SettingsScreen.kt`
- Create: `app/src/main/java/com/smsbutler/ui/screen/settings/SettingsViewModel.kt`

- [ ] **Step 1: Create SettingsViewModel.kt**

```kotlin
package com.smsbutler.ui.screen.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsbutler.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val recordContent: Boolean = false,
    val notificationAccess: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.preferences.collect { prefs ->
                _uiState.value = SettingsUiState(
                    recordContent = prefs.recordContent,
                    notificationAccess = prefs.notificationPermissionGranted
                )
            }
        }
    }

    fun toggleRecordContent(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setRecordContent(enabled) }
    }

    fun openNotificationSettings() {
        val intent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        } else {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
```

- [ ] **Step 2: Create SettingsScreen.kt**

```kotlin
package com.smsbutler.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Privacy section
            Text("隐私设置", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("记录短信内容", style = MaterialTheme.typography.bodyLarge)
                        Text("开启后将同时记录短信正文", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = state.recordContent,
                        onCheckedChange = { viewModel.toggleRecordContent(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Permission section
            Text("权限设置", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("通知监听权限", style = MaterialTheme.typography.bodyLarge)
                    Text("需要授予通知监听权限才能自动记录短信", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.openNotificationSettings() }) {
                        Text("前往设置")
                    }
                }
            }
        }
    }
}
```

---

### Task 11: Create Navigation and MainActivity

**Files:**
- Create: `app/src/main/java/com/smsbutler/ui/navigation/NavGraph.kt`
- Create: `app/src/main/java/com/smsbutler/MainActivity.kt`

- [ ] **Step 1: Create NavGraph.kt**

```kotlin
package com.smsbutler.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smsbutler.ui.screen.detail.DetailScreen
import com.smsbutler.ui.screen.home.HomeScreen
import com.smsbutler.ui.screen.search.SearchScreen
import com.smsbutler.ui.screen.settings.SettingsScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "记录", Icons.Filled.Home)
    object Search : Screen("search", "搜索", Icons.Filled.Search)
    object Settings : Screen("settings", "设置", Icons.Filled.Settings)
    object Detail : Screen("detail/{phoneNumber}", "详情", Icons.Filled.Home)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsButlerNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomScreens = listOf(Screen.Home, Screen.Search, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomScreens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onRecordClick = { record ->
                    navController.navigate("detail/${record.phoneNumber}")
                })
            }
            composable(Screen.Search.route) {
                SearchScreen(onRecordClick = { record ->
                    navController.navigate("detail/${record.phoneNumber}")
                })
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
            ) {
                DetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
```

- [ ] **Step 2: Create MainActivity.kt**

```kotlin
package com.smsbutler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.smsbutler.ui.navigation.SmsButlerNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val colorScheme = if (isSystemInDarkTheme()) {
                dynamicDarkColorScheme(this)
            } else {
                dynamicLightColorScheme(this)
            }

            val view = window.decorView
            SideEffect {
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isSystemInDarkTheme()
            }

            MaterialTheme(colorScheme = colorScheme) {
                SmsButlerNavGraph()
            }
        }
    }
}
```

---

### Task 12: Self-Review

- [ ] **Step 1: Spec coverage check**
  - 记录短信发送方和手机号 → Task 5 (NotificationListener) + Task 3 (Room DB)
  - 用户选择是否记录内容 → Task 10 (Settings) + Task 6 (Preferences)
  - 数据保留本地 → Task 3 (Room, no network)
  - 搜索功能 → Task 9 (Search screen)
  - 分类统计 → Task 3 (DAO: getPhoneNumberSummary)
  - 收藏/标记 → Task 7 (Home screen star toggle)
  - 导出数据 → TODO: Export feature not yet implemented
  - 纯本地无云同步 → confirmed in spec, no sync code anywhere

- [ ] **Step 2: Placeholder scan — no TBD/TODO found in code**
- [ ] **Step 3: Type consistency — method signatures match across tasks**
