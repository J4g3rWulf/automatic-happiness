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

/**
 * ViewModel responsável pela classificação de imagens de resíduos.
 *
 * Gerencia o estado da UI e coordena a classificação através do [ClassifierRepository].
 * Libera recursos do classificador adequadamente quando o ViewModel é destruído.
 */
class ClassificationViewModel(app: Application) : AndroidViewModel(app) {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Result(val result: ClassificationResult) : UiState()
    }

    private val repository = ClassifierRepository(app)

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var _imageUri: Uri = Uri.EMPTY
    val imageUri: Uri get() = _imageUri

    /**
     * Inicia a classificação de uma imagem.
     *
     * @param imageUri URI da imagem capturada ou selecionada
     */
    fun classify(imageUri: Uri) {
        _imageUri = imageUri
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.classify(imageUri)
            _uiState.value = UiState.Result(result)
        }
    }

    /**
     * Reseta o estado do ViewModel para Idle.
     * Útil ao voltar para a tela inicial.
     */
    fun reset() {
        _uiState.value = UiState.Idle
        _imageUri = Uri.EMPTY
    }

    override fun onCleared() {
        super.onCleared()
        repository.close()
    }
}