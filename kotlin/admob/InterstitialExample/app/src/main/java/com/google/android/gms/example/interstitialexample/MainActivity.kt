package com.google.android.gms.example.interstitialexample

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.example.interstitialexample.databinding.ActivityMainBinding

const val GAME_LENGTH_MILLISECONDS = 3000L// 规范为常量，所以添加常量变成了一个非常高频的事情。
const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding
  private var interstitialAd: InterstitialAd? = null
  private var countdownTimer: CountDownTimer? = null
  private var gameIsInProgress = false
  private var adIsLoading: Boolean = false
  private var timerMilliseconds = 0L
  private var TAG = "MainActivity"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)//... databinding标准代码，加入到liveTemplete

    // Log the Mobile Ads SDK version.
    Log.d(TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion())

    // Initialize the Mobile Ads SDK.
    MobileAds.initialize(this) {}

    // Set your test devices. Check your logcat output for the hashed device ID to
    // get test ads on a physical device. e.g.
    // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
    // to get test ads on this device."
    MobileAds.setRequestConfiguration(
      RequestConfiguration.Builder().setTestDeviceIds(listOf("ABCDEF012345")).build()
    )

    // Create the "retry" button, which triggers an interstitial between game plays.
    binding.retryButton.visibility = View.INVISIBLE  //通过这个按钮来触发插页式广告
    binding.retryButton.setOnClickListener { showInterstitial() }// 展示插页式广告，相对的，类似于我这边的触发事件

    // Kick off the first play of the "game."
    startGame()// 第一次跳过
  }

  private fun loadAd() {
    var adRequest = AdRequest.Builder().build()// only things ,build reqeust load

    InterstitialAd.load(
      this,
      AD_UNIT_ID,
      adRequest,
      object : InterstitialAdLoadCallback() {
        override fun onAdFailedToLoad(adError: LoadAdError) {
          Log.d(TAG, adError?.message)
          interstitialAd = null // mark
          adIsLoading = false// mark
          val error =
            "domain: ${adError.domain}, code: ${adError.code}, " + "message: ${adError.message}"
          Toast.makeText(//notification
              this@MainActivity,
              "onAdFailedToLoad() with error $error",
              Toast.LENGTH_SHORT
            )
            .show()
        }

        override fun onAdLoaded(ad: InterstitialAd) {
          Log.d(TAG, "Ad was loaded.")
          interstitialAd = ad //mark
          adIsLoading = false //mark
          Toast.makeText(this@MainActivity, "onAdLoaded()", Toast.LENGTH_SHORT).show() //notification
        }
      }
    )
  }

  // Create the game timer, which counts down to the end of the level
  // and shows the "retry" button.
  private fun createTimer(milliseconds: Long) {
    countdownTimer?.cancel()//创建倒计时，取消可能的倒计时

    countdownTimer =
      object : CountDownTimer(milliseconds, 50) {//50毫秒一次回调
        override fun onTick(millisUntilFinished: Long) {
          timerMilliseconds = millisUntilFinished// 更新剩余时间
          binding.timer.text = "seconds remaining: ${ millisUntilFinished / 1000 + 1 }"
        }

        override fun onFinish() {
          gameIsInProgress = false//代表游戏结束
          binding.timer.text = "done!"
          binding.retryButton.visibility = View.VISIBLE//可以重新打开游戏
        }
      }
  }

  // Show the ad if it's ready. Otherwise toast and restart the game.  //并不明确，这似乎不是一个好的案例
  private fun showInterstitial() {
    if (interstitialAd != null) {
      interstitialAd?.fullScreenContentCallback =
        object : FullScreenContentCallback() {
          override fun onAdDismissedFullScreenContent() {
            Log.d(TAG, "Ad was dismissed.")
            // Don't forget to set the ad reference to null so you
            // don't show the ad a second time.
            interstitialAd = null
            loadAd()
          }

          override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            Log.d(TAG, "Ad failed to show.")
            // Don't forget to set the ad reference to null so you
            // don't show the ad a second time.
            interstitialAd = null
          }

          override fun onAdShowedFullScreenContent() {
            Log.d(TAG, "Ad showed fullscreen content.")
            // Called when ad is dismissed.
          }
        }
      interstitialAd?.show(this)
    } else {
      Toast.makeText(this, "Ad wasn't loaded.", Toast.LENGTH_SHORT).show()
      startGame()
    }
  }

  // Request a new ad if one isn't already loaded, hide the button, and kick off the timer.
  private fun startGame() {//开始游戏，顺带，如果广告没有加载且没有缓存，则标记加载，加载广告
    if (!adIsLoading && interstitialAd == null) {
      adIsLoading = true
      loadAd()
    }

    binding.retryButton.visibility = View.INVISIBLE// 隐藏按钮，不给第二次触发机会
    resumeGame(GAME_LENGTH_MILLISECONDS)// 激活游戏（游戏时长）
  }

  private fun resumeGame(milliseconds: Long) {
    // Create a new timer for the correct length and start it.
    gameIsInProgress = true// 标记游戏进行中---这个代表游戏
    timerMilliseconds = milliseconds// 记录计时器时间
    createTimer(milliseconds) // 创建计时器
    countdownTimer?.start()// 开始倒计时
  }

  // Resume the game if it's in progress.
  public override fun onResume() {
    super.onResume()

    if (gameIsInProgress) {// 如果游戏正在进行，则恢复游戏
      resumeGame(timerMilliseconds)// 使用真实剩余时间，恢复游戏
    }
  }

  // Cancel the timer if the game is paused.
  public override fun onPause() {// 取消倒计时
    countdownTimer?.cancel()
    super.onPause()
  }
}
