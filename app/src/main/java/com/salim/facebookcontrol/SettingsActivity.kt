package com.salim.facebookcontrol

import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.security.MessageDigest

class SettingsActivity : AppCompatActivity() {
    private val rows = listOf(
        "منع Like / التفاعلات" to Prefs.LIKE,
        "منع التعليقات" to Prefs.COMMENTS,
        "منع المشاركة" to Prefs.SHARE,
        "منع Follow / الصداقة" to Prefs.FOLLOW,
        "منع الملفات الشخصية" to Prefs.PROFILES,
        "منع Messenger" to Prefs.MESSENGER,
        "منع Stories" to Prefs.STORIES,
        "منع Reels / Watch" to Prefs.REELS,
        "منع البحث" to Prefs.SEARCH,
        "منع إنشاء المنشورات" to Prefs.POST,
        "منع رفع الصور والفيديو" to Prefs.UPLOAD,
        "منع Marketplace" to Prefs.MARKETPLACE,
        "منع تفاعلات المجموعات" to Prefs.GROUPS,
        "حظر جميع الأزرار" to Prefs.ALL_BUTTONS,
        "تعطيل JavaScript المخصص" to Prefs.CUSTOM_JS_DISABLED
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Prefs.pin(this) != null) askPin(true)
        else build()
    }
    private fun build(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,20,24,20)}
        root.addView(TextView(this).apply{text="تحكم Facebook";textSize=26f;setPadding(0,0,0,18)})
        rows.forEach{(label,key)->
            val sw=Switch(this).apply{text=label;textSize=17f;isChecked=Prefs.get(this@SettingsActivity,key);setPadding(0,10,0,10);setOnCheckedChangeListener{_,v->Prefs.set(this@SettingsActivity,key,v)}}
            root.addView(sw,LinearLayout.LayoutParams(-1,-2))
        }
        root.addView(TextView(this).apply{text="\nJavaScript مخصص";textSize=20f})
        val js=EditText(this).apply{hint="ضع JavaScript هنا (اختياري)";minLines=5;gravity=Gravity.TOP;inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE;setText(getSharedPreferences("control",0).getString(Prefs.CUSTOM_JS, "") ?: "")}
        root.addView(js)
        val css=EditText(this).apply{hint="ضع CSS هنا (اختياري)";minLines=5;gravity=Gravity.TOP;inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE;setText(Prefs.customCss(this@SettingsActivity))}
        root.addView(css)
        root.addView(Button(this).apply{text="حفظ JavaScript / CSS";setOnClickListener{getSharedPreferences("control",0).edit().putString(Prefs.CUSTOM_JS,js.text.toString()).putString(Prefs.CUSTOM_CSS,css.text.toString()).apply();Toast.makeText(this@SettingsActivity,"تم الحفظ",Toast.LENGTH_SHORT).show()}})
        root.addView(Button(this).apply{text=if(Prefs.pin(this@SettingsActivity)==null) "تعيين PIN" else "تغيير PIN";setOnClickListener{askPin(false)}})
        setContentView(ScrollView(this).apply{addView(root)})
    }
    private fun askPin(checkExisting:Boolean){
        val input=EditText(this).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD;hint="PIN من 4 أو 6 أرقام"}
        AlertDialog.Builder(this).setTitle(if(checkExisting)"أدخل PIN" else "PIN جديد").setView(input).setCancelable(false).setPositiveButton("متابعة"){_,_->
            val value=input.text.toString()
            if(checkExisting){if(hash(value)==Prefs.pin(this)) build() else {Toast.makeText(this,"PIN غير صحيح",Toast.LENGTH_SHORT).show();finish()}}
            else if(value.matches(Regex("\\d{4}|\\d{6}"))){Prefs.setPin(this,hash(value));build()} else Toast.makeText(this,"استخدم 4 أو 6 أرقام",Toast.LENGTH_SHORT).show()
        }.setNegativeButton("إلغاء"){_,_->finish()}.show()
    }
    private fun hash(s:String):String=MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString(""){"%02x".format(it)}
}
