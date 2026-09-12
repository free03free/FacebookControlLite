package com.salim.facebookcontrol

import android.content.Context
import android.webkit.WebView
import java.net.URI

object Controller {
    private val authPaths = listOf("/login", "/checkpoint", "/recover", "/reg", "/registration")

    fun isAuth(url: String): Boolean {
        return try {
            val u = URI(url)
            val host = u.host?.lowercase() ?: return false
            if (!(host == "facebook.com" || host.endsWith(".facebook.com") || host == "fb.com" || host.endsWith(".fb.com"))) return false
            val path = u.path.lowercase()
            authPaths.any { path == it || path.startsWith("$it/") }
        } catch (_: Exception) { false }
    }

    fun blockUrl(c: Context, url: String): Boolean {
        if (isAuth(url)) return false
        val u = url.lowercase()
        val rules = listOf(
            Prefs.MESSENGER to listOf("/messages", "messenger.com"),
            Prefs.STORIES to listOf("/stories"),
            Prefs.REELS to listOf("/reel", "/reels", "/watch"),
            Prefs.SEARCH to listOf("/search"),
            Prefs.MARKETPLACE to listOf("/marketplace"),
            Prefs.PROFILES to listOf("/profile.php", "/people/"),
            Prefs.POST to listOf("/composer", "/create/"),
            Prefs.UPLOAD to listOf("/photos/upload", "/video/upload"),
            Prefs.FOLLOW to listOf("/friends/requests")
        )
        return rules.any { (key, paths) -> Prefs.get(c, key) && paths.any { u.contains(it) } }
    }

    fun inject(c: Context, v: WebView) {
        if (isAuth(v.url ?: "")) return
        val css = buildCss(c)
        val js = buildJs(c)
        if (css.isNotEmpty()) v.evaluateJavascript("(function(){var s=document.getElementById('__fcl_css');if(!s){s=document.createElement('style');s.id='__fcl_css';document.documentElement.appendChild(s);}s.textContent=${quote(css)};})();", null)
        if (js.isNotEmpty()) v.evaluateJavascript("(function(){try{${js}}catch(e){}})();", null)
        val custom = Prefs.customJs(c)
        if (!custom.isNullOrBlank()) v.evaluateJavascript("(function(){try{${custom}}catch(e){}})();", null)
    }

    private fun buildCss(c: Context): String {
        val b = StringBuilder()
        if (Prefs.get(c, Prefs.ALL_BUTTONS)) b.append("button,[role='button'],input[type='button'],input[type='submit'],input[type='reset']{visibility:hidden!important;pointer-events:none!important;}")
        if (Prefs.get(c, Prefs.MESSENGER)) b.append("a[href*='/messages']{display:none!important;}")
        if (Prefs.get(c, Prefs.STORIES)) b.append("a[href*='/stories']{display:none!important;}")
        if (Prefs.get(c, Prefs.REELS)) b.append("a[href*='/reel'],a[href*='/reels'],a[href*='/watch']{display:none!important;}")
        if (Prefs.get(c, Prefs.SEARCH)) b.append("a[href*='/search']{display:none!important;}")
        if (Prefs.get(c, Prefs.MARKETPLACE)) b.append("a[href*='/marketplace']{display:none!important;}")
        return b.toString()
    }

    private fun buildJs(c: Context): String {
        val flags = mapOf(
            "like" to Prefs.LIKE, "comment" to Prefs.COMMENTS, "share" to Prefs.SHARE,
            "follow" to Prefs.FOLLOW, "profiles" to Prefs.PROFILES, "messenger" to Prefs.MESSENGER,
            "stories" to Prefs.STORIES, "reels" to Prefs.REELS, "search" to Prefs.SEARCH,
            "post" to Prefs.POST, "upload" to Prefs.UPLOAD, "market" to Prefs.MARKETPLACE,
            "groups" to Prefs.GROUPS
        ).filter { Prefs.get(c, it.value) }.keys
        val json = flags.joinToString(",") { "'$it':true" }
        return """
            const F={$json};
            function fcl(){
              const els=document.querySelectorAll('a,button,[role="button"],div');
              els.forEach(function(e){
                const t=((e.innerText||'')+' '+(e.getAttribute('aria-label')||'')+' '+(e.getAttribute('title')||'')).trim().toLowerCase();
                const h=(e.getAttribute('href')||'').toLowerCase();
                let hide=false;
                if(F.like && /like|react|أعجب|إعجاب|تفاعل/.test(t)) hide=true;
                if(F.comment && /comment|تعليق/.test(t)) hide=true;
                if(F.share && /share|مشاركة/.test(t)) hide=true;
                if(F.follow && /follow|friend|متابعة|صديق/.test(t)) hide=true;
                if(F.profiles && (/profile|people\\//.test(h) || /profile|الملف الشخصي/.test(t))) hide=true;
                if(F.messenger && (/messenger|messages|رسائل/.test(h+t))) hide=true;
                if(F.stories && /stories|قصة|قصص/.test(h+t)) hide=true;
                if(F.reels && /reel|watch|ريلز|مشاهدة/.test(h+t)) hide=true;
                if(F.search && /search|بحث/.test(h+t)) hide=true;
                if(F.post && /create|composer|post|منشور|نشر/.test(h+t)) hide=true;
                if(F.upload && /upload|photo|video|رفع|صورة|فيديو/.test(h+t)) hide=true;
                if(F.market && /marketplace|السوق/.test(h+t)) hide=true;
                if(F.groups && /group|مجموعة|مجموعات/.test(h+t)) hide=true;
                if(hide){e.style.setProperty('display','none','important');e.style.setProperty('pointer-events','none','important');}
              });
            }
            fcl(); setInterval(fcl,800);
            document.addEventListener('click',function(e){
              let n=e.target; for(let i=0;n&&i<5;i++,n=n.parentElement){
                const t=((n.innerText||'')+' '+(n.getAttribute&&n.getAttribute('aria-label')||'')).toLowerCase();
                if((F.like&&/like|react|أعجب|إعجاب|تفاعل/.test(t))||(F.comment&&/comment|تعليق/.test(t))||(F.share&&/share|مشاركة/.test(t))){e.preventDefault();e.stopImmediatePropagation();return false;}
              }
            },true);
        """.trimIndent()
    }

    private fun quote(s: String): String = org.json.JSONObject.quote(s)
}
