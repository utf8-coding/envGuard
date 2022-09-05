package com.utf8coding.envGuardAdmin.activities

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.utf8coding.envGuardAdmin.R
import com.utf8coding.envGuardAdmin.network.NetWorkResponse
import com.utf8coding.envGuardAdmin.viewModels.LoginActivityViewModel
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.setEventListener
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class LoginActivity : BaseActivity() {
    companion object{
        const val DEFAULT_KEY_NAME = "default_key"
    }

    private lateinit var viewModel: LoginActivityViewModel
    private lateinit var userNameTextField: TextInputLayout
    private lateinit var userNameTextEditText: TextInputEditText
    private lateinit var passwordTextField: TextInputLayout
    private lateinit var passwordTextEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var loadingIndicator: LinearProgressIndicator
    private lateinit var loginView: CoordinatorLayout
    private lateinit var logoImageView: ImageView
    private lateinit var rememberCheckBox: CheckBox
    private lateinit var rememberTextView: TextView
    private lateinit var bioCheckCheckBox: CheckBox
    private lateinit var bioCheckTextView: TextView
    private lateinit var manager: FingerprintManager
    private lateinit var fingerPrintMask: ConstraintLayout
    private lateinit var fingerPrintHintImage: ImageView
    private val mCancellationSignal = CancellationSignal()
    private var mCipher: Cipher? = null
    private var keyStore: KeyStore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        viewModel = ViewModelProvider(this)[LoginActivityViewModel::class.java]

        initAccess()

        initKey()

        initViews()

        testConnection()

    }


    private fun initAccess(){
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            //未赋予权限，申请权限
            if (
                ActivityCompat
                    .shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
            ) {
                //选择不开启权限
                Toast.makeText(this, "允许本权限则方可使用地图功能", Toast.LENGTH_SHORT).show()
            }
            //申请权限
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), 1)

        }
    }

    private fun initKey(){
        //manager
        manager = getSystemService(FINGERPRINT_SERVICE) as FingerprintManager
        if (!(manager.hasEnrolledFingerprints() && manager.isHardwareDetected)) {
            bioCheckTextView.visibility = GONE
            bioCheckCheckBox.visibility = GONE
        }

        //keystore
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore!!.load(null)
            val keyGenerator: KeyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val builder = KeyGenParameterSpec.Builder(
                DEFAULT_KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or
                        KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        //mCipher
        try {
            val key: SecretKey = keyStore!!.getKey(DEFAULT_KEY_NAME, null) as SecretKey
            mCipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7
            )
            mCipher!!.init(Cipher.ENCRYPT_MODE, key)
        } catch (e: java.lang.Exception) {
            throw java.lang.RuntimeException(e)
        }

    }

    private fun initViews(){
        userNameTextField = findViewById(R.id.userNameTextField)
        userNameTextEditText = findViewById(R.id.userNameEditText)
        passwordTextField = findViewById(R.id.passwordTextField)
        passwordTextEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        loginView = findViewById(R.id.loginView)
        logoImageView = findViewById(R.id.imageView)
        rememberCheckBox = findViewById(R.id.rememberCheckBox)
        rememberTextView = findViewById(R.id.rememberText)
        bioCheckCheckBox = findViewById(R.id.bioCheckCheckBox)
        bioCheckTextView = findViewById(R.id.bioCheckText)
        fingerPrintMask = findViewById(R.id.fingerPrintMask)
        fingerPrintHintImage = findViewById(R.id.fingerPrintHintImage)

        //键盘监听
        setEventListener(this)  {
            if (!it) {
                userNameTextEditText.clearFocus()
                passwordTextEditText.clearFocus()
            }
        }
        //空不让登录
        userNameTextEditText.setOnEditorActionListener { v, _, _ ->
            if (v.text.toString() == "") {
                loginButton.isClickable = false
            } else if (!loginButton.isClickable){
                loginButton.isClickable = true
            }
            true
        }
        passwordTextEditText.setOnEditorActionListener { v, _, _ ->
            if (v.text.toString() == "") {
                loginButton.isClickable = false
            } else if (!loginButton.isClickable){
                loginButton.isClickable = true
            }
            true
        }

        //按下登录
        loginButton.setOnClickListener {
            onLogin()
        }

        //是否记住
        rememberTextView.setOnClickListener{
            rememberCheckBox.isChecked = !rememberCheckBox.isChecked
        }
        bioCheckTextView.setOnClickListener{
            bioCheckCheckBox.isChecked = !bioCheckCheckBox.isChecked
        }

        // bio/记住 状态
        if (rememberCheckBox.isChecked){
            bioCheckCheckBox.isClickable = true
            bioCheckTextView.isClickable = true
            bioCheckCheckBox.alpha = 1f
            bioCheckTextView.alpha = 1f
        } else {
            bioCheckTextView.isClickable = false
            bioCheckCheckBox.isClickable = false
            bioCheckCheckBox.isChecked = false
            bioCheckCheckBox.alpha = 0.5f
            bioCheckTextView.alpha = 0.5f
        }
        rememberCheckBox.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked){
                bioCheckCheckBox.isClickable = true
                bioCheckTextView.isClickable = true
                bioCheckCheckBox.alpha = 1f
                bioCheckTextView.alpha = 1f
            } else {
                bioCheckTextView.isClickable = false
                bioCheckCheckBox.isClickable = false
                bioCheckCheckBox.isChecked = false
                bioCheckCheckBox.alpha = 0.5f
                bioCheckTextView.alpha = 0.5f
            }
        }

        //进行生物登录
        val namePasswordInfo = viewModel.getLoginInfo()
        if (namePasswordInfo[0] != "" && namePasswordInfo[1] != "" && namePasswordInfo[2] == "false"){
            userNameTextEditText.setText(namePasswordInfo[0])
            passwordTextEditText.setText(namePasswordInfo[1])
            rememberCheckBox.isChecked = true
        } else if (namePasswordInfo[0] != "" && namePasswordInfo[1] != "" && namePasswordInfo[2] == "true"){
            bioCheckCheckBox.isChecked = true
            rememberCheckBox.isChecked = true
            fingerPrintMask.visibility = VISIBLE
            fingerPrintMask.animate().alpha(1f)
            fingerPrintMask.setOnClickListener {
                //cancellation
                mCancellationSignal.cancel()
                rememberCheckBox.isChecked = false
                fingerPrintMask.animate().alpha(0f).setListener(OnEndAnimationListener{
                    fingerPrintMask.visibility = GONE
                    fingerPrintMask.animate().setListener(OnEndAnimationListener{})
                })
            }
            doBioAuth(namePasswordInfo[0], namePasswordInfo[1], mCancellationSignal)
        }
    }

    private fun onLogin(){
        if (!rememberCheckBox.isChecked){
            viewModel.clearPassWord()
        }

        //指示条显示
        loadingIndicator.visibility = View.VISIBLE
        loadingIndicator.animate().alpha(1f)

        if (userNameTextEditText.text.toString() != "" && passwordTextEditText.text.toString() != ""){
            viewModel.login(
                userNameTextEditText.text.toString(), passwordTextEditText.text.toString(),
                onSuccess = {
                    onLoginSuccess()
                },
                onFailure = {
                    onLoginFailure(it)
                }
            )
        } else {
            onLoginFailure(10) //10是什么都不干，只让加载条消失
            showEmptyEditTextSnackBar()
        }
    }

    private fun doBioAuth(userName: String, password: String, cancelSignal: CancellationSignal){

        //authentication
        manager.authenticate(
            FingerprintManager.CryptoObject(mCipher!!),
            cancelSignal,
            0,
            object : FingerprintManager.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(":::", "onAuthenticationError: ${errString}")
                }

                override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
                    super.onAuthenticationHelp(helpCode, helpString)
                    Log.e(":::", "onAuthenticationHelp: ${helpString}")
                }

                override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    userNameTextEditText.setText(userName)
                    passwordTextEditText.setText(password)
                    onLogin()
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@LoginActivity, "指纹识别失败，请重试", Toast.LENGTH_SHORT).show()
                    doBioAuth(userName, password, cancelSignal)
                }
            },
            @SuppressLint("HandlerLeak") //todo 有可能！！
            object : Handler(){})
    }

    private fun onLoginSuccess(){
        if (rememberCheckBox.isChecked){
            viewModel.keepPassword(userNameTextEditText.text.toString(), passwordTextEditText.text.toString(), bioCheckCheckBox.isChecked)
        } else {
            viewModel.clearPassWord()
        }
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun onLoginFailure(errorCode: Int){
        //指示条隐藏
        loadingIndicator.animate().alpha(0f)
            .setListener(
                OnEndAnimationListener {
                    loadingIndicator.visibility = View.GONE
                }
            )

        when(errorCode){
            NetWorkResponse.WRONG_PASS_OR_NO_USER -> {
                showLoginErrorSnackBar()
            }
            10 ->{}
            else -> {
                showLoginErrorSnackBar(R.string.未知登录错误)
            }
        }
    }

    private fun showEmptyEditTextSnackBar(){
        Snackbar.make(loginView, R.string.不能为空, Snackbar.LENGTH_SHORT)
            .setAction(R.string.重试) {}
            .setAnchorView(logoImageView)
            .setActionTextColor(resources.getColor(R.color.white))
            .show()
    }

    private fun showLoginErrorSnackBar(stringId: Int = R.string.密码或用户名错误){
        Snackbar.make(loginView, stringId, Snackbar.LENGTH_LONG)
            .setAction(R.string.清除密码并重试){
                passwordTextEditText.setText("")
            }
            .setAnchorView(logoImageView)
            .setActionTextColor(resources.getColor(R.color.white))
            .setBackgroundTint(resources.getColor(R.color.red))
            .show()
    }

    private fun showNoConnectionSnackBar(){
        Snackbar.make(loginView, R.string.没有检测到网络连接, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.重试) {
                testConnection()
            }
            .setAnchorView(logoImageView)
            .setActionTextColor(resources.getColor(R.color.white))
            .setBackgroundTint(resources.getColor(R.color.red))
            .show()
    }

    private fun testConnection(){
        viewModel.testConnection {
            //onFailure
            showNoConnectionSnackBar()
        }
    }

    open inner class OnEndAnimationListener(private val mOnAnimationEnd: (animation: Animator?) -> Unit): Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {}
        override fun onAnimationEnd(animation: Animator?) {
            mOnAnimationEnd(animation)
        }
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationRepeat(animation: Animator?) {}
    }
}
