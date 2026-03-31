package br.recycleapp

import android.app.Application
import br.recycleapp.di.AppModule

/**
 * Application customizada - ponto de entrada do processo.
 * Responsável por liberar o AppModule quando o app encerrar.
 */
class RecycleApplication : Application() {

    override fun onTerminate() {
        super.onTerminate()
        AppModule.clear()   // libera o modelo TFLite ao encerrar
    }
}