package com.example.mobile_social_media.data.viewModels
import com.example.mobile_social_media.data.model.Usuario
import com.example.mobile_social_media.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class MainViewModel(
    private val usuarioRepository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    fun carregarUsuarioAtual() {
        val usuarioLocal = usuarioRepository.getUsuarioAtual()
        if (usuarioLocal != null) {
            viewModelScope.launch {
                val usuarioNoBanco = usuarioRepository.buscarUsuario(usuarioLocal.uid)
                if (usuarioNoBanco == null) {
                    usuarioRepository.salvarUsuario(usuarioLocal)
                }
                _usuario.value = usuarioLocal
            }
        }
    }
}
