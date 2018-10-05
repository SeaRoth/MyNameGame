package wt.cr.com.mynamegame.infrastructure.common.utils

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.BindingAdapter
import android.databinding.BindingConversion
import android.graphics.drawable.Drawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Spinner
import com.squareup.picasso.Picasso
import android.view.ViewGroup
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator


@BindingConversion
fun convertBooleanToVisibility(visible: Boolean): Int {
    return if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter(value=["app:imageUrl", "app:placeholder"],requireAll=false)
fun loadImage(imageView: ImageView, url: String?, placeholder: Drawable) {
    WTServiceLocator.resolve(Picasso::class.java).load(url).placeholder(placeholder).into(imageView)
}

@BindingAdapter("app:removeFocus")
fun setFocus(v: View, removeFocus: Boolean) {
    if (removeFocus) {
        v.isFocusableInTouchMode = false
        v.isFocusable = false
        v.isFocusableInTouchMode = true
        v.isFocusable = true
        closeSoftKeyboard(v)
    }
}

fun closeSoftKeyboard(view: View){
    val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.applicationWindowToken, 0)
}

fun BaseObservable.onChange(action: () -> Unit) {
    this.addOnPropertyChangedCallback(object : android.databinding.Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: android.databinding.Observable?, propertyId: Int) {
            action()
        }
    })
}

@BindingAdapter("app:layout_marginBottom")
fun setMarginBottom(view: View, margin: Int) {
    val params = view.layoutParams
    if (params is ViewGroup.MarginLayoutParams) {
        params.bottomMargin = margin
        view.requestLayout()
    }
}