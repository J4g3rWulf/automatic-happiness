package br.recycleapp.ui.screens

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Gerencia o estado da animação inicial da HomeScreen.
 *
 * Usa [AtomicBoolean] para garantir thread-safety, pois pode ser acessado
 * de diferentes threads durante a composição e recomposição.
 *
 * **Nota:** Esta é uma solução pragmática para controlar animação única.
 * Para apps maiores, considere gerenciar este estado em um ViewModel ou
 * usar SavedStateHandle para persistir entre process death.
 */
object HomeAnimationState {
    private val _hasAnimated = AtomicBoolean(false)

    /**
     * Verifica se a animação inicial já foi exibida.
     */
    val hasAnimated: Boolean
        get() = _hasAnimated.get()

    /**
     * Marca que a animação inicial foi exibida.
     * Retorna true se esta foi a primeira vez (transição false -> true).
     */
    fun markAsAnimated(): Boolean {
        return _hasAnimated.compareAndSet(false, true)
    }

    /**
     * Reseta o estado da animação (útil para testes ou debug).
     */
    @Suppress("unused")
    fun reset() {
        _hasAnimated.set(false)
    }
}