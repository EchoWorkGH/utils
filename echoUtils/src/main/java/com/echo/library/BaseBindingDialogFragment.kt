package com.echo.library

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.Keep
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.echo.library.network.RxUtil
import com.echo.library.rv.HeaderAndFooterAdapter
import com.echo.library.util.DialogUtils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/14
 * change   :
 * describe :
 */
@Keep
abstract class BaseBindingDialogFragment<T : ViewDataBinding> : BaseVHDialogFragment() {
    protected var mContext: Context? = null
    protected var mRes: Resources? = null
    protected var mPageIndex = 1
    protected var mPageSize = 6
    protected abstract fun updateData() //更新数据
    protected lateinit var binding: T
    val headerAndFooterAdapter = HeaderAndFooterAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.TransparentTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (dialog != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
                    or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        val view = inflater.inflate(layoutId, container, false)
        binding = DataBindingUtil.bind(view)!!
        initView()
        return view
    }

    fun onClickBack() {
        dismiss()
    }

    fun onClickBack(view: View?) {
        onClickBack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mRes = mContext!!.resources
    }

    var needUpdateOnResume = true
    override fun onResume() {
        super.onResume()
        if (needUpdateOnResume) {
            updateData()
        }
    }

    /**
     * 加载页面布局文件
     *
     * @return
     */
    protected abstract val layoutId: Int

    /**
     * 让布局中的view与fragment中的变量建立起映射
     */
    protected abstract fun initView()
    fun showLoadingDialog() {
        RxUtil.runMainThread { DialogUtils.showProgressDialog(activity) }
    }

    fun showLoadingDialog(o: Any?) {
        showLoadingDialog()
    }

    fun dismissLoadingDialog() {
        RxUtil.runMainThread { DialogUtils.hideProgressDialog() }
    }

    protected var mCompositeDisposable: CompositeDisposable? = null
    fun addSubscription(disposable: Disposable?) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = CompositeDisposable()
        }
        mCompositeDisposable!!.add(disposable!!) //将所有disposable放入,集中处理
    }

    //RxJava解除订阅
    protected fun unDispose() {
        if (mCompositeDisposable != null) {
            //保证LifecycleOwner结束时取消所有正在执行的订阅
            mCompositeDisposable!!.clear()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unDispose()
    }
}