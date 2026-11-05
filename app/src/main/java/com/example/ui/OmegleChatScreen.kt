package com.example.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.R

private const val OMEGLE_URL = "https://omegle.chiwoko2006.workers.dev/"

private val BrandCoral = Color(0xFFFF6B35)
private val DarkSurface = Color(0xFF12131A)
private val DarkCard = Color(0xFF1C1E28)

@Composable
fun OmegleChatScreen() {
  val context = LocalContext.current

  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
    )
  }
  var hasAudioPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED
    )
  }
  var continueTextOnly by remember { mutableStateOf(false) }
  var permissionRequestedOnce by remember { mutableStateOf(false) }

  val permissionLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
      hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
      hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] == true
      permissionRequestedOnce = true
    }

  val allMediaGranted = hasCameraPermission && hasAudioPermission

  if (!allMediaGranted && !continueTextOnly) {
    PermissionGateContent(
      permissionRequestedOnce = permissionRequestedOnce,
      onGrantClick = {
        permissionLauncher.launch(
          arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )
      },
      onContinueTextOnly = { continueTextOnly = true },
      onOpenSettings = {
        val intent =
          Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
          }
        context.startActivity(intent)
      },
    )
  } else {
    OmegleWebViewContent(
      hasCameraPermission = hasCameraPermission,
      hasAudioPermission = hasAudioPermission,
      onRequestMediaPermissions = {
        permissionLauncher.launch(
          arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )
      },
    )
  }
}

@Composable
private fun PermissionGateContent(
  permissionRequestedOnce: Boolean,
  onGrantClick: () -> Unit,
  onContinueTextOnly: () -> Unit,
  onOpenSettings: () -> Unit,
) {
  Scaffold(
    containerColor = DarkSurface,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
  ) { innerPadding ->
    Box(
      modifier =
        Modifier.fillMaxSize()
          .padding(innerPadding)
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(horizontal = 24.dp),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Image(
          painter = painterResource(id = R.drawable.ic_omegle_foreground_1793876647213),
          contentDescription = stringResource(R.string.app_name),
          modifier = Modifier.size(96.dp).clip(RoundedCornerShape(24.dp)),
          contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
          text = stringResource(R.string.permission_title),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = stringResource(R.string.permission_subtitle),
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFFB4B8C8),
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
          color = DarkCard,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            PermissionFeatureRow(
              icon = Icons.Rounded.Videocam,
              title = "Live Video",
              description = "Face-to-face video chat with strangers",
            )
            PermissionFeatureRow(
              icon = Icons.Rounded.Mic,
              title = "Crystal-Clear Audio",
              description = "Real-time voice conversations",
            )
            PermissionFeatureRow(
              icon = Icons.Rounded.ChatBubbleOutline,
              title = "Instant Text",
              description = "Send messages anytime during or without video",
            )
          }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
          onClick = onGrantClick,
          colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
          shape = RoundedCornerShape(14.dp),
          modifier =
            Modifier.fillMaxWidth()
              .height(52.dp)
              .minimumInteractiveComponentSize()
              .testTag("grant_permissions_button"),
        ) {
          Text(
            text = stringResource(R.string.grant_permissions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
          onClick = onContinueTextOnly,
          shape = RoundedCornerShape(14.dp),
          modifier =
            Modifier.fillMaxWidth()
              .height(48.dp)
              .minimumInteractiveComponentSize()
              .testTag("text_only_button"),
        ) {
          Text(
            text = stringResource(R.string.continue_text_only),
            color = Color(0xFFD0D4E4),
          )
        }

        if (permissionRequestedOnce) {
          Spacer(modifier = Modifier.height(8.dp))
          TextButton(
            onClick = onOpenSettings,
            modifier = Modifier.minimumInteractiveComponentSize().testTag("open_settings_button"),
          ) {
            Icon(
              imageVector = Icons.Rounded.Settings,
              contentDescription = null,
              tint = BrandCoral,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = stringResource(R.string.open_settings),
              color = BrandCoral,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PermissionFeatureRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  description: String,
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier =
        Modifier.size(40.dp)
          .clip(CircleShape)
          .background(BrandCoral.copy(alpha = 0.15f)),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = BrandCoral,
        modifier = Modifier.size(20.dp),
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
      )
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF9EA3B5),
      )
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun OmegleWebViewContent(
  hasCameraPermission: Boolean,
  hasAudioPermission: Boolean,
  onRequestMediaPermissions: () -> Unit,
) {
  val context = LocalContext.current
  var loadingProgress by remember { mutableIntStateOf(0) }
  var canGoBack by remember { mutableStateOf(false) }
  var hasError by remember { mutableStateOf(false) }
  var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

  val filePickerLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
      filePathCallback?.onReceiveValue(if (uri != null) arrayOf(uri) else emptyArray())
      filePathCallback = null
    }

  val webView = remember {
    WebView(context).apply {
      layoutParams =
        ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT,
        )
      settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        mediaPlaybackRequiresUserGesture = false
        allowFileAccess = true
        allowContentAccess = true
        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        useWideViewPort = true
        loadWithOverviewMode = true
      }
      keepScreenOn = true
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      webView.stopLoading()
      webView.destroy()
    }
  }

  BackHandler(enabled = canGoBack) {
    if (webView.canGoBack()) {
      webView.goBack()
    }
  }

  Scaffold(
    containerColor = DarkSurface,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      Surface(
        color = DarkSurface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
      ) {
        Column {
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Image(
                painter = painterResource(id = R.drawable.ic_omegle_foreground_1793876647213),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              val mediaReady = hasCameraPermission && hasAudioPermission
              Surface(
                color =
                  if (mediaReady) Color(0xFF1E3A2F) else BrandCoral.copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp),
                modifier =
                  Modifier.clip(RoundedCornerShape(20.dp))
                    .clickable(enabled = !mediaReady) { onRequestMediaPermissions() }
                    .testTag("media_status_pill"),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(
                    imageVector =
                      if (mediaReady) Icons.Rounded.Videocam
                      else Icons.Rounded.ChatBubbleOutline,
                    contentDescription = null,
                    tint = if (mediaReady) Color(0xFF4ADE80) else BrandCoral,
                    modifier = Modifier.size(14.dp),
                  )
                  Spacer(modifier = Modifier.width(5.dp))
                  Text(
                    text =
                      if (mediaReady) stringResource(R.string.camera_mic_active)
                      else stringResource(R.string.text_only_mode),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (mediaReady) Color(0xFF4ADE80) else BrandCoral,
                    fontWeight = FontWeight.Medium,
                  )
                }
              }

              IconButton(
                onClick = {
                  hasError = false
                  webView.reload()
                },
                modifier = Modifier.minimumInteractiveComponentSize().testTag("reload_button"),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Refresh,
                  contentDescription = stringResource(R.string.reload_page),
                  tint = Color.White,
                )
              }
            }
          }

          if (loadingProgress in 1..99) {
            LinearProgressIndicator(
              progress = { loadingProgress / 100f },
              color = BrandCoral,
              trackColor = DarkCard,
              modifier = Modifier.fillMaxWidth().height(2.dp),
            )
          }
        }
      }
    },
  ) { innerPadding ->
    Box(
      modifier =
        Modifier.fillMaxSize()
          .padding(innerPadding)
          .navigationBarsPadding()
          .imePadding()
    ) {
      AndroidView(
        factory = {
          webView.apply {
            webViewClient =
              object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                  hasError = false
                  canGoBack = view?.canGoBack() == true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                  canGoBack = view?.canGoBack() == true
                }

                override fun onReceivedError(
                  view: WebView?,
                  request: WebResourceRequest?,
                  error: WebResourceError?,
                ) {
                  if (request?.isForMainFrame == true) {
                    hasError = true
                  }
                }

                override fun shouldOverrideUrlLoading(
                  view: WebView?,
                  request: WebResourceRequest?,
                ): Boolean {
                  val url = request?.url?.toString() ?: return false
                  return if (url.startsWith("https://omegle.chiwoko2006.workers.dev")) {
                    false
                  } else {
                    runCatching {
                      context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                    true
                  }
                }
              }

            webChromeClient =
              object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                  loadingProgress = newProgress
                }

                override fun onPermissionRequest(request: PermissionRequest?) {
                  request ?: return
                  val grantedResources = mutableListOf<String>()
                  for (resource in request.resources) {
                    when (resource) {
                      PermissionRequest.RESOURCE_VIDEO_CAPTURE -> {
                        if (
                          ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA,
                          ) == PackageManager.PERMISSION_GRANTED
                        ) {
                          grantedResources.add(resource)
                        }
                      }
                      PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {
                        if (
                          ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO,
                          ) == PackageManager.PERMISSION_GRANTED
                        ) {
                          grantedResources.add(resource)
                        }
                      }
                    }
                  }
                  if (grantedResources.isNotEmpty()) {
                    request.grant(grantedResources.toTypedArray())
                  } else {
                    request.deny()
                  }
                }

                override fun onShowFileChooser(
                  webView: WebView?,
                  callback: ValueCallback<Array<Uri>>?,
                  fileChooserParams: FileChooserParams?,
                ): Boolean {
                  filePathCallback?.onReceiveValue(null)
                  filePathCallback = callback
                  filePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                  )
                  return true
                }
              }

            loadUrl(OMEGLE_URL)
          }
        },
        modifier = Modifier.fillMaxSize().testTag("omegle_webview"),
      )

      AnimatedVisibility(
        visible = hasError,
        enter = fadeIn(),
        exit = fadeOut(),
      ) {
        Box(
          modifier = Modifier.fillMaxSize().background(DarkSurface).padding(24.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 360.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.WifiOff,
              contentDescription = null,
              tint = BrandCoral,
              modifier = Modifier.size(56.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = stringResource(R.string.connection_error_title),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color.White,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = stringResource(R.string.connection_error_subtitle),
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFF9EA3B5),
              textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
              onClick = {
                hasError = false
                webView.loadUrl(OMEGLE_URL)
              },
              colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
              shape = RoundedCornerShape(12.dp),
              modifier =
                Modifier.height(48.dp)
                  .minimumInteractiveComponentSize()
                  .testTag("retry_button"),
            ) {
              Text(
                text = stringResource(R.string.retry),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }
      }
    }
  }
}
