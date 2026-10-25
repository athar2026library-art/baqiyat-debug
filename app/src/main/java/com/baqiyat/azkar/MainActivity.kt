package com.baqiyat.azkar

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import com.baqiyat.azkar.data.AiRepository
import com.baqiyat.azkar.data.AppDatabase
import com.baqiyat.azkar.data.AuthRepository
import com.baqiyat.azkar.data.FirestoreRepository
import com.baqiyat.azkar.data.ProgressRepository
import com.baqiyat.azkar.data.SettingsRepository
import com.baqiyat.azkar.data.dataStore
import com.baqiyat.azkar.ui.AppNavGraph
import com.baqiyat.azkar.ui.AppViewModel
import com.baqiyat.azkar.ui.AppViewModelFactory
import com.baqiyat.azkar.ui.components.BaqiyatBackground
import com.baqiyat.azkar.ui.components.BaqiyatButton
import com.baqiyat.azkar.ui.theme.BaqiyatTheme
import com.baqiyat.azkar.ui.theme.rememberTimeMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.baqiyat.azkar.ui.NotificationWorker
import com.baqiyat.azkar.ui.SyncWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Background workers
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "baqiyat_notifications",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS).build()
        )
        SyncWorker.enqueue(applicationContext)

        val progressRepository = ProgressRepository.getInstance(applicationContext)
        val settingsRepository = SettingsRepository(applicationContext.dataStore)
        val aiRepository = AiRepository(applicationContext)
        val authRepository = AuthRepository(this)
        val firestoreRepository = FirestoreRepository()

        // Token registration must never block app launch.
        runCatching {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && !user.isAnonymous) {
                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        FirebaseFirestore.getInstance().collection("users").document(user.uid).set(
                            mapOf(
                                "fcmTokens" to FieldValue.arrayUnion(token),
                                "updatedAt" to FieldValue.serverTimestamp()
                            ),
                            com.google.firebase.firestore.SetOptions.merge()
                        )
                    }
                    .addOnFailureListener { error ->
                        Log.w("BaqiyatStartup", "FCM token registration skipped", error)
                    }
            }
        }.onFailure { error ->
            Log.w("BaqiyatStartup", "Firebase messaging unavailable during startup", error)
        }

        setContent {
            val viewModel: AppViewModel = viewModel(
                factory = AppViewModelFactory(
                    progressRepository,
                    settingsRepository,
                    aiRepository,
                    authRepository,
                    firestoreRepository
                )
            )
            val settings by viewModel.settings.collectAsState()
            val timeMode = rememberTimeMode()
            BaqiyatTheme(darkTheme = settings.darkMode, timeMode = timeMode) {
                BaqiyatBackground {
                    if (!settings.onboardingComplete) {
                        BaqiyatOnboarding(onDone = { viewModel.settingsRepository.setOnboardingComplete() })
                    } else {
                        AppNavGraph(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun BaqiyatOnboarding(onDone: () -> Unit) {
    var page by remember { mutableStateOf(0) }
    val pages = listOf(
        Triple("وردك اليومي بين يديك", "ابدأ بأذكار الصباح والمساء بواجهة هادئة تساعدك على الاستمرار.", com.baqiyat.azkar.R.drawable.logo_baqiyat),
        Triple("اقرأ بتركيز وراحة", "خصّص حجم الخط، فعّل الوضع الليلي، وتابع آخر موضع وصلت إليه.", com.baqiyat.azkar.R.drawable.logo_baqiyat),
        Triple("ابقَ على تواصل", "أرسل اقتراحاتك وشكاواك، وتابع ردود فريق الباقيات وإشعارات التحديثات.", com.baqiyat.azkar.R.drawable.logo_baqiyat)
    )
    val current = pages[page]
    Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(48.dp))
        Image(painterResource(current.third), contentDescription = "شعار الباقيات", modifier = Modifier.size(154.dp))
        Spacer(Modifier.height(30.dp))
        androidx.compose.material3.Text("مرحباً بك في الباقيات", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        androidx.compose.material3.Text(current.first, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        androidx.compose.material3.Text(current.second, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 26.sp)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            pages.indices.forEach { index ->
                Box(Modifier.size(if (index == page) 26.dp else 8.dp, 8.dp).background(if (index == page) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = .35f), RoundedCornerShape(8.dp)))
            }
        }
        Spacer(Modifier.height(24.dp))
        BaqiyatButton(
            text = if (page == pages.lastIndex) "ابدأ الآن" else "التالي",
            onClick = { if (page == pages.lastIndex) onDone() else page++ },
            modifier = Modifier.fillMaxWidth()
        )
        if (page < pages.lastIndex) { TextButton(onClick = onDone) { androidx.compose.material3.Text("تخطي") } } else { Spacer(Modifier.height(48.dp)) }
    }
}
