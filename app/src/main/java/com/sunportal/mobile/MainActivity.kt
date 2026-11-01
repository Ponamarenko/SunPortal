package com.sunportal.mobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

/**
 * Главная активность приложения SunPortal
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val textView = findViewById<TextView>(R.id.textView)
        val updateButton = findViewById<Button>(R.id.updateButton)
        
        // Відображення версії
        val appVersion = "1.0.0"
        val buildNumber = "1"
        textView.text = "SunPortal v$appVersion\nБілд: $buildNumber\n\n" +
                        "Про додаток\n" +
                        "SunPortal - система моніторингу сонячної енергетики\n\n" +
                        "🌞 Моніторинг сонячної енергії\n" +
                        "🔋 Управління акумулятором\n" +
                        "⚡ Параметри мережі\n" +
                        "📊 Енергетична статистика\n" +
                        "📱 Мобільний контроль"
        
        // Обробка натискання кнопки перевірки оновлень
        updateButton.setOnClickListener {
            checkForUpdates()
        }
    }
    
    private fun checkForUpdates() {
        // TODO: Реалізувати перевірку оновлень через GitHub API
        Toast.makeText(this, "Перевірка оновлень...", Toast.LENGTH_SHORT).show()
    }
}