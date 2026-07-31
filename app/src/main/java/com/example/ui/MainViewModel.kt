package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.CrrsaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = CrrsaRepository(application)

    // Reactive StateFlows
    val chatMessages: StateFlow<List<ChatMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val faqs: StateFlow<List<FaqItem>> = repository.allFaqs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val serviceMenu: StateFlow<List<ServiceMenuItem>> = repository.allServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<List<AppSetting>> = repository.allSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state states
    private val _currentLanguage = MutableStateFlow("EN")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _isVoiceListening = MutableStateFlow(false)
    val isVoiceListening: StateFlow<Boolean> = _isVoiceListening.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFaqCategory = MutableStateFlow("All")
    val selectedFaqCategory: StateFlow<String> = _selectedFaqCategory.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        viewModelScope.launch {
            val lang = repository.getSetting("app_language") ?: "EN"
            _currentLanguage.value = lang

            val dark = repository.getSetting("dark_mode")?.toBoolean() ?: false
            _isDarkMode.value = dark

            // Ensure upload directory with 755 permissions
            repository.ensureUploadsDirectory()
        }
    }

    // Chat actions
    fun sendMessage(text: String, attachmentPath: String? = null, attachmentType: String? = null) {
        if (text.isBlank() && attachmentPath == null) return
        viewModelScope.launch {
            _isAiThinking.value = true
            repository.sendMessage(text, attachmentPath, attachmentType)
            _isAiThinking.value = false
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
            showSnackbar("Chat history cleared")
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }

    // Voice Input simulation
    fun toggleVoiceListening() {
        _isVoiceListening.value = !_isVoiceListening.value
        if (!_isVoiceListening.value) {
            // Simulated voice speech-to-text input result
            sendMessage("What are the requirements and fee for a New Residency ID card?")
        }
    }

    // Language Toggle
    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
        viewModelScope.launch {
            repository.updateSetting("app_language", lang)
            showSnackbar("Language changed to $lang")
        }
    }

    // Dark Mode Toggle
    fun toggleDarkMode() {
        val newMode = !_isDarkMode.value
        _isDarkMode.value = newMode
        viewModelScope.launch {
            repository.updateSetting("dark_mode", newMode.toString())
        }
    }

    // Search and Filters
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFaqCategory(category: String) {
        _selectedFaqCategory.value = category
    }

    // Admin Authentication
    fun loginAdmin(pass: String): Boolean {
        return if (pass == "admin123" || pass == "crrsa2026") {
            _isAdminLoggedIn.value = true
            showSnackbar("Welcome Admin! Authenticated successfully.")
            true
        } else {
            showSnackbar("Invalid password. Default is 'admin123'")
            false
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        showSnackbar("Admin logged out")
    }

    // Admin FAQ operations
    fun addFaq(category: String, qEn: String, qAm: String, aEn: String, aAm: String) {
        viewModelScope.launch {
            repository.addFaq(
                FaqItem(
                    category = category,
                    questionEn = qEn,
                    questionAm = qAm,
                    answerEn = aEn,
                    answerAm = aAm,
                    isPopular = true
                )
            )
            showSnackbar("New FAQ added successfully")
        }
    }

    fun deleteFaq(id: Long) {
        viewModelScope.launch {
            repository.deleteFaq(id)
            showSnackbar("FAQ removed")
        }
    }

    // Admin Service operations
    fun addService(titleEn: String, titleAm: String, descEn: String, descAm: String, docsEn: String, docsAm: String, fee: Double, days: Int) {
        viewModelScope.launch {
            repository.addService(
                ServiceMenuItem(
                    serviceCode = "SVC_" + System.currentTimeMillis() % 1000,
                    category = "Civil Registration",
                    titleEn = titleEn,
                    titleAm = titleAm,
                    descriptionEn = descEn,
                    descriptionAm = descAm,
                    requiredDocumentsEn = docsEn,
                    requiredDocumentsAm = docsAm,
                    feeEtb = fee,
                    processingTimeDays = days
                )
            )
            showSnackbar("Service item created")
        }
    }

    fun deleteService(id: Long) {
        viewModelScope.launch {
            repository.deleteService(id)
            showSnackbar("Service removed")
        }
    }

    // Directory 755 Status Inspector
    fun getUploadDirPermissionStatus(): String {
        return repository.getUploadDirStatus()
    }

    fun showSnackbar(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
