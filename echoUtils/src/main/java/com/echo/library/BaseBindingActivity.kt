package com.echo.library

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.echo.library.network.Subscription
import com.echo.library.rv.HeaderAndFooterAdapter
import com.echo.library.util.DialogUtils
import io.reactivex.disposables.Disposable

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/30
 * change   :
 * describe :
 */
@Keep
abstract class BaseBindingActivity<T : ViewDataBinding> : AppCompatActivity() {
    val headerAndFooterAdapter = HeaderAndFooterAdapter()
    lateinit var binding: T
    protected abstract val layoutId: Int
    val activity: BaseBindingActivity<*>
        get() = this
    var needUpdateOnResume = true
    override fun onResume() {
        super.onResume()
        if (needUpdateOnResume) {
            updateData()
        }
    }

    open fun updateData() {}


    lateinit var subscription: Subscription
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
                or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        binding = DataBindingUtil.setContentView(this,
                layoutId)
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                vhInit()
                vhSwitch(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                binding.getRoot().viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        subscription = Subscription(this)
        getExtraData(intent)
        initView()
    }

    open fun getExtraData(intent: Intent) {}
    open fun initView() {}
    var constraintLayoutH: ConstraintSet? = null
    var constraintLayoutV: ConstraintSet? = null
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (constraintLayoutV != null) {
            vhSwitch(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
        }
    }

    /**
     * 重置横竖布局
     * eg：有时候布局会有变化，需要重置
     */
    open fun resetVh() {
        constraintLayoutV = null
        constraintLayoutH = null
        vhInit()
    }

    /**
     * 需要切换布局的时候，根布局是ConstraintLayout的时候使用
     */
    @CallSuper
    open fun vhInit() {
        if (constraintLayoutV != null) {
            return
        }
        if (vhGetRootConstraintLayout() == null) {
            return
        }
        constraintLayoutV = ConstraintSet().apply { clone(vhGetRootConstraintLayout()) }
        constraintLayoutH = ConstraintSet().apply { clone(vhGetRootConstraintLayout()) }
    }

    /**
     * 标准的统一的额背景
     * 竖屏 345*507
     * 横屏 507*331
     */
    open val standardBgView: View?
        get() = null

    /**
     * @param isv 竖向布局
     */
    @CallSuper
    open fun vhSwitch(isv: Boolean) {
        if (vhGetRootConstraintLayout() == null) {
            return
        }
        val cu = if (isv) constraintLayoutV else constraintLayoutH
        cu?.apply { applyTo(vhGetRootConstraintLayout()) }
    }

    /**
     * 需要切换布局的时候，根布局是ConstraintLayout的时候使用
     */
    open fun vhGetRootConstraintLayout(): ConstraintLayout? {
        return null
    }

    fun showLoadingDialog() {
        DialogUtils.showProgressDialog(this)
    }

    fun showLoadingDialog(disposable: Disposable?) {
        showLoadingDialog()
    }

    fun dismissLoadingDialog() {
        DialogUtils.hideProgressDialog()
    }

    fun addSubscription(disposable: Disposable) {
        subscription.addSubscription(disposable)
    }


    open fun onClickBack(v: View?) {
        finish()
    }

    open fun onFragmentDismiss(baseBindingDialogFragment: BaseBindingDialogFragment<*>) {

    }
}