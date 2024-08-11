





package anynews.extension.trtarabi

import anynews.extension.shared.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.*
import org.jsoup.helper.*
import org.jsoup.nodes.*
import org.jsoup.select.*
import org.json.JSONObject
import org.json.JSONArray
import java.util.ArrayList


class TrtArabi : ExtensionAbstract {
    val TRTARABI_NEWS : String = "https://www.trtarabi.com/"
    val SCRAP_PREFIX : String = "https://www.trtarabi.com/api/content?path="
    constructor() {
        categories.add("now")
        categories.add("explainers")
        categories.add("issues")
        categories.add("opinion")
        
    }


    companion object {
        fun wrapTypeWithURL(type : String,count : Int,page : Int) : String {
            return "https://www.trtarabi.com/api/category/$type?limit=$count&offset=$page";
        }
        fun request(url : String) : Response? {
            val client : OkHttpClient= OkHttpClient()
            val request : Request =
                    Request.Builder()
                            .url(url)
                            .addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                            .addHeader("authority", "www.trtarabi.com")
                            .build()
            val response : Response?  = client.newCall(request).execute()
            return response;
        }
    }

    override fun loadNewsHeadlines(type: String, count: Int, offset: Int): ArrayList<NewsCard>? {
        val res :  Response?
        try {
            res  = TrtArabi.request(TrtArabi.wrapTypeWithURL(type,count,offset))
            if(res == null || res.body == null) {
                return null
            }
        } catch(e : Exception) {
            return null
        } 

        val resBody : String = res.body!!.string()
        val jo : JSONObject = JSONObject(resBody);
        val articles : JSONArray =  jo.getJSONObject("news").getJSONArray("contents")
        var list: ArrayList<NewsCard> = ArrayList<NewsCard>()
        
        for(i in 0..articles.length() - 1) {
            val o : JSONObject = articles.getJSONObject(i)
            val title : String = o.getString("title")
            val date : String = o.getString("publishedDate")
            val imgURL : String = o.getString("mainImageUrl")
            val link : String = o.getString("path")
            list.add(NewsCard(title, date, imgURL, link))
        }
        println(list)
        return list
    }

    override fun scrapeUrl(url: String): NewsPage? {
        val res :  Response?
        try {
            res  =   TrtArabi.request(SCRAP_PREFIX + url)
            if(res == null || res.body == null) {
                return null
            }
        } catch(e : Exception) {
            return null
        } 
        //NOTE: there is a related thingy you can get data from it for related output

        val resBody : String = res.body!!.string()
        val jo : JSONObject = JSONObject(resBody);
        val data = jo.getJSONObject("content")
        
        var header: HashMap<String, String> = HashMap()
        header.put(HEADER_TITLE,data.getString("title"))
        header.put(HEADER_AUTHOR_LINK, TRTARABI_NEWS + data.getJSONObject("fields").getJSONArray("authors").getJSONObject(0).getString("path"))
        header.put(HEADER_DATE,data.getJSONObject("published").getString("date"))
        header.put(HEADER_AUTHOR,  { 
                val author = data.getJSONObject("fields").getJSONArray("authors").getJSONObject(0)
                author.getString("firstName") + " " + author.getString("lastName") 
            }()
        )
        val HEADER_IMG = data.getJSONObject("fields").getJSONObject("mainImage").getString("url")
        
        var content: ArrayList<HashMap<String, String>> = ArrayList()
        val body = data.getJSONArray("body")

        for(i in 0..body.length() - 1) {
            val elem = body.getJSONObject(i)

            val value : HashMap<String,String> = HashMap()
            if(elem.getString("blockType") == "text") {
                value.put("type",CONTENT_PARAGRAPH)
                value.put("val",Jsoup.parse(elem.getString("value")).text())
            } else if(elem.getString("blockType") == "youtube") {
                value.put("type","video")
                value.put("val",elem.getJSONObject("metadata").getString("url"))
            }
            if(value.keys.size == 0) continue
            content.add(value)
        }
        return NewsPage(header, content)
    }
}

fun main() {
    val ext: TrtArabi = TrtArabi()
    // ext.loadNewsHeadlines("explainers", 5, 0)
    // ext.scrapeUrl("/explainers/قوة-الدبلوماسية-التركية-كيف-أسهمت-أنقرة-بتبادل-السجناء-بين-روسيا-والغرب-18190838")
}

