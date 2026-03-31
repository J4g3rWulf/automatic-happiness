package br.recycleapp.di

import android.content.Context
import br.recycleapp.data.repository.ClassifierRepository
import br.recycleapp.domain.repository.ITrashClassifier
import br.recycleapp.domain.usecase.ClassifyImageUseCase

/**
 * Service Locator - ponto central de criação e compartilhamento de dependências.
 *
 * Garante instância única do repositório para todo o ciclo de vida do app,
 * o que é especialmente importante para o classificador TFLite - evita
 * recarregar o modelo a cada nova classificação.
 *
 * Para projetos maiores, considerar migração para Hilt ou Koin.
 */
object AppModule {

    @Volatile
    private var repository: ITrashClassifier? = null

    /**
     * Retorna a instância única do repositório.
     * Thread-safe via double-checked locking.
     */
    fun provideClassifierRepository(context: Context): ITrashClassifier {
        return repository ?: synchronized(this) {
            repository ?: ClassifierRepository(
                context.applicationContext  // applicationContext evita memory leak
            ).also { repository = it }
        }
    }

    /**
     * Retorna um UseCase conectado ao repositório singleton.
     */
    fun provideClassifyImageUseCase(context: Context): ClassifyImageUseCase {
        return ClassifyImageUseCase(
            provideClassifierRepository(context)
        )
    }

    /**
     * Libera o repositório e o modelo TFLite.
     * Chamar no onTerminate() da Application ou quando o app encerrar.
     */
    fun clear() {
        repository?.close()
        repository = null
    }
}