package com.example.monedicto

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class BloqueoAppsService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val prefs = getSharedPreferences("MONEDICTO_BLOQUEOS", MODE_PRIVATE)
        val bloqueoActivo = prefs.getBoolean("BLOQUEO_APPS_ACTIVO", false)

        if (!bloqueoActivo || event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val paqueteActual = event.packageName?.toString() ?: return

            // Leer la lista personalizada elegida por el usuario
            val appsBloqueadas = prefs.getStringSet("PAQUETES_BLOQUEADOS", emptySet()) ?: emptySet()

            if (appsBloqueadas.contains(paqueteActual)) {
                Toast.makeText(this, "Monedicto: Acceso bloqueado por tu bienestar", Toast.LENGTH_LONG).show()
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }
    }

    override fun onInterrupt() {}
}