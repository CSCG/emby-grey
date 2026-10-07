package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.download.MediaDownloadManager
import com.example.data.local.DownloadItemEntity
import com.example.data.local.ServerPreferences
import com.example.data.model.EmbyItemDto
import com.example.data.model.LiveTvChannelDto
import com.example.data.model.PersonDto
import com.example.data.model.PlaybackQuality
import com.example.data.model.ServerConnection
import com.example.data.repository.EmbyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val connection: ServerConnection? = null,
    val libraries: List<EmbyItemDto> = emptyList(),
    val continueWatching: List<EmbyItemDto> = emptyList(),
    val latestItems: List<EmbyItemDto> = emptyList(),
    val collections: List<EmbyItemDto> = emptyList(),
    val errorMessage: String? = null
)

class EmbyViewModel(
    private val repository: EmbyRepository,
    private val downloadManager: MediaDownloadManager,
    private val preferences: ServerPreferences
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _selectedItem = MutableStateFlow<EmbyItemDto?>(null)
    val selectedItem: StateFlow<EmbyItemDto?> = _selectedItem.asStateFlow()

    private val _episodes = MutableStateFlow<List<EmbyItemDto>>(emptyList())
    val episodes: StateFlow<List<EmbyItemDto>> = _episodes.asStateFlow()

    private val _libraryItems = MutableStateFlow<List<EmbyItemDto>>(emptyList())
    val libraryItems: StateFlow<List<EmbyItemDto>> = _libraryItems.asStateFlow()

    private val _isLibraryLoading = MutableStateFlow(false)
    val isLibraryLoading: StateFlow<Boolean> = _isLibraryLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSearchFilter = MutableStateFlow("All") // All, Movie, Series
    val selectedSearchFilter: StateFlow<String> = _selectedSearchFilter.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchResults = MutableStateFlow<List<EmbyItemDto>>(emptyList())
    val searchResults: StateFlow<List<EmbyItemDto>> = _searchResults.asStateFlow()

    private val _similarItems = MutableStateFlow<List<EmbyItemDto>>(emptyList())
    val similarItems: StateFlow<List<EmbyItemDto>> = _similarItems.asStateFlow()

    private val _collectionItems = MutableStateFlow<List<EmbyItemDto>>(emptyList())
    val collectionItems: StateFlow<List<EmbyItemDto>> = _collectionItems.asStateFlow()

    private val _selectedPerson = MutableStateFlow<PersonDto?>(null)
    val selectedPerson: StateFlow<PersonDto?> = _selectedPerson.asStateFlow()

    private val _personItems = MutableStateFlow<List<EmbyItemDto>>(emptyList())
    val personItems: StateFlow<List<EmbyItemDto>> = _personItems.asStateFlow()

    private val _liveTvChannels = MutableStateFlow<List<LiveTvChannelDto>>(emptyList())
    val liveTvChannels: StateFlow<List<LiveTvChannelDto>> = _liveTvChannels.asStateFlow()

    private val _selectedLiveTvCategory = MutableStateFlow("All")
    val selectedLiveTvCategory: StateFlow<String> = _selectedLiveTvCategory.asStateFlow()

    private val _isLiveTvLoading = MutableStateFlow(false)
    val isLiveTvLoading: StateFlow<Boolean> = _isLiveTvLoading.asStateFlow()

    private var searchJob: Job? = null

    val downloads: StateFlow<List<DownloadItemEntity>> = downloadManager.downloadsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val serverPreferences = preferences

    init {
        val conn = preferences.getConnection()
        if (conn != null) {
            _homeState.value = _homeState.value.copy(connection = conn)
            loadHomeData()
        }
        loadLiveTvChannels()
    }

    fun loadHomeData() {
        loadLiveTvChannels()
        viewModelScope.launch {
            _homeState.value = _homeState.value.copy(isLoading = true, errorMessage = null)
            try {
                val views = repository.getViews()
                val resume = repository.getResumeItems()
                val latest = repository.getLatestItems()
                val collections = repository.getCollections()
                _homeState.value = _homeState.value.copy(
                    isLoading = false,
                    connection = preferences.getConnection(),
                    libraries = views,
                    continueWatching = resume,
                    latestItems = latest,
                    collections = collections
                )
            } catch (e: Exception) {
                _homeState.value = _homeState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to load media"
                )
            }
        }
    }

    fun connectServer(url: String, user: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _homeState.value = _homeState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.authenticate(url, user, pass)
            result.fold(
                onSuccess = { conn ->
                    _homeState.value = _homeState.value.copy(
                        isLoading = false,
                        connection = conn,
                        errorMessage = null
                    )
                    loadHomeData()
                    onSuccess()
                },
                onFailure = { err ->
                    _homeState.value = _homeState.value.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Connection error"
                    )
                }
            )
        }
    }

    fun connectDemoServer(onSuccess: () -> Unit) {
        val conn = repository.connectDemo()
        _homeState.value = _homeState.value.copy(
            connection = conn,
            errorMessage = null
        )
        loadHomeData()
        onSuccess()
    }

    fun disconnectServer() {
        preferences.clearConnection()
        _homeState.value = HomeUiState(connection = null)
    }

    fun selectItem(itemId: String) {
        viewModelScope.launch {
            val item = repository.getItemDetails(itemId)
            _selectedItem.value = item
            val isSeries = item?.type.equals("Series", ignoreCase = true) || item?.type.equals("Season", ignoreCase = true)
            val isCollection = item?.type.equals("BoxSet", ignoreCase = true) ||
                    item?.type.equals("CollectionFolder", ignoreCase = true) ||
                    item?.type.equals("Playlist", ignoreCase = true) ||
                    item?.type.equals("Folder", ignoreCase = true) ||
                    item?.collectionType != null ||
                    item?.type?.contains("Collection", ignoreCase = true) == true ||
                    itemId.startsWith("boxset_")

            when {
                isSeries -> {
                    _episodes.value = repository.getEpisodes(seriesId = itemId)
                    _collectionItems.value = emptyList()
                }
                isCollection -> {
                    _collectionItems.value = repository.getCollectionItems(collectionId = itemId)
                    _episodes.value = emptyList()
                }
                item?.type.equals("Episode", ignoreCase = true) && !item?.seriesId.isNullOrBlank() -> {
                    _episodes.value = repository.getEpisodes(seriesId = item?.seriesId ?: "")
                    _collectionItems.value = emptyList()
                }
                else -> {
                    _episodes.value = emptyList()
                    _collectionItems.value = emptyList()
                }
            }
            _similarItems.value = repository.getSimilarItems(itemId)
        }
    }

    fun selectPerson(person: PersonDto) {
        _selectedPerson.value = person
        viewModelScope.launch {
            if (!person.id.isNullOrBlank()) {
                _personItems.value = repository.getItemsByPerson(person.id)
            } else {
                _personItems.value = emptyList()
            }
        }
    }

    fun clearSelectedPerson() {
        _selectedPerson.value = null
        _personItems.value = emptyList()
    }

    fun getPersonImageUrl(person: PersonDto): String? {
        return repository.getPersonImageUrl(person)
    }

    fun loadLiveTvChannels() {
        viewModelScope.launch {
            _isLiveTvLoading.value = true
            val channels = repository.getLiveTvChannels()
            _liveTvChannels.value = channels
            _isLiveTvLoading.value = false
        }
    }

    fun refreshLiveTv() {
        loadLiveTvChannels()
    }

    fun filterLiveTv(category: String) {
        _selectedLiveTvCategory.value = category
    }

    fun getChannelLogoUrl(channel: LiveTvChannelDto): String? {
        return repository.getChannelLogoUrl(channel)
    }

    fun buildLiveTvItemDto(channel: LiveTvChannelDto): EmbyItemDto {
        return EmbyItemDto(
            id = channel.id,
            name = channel.name,
            type = "LiveTvChannel",
            path = channel.snapshotUrl ?: channel.streamUrl,
            overview = channel.currentProgram?.overview ?: channel.streamUrl,
            genres = channel.currentProgram?.genres,
            collectionType = if (channel.isTrafficCam) "TrafficCam" else if (channel.isOnlineFast) "FreeFast" else "EmbyTuner",
            seriesName = channel.currentProgram?.name ?: channel.category,
            officialRating = channel.location ?: if (channel.isTrafficCam) "DOT Live Cam" else null
        )
    }

    fun loadLibrary(parentId: String?, type: String? = null) {
        viewModelScope.launch {
            _isLibraryLoading.value = true
            val items = repository.getItems(
                parentId = parentId,
                includeItemTypes = type
            )
            _libraryItems.value = items
            _isLibraryLoading.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        triggerSearch(query, _selectedSearchFilter.value)
    }

    fun setSearchFilter(filter: String) {
        _selectedSearchFilter.value = filter
        triggerSearch(_searchQuery.value, filter)
    }

    private fun triggerSearch(query: String, filter: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(300) // Debounce rapid typing
            val filterParam = if (filter == "All") null else filter
            val results = repository.searchMedia(query, filterParam)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun downloadItem(item: EmbyItemDto) {
        downloadManager.startDownload(item)
    }

    fun deleteDownload(itemId: String) {
        downloadManager.cancelOrDeleteDownload(itemId)
    }

    fun getUsedStorageBytes(): Long {
        return downloadManager.getUsedStorageBytes()
    }

    fun setHwAcceleration(enabled: Boolean) {
        preferences.hardwareAcceleration = enabled
    }

    fun setDefaultQuality(quality: PlaybackQuality) {
        preferences.defaultQuality = quality
    }

    fun setPreferredSubtitleLang(lang: String) {
        preferences.preferredSubtitleLanguage = lang
    }

    fun getImageUrl(item: EmbyItemDto, isBackdrop: Boolean = false): String {
        return repository.getImageUrl(item, isBackdrop)
    }

    fun getImageUrl(itemId: String, isBackdrop: Boolean = false): String {
        return repository.getImageUrl(itemId, isBackdrop)
    }

    suspend fun getResumePosition(item: EmbyItemDto): Long? {
        return repository.getResumePosition(item.id, item)
    }

    fun markItemPlayed(itemId: String, played: Boolean) {
        viewModelScope.launch {
            repository.markItemPlayed(itemId, played)
            // Refresh item details and home data
            selectItem(itemId)
            loadHomeData()
        }
    }

    fun clearResumePosition(itemId: String) {
        viewModelScope.launch {
            repository.clearResumePosition(itemId)
            selectItem(itemId)
            loadHomeData()
        }
    }

    // Compatibility aliases for UI screens
    fun connectToServer(url: String, user: String, pass: String, onSuccess: () -> Unit) = connectServer(url, user, pass, onSuccess)
    fun loadDemoMode(onSuccess: () -> Unit) = connectDemoServer(onSuccess)
    fun disconnect() = disconnectServer()
    fun updateSearchQuery(query: String) = onSearchQueryChanged(query)
    fun updateDefaultQuality(quality: PlaybackQuality) = setDefaultQuality(quality)
    fun setHardwareAcceleration(enabled: Boolean) = setHwAcceleration(enabled)
    fun updatePreferredSubtitle(lang: String) = setPreferredSubtitleLang(lang)
    fun cancelDownload(itemId: String) = deleteDownload(itemId)
}

class EmbyViewModelFactory(
    private val repository: EmbyRepository,
    private val downloadManager: MediaDownloadManager,
    private val preferences: ServerPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EmbyViewModel(repository, downloadManager, preferences) as T
    }
}
