package br.recycleapp

import android.app.Application
import br.recycleapp.di.AppModule
import com.google.firebase.FirebaseApp

/**
 * Application customizada - ponto de entrada do processo.
 *
 * Responsabilidades:
 * - Inicializa o Firebase antes de qualquer outra dependência
 * - Libera o AppModule (e o modelo TFLite) quando o app encerrar
 */
class RecycleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        AppModule.clear()
    }
}