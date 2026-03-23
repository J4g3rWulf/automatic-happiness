package br.recycleapp.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.recycleapp.data.model.ClassificationResult
import br.recycleapp.data.repository.ClassifierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClassificationViewModel(app: Application) : AndroidViewModel(app) {

    sealed class UiState {
        object Idle    : UiState()
        object Loading : UiState()
        data class Result(val result: ClassificationResult) : UiState()
    }

    private val repository = ClassifierRepository(app)

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Guarda a URI para que a ResultScreen possa limpar o arquivo temporário
    private var _imageUri: Uri = Uri.EMPTY
    val imageUri: Uri get() = _imageUri

    fun classify(imageUri: Uri) {
        _imageUri = imageUri
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.classify(imageUri)
            _uiState.value = UiState.Result(result)
        }
    }

    fun reset() {
        _uiState.value = UiState.Idle
        _imageUri = Uri.EMPTY
    }
}