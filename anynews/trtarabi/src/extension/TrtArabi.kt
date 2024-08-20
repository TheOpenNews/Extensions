





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
    val IMG_RESIZE_LINK_200 = "https://mguot2nqx9.execute-api.eu-west-1.amazonaws.com/default/r/trtarabi/w100/q50/"
    val IMG_RESIZE_LINK_500 = "https://mguot2nqx9.execute-api.eu-west-1.amazonaws.com/default/r/trtarabi/w500/q60/"
    constructor() {
        iconLink = "trtarabi.jpg";

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
            var list: ArrayList<NewsCard> = ArrayList<NewsCard>()
            try {
            res  = TrtArabi.request(TrtArabi.wrapTypeWithURL(type,count,offset))
            if(res == null || res.body == null) {
                return null
            }
            val resBody : String = res.body!!.string()
            val jo : JSONObject = JSONObject(resBody);
            val articles : JSONArray =  jo.getJSONObject("news").getJSONArray("contents")
            
            for(i in 0..articles.length() - 1) {
                val o : JSONObject = articles.getJSONObject(i)
                val title : String = o.getString("title")
                val date : String = o.getString("publishedDate")
                var imgURL : String = o.getString("mainImageUrl")
                imgURL = IMG_RESIZE_LINK_200 + imgURL.substring(imgURL.indexOf("/trtarabi/") + "/trtarabi/".length)

                val link : String = o.getString("path")
                list.add(NewsCard(title, date, imgURL, link))
            }            
        } catch(e : Exception) {
            println("TrtArabi loadNewsHeadlines Error: " + e.message);
            return null
        } 
        return list
    }

    override fun scrapeUrl(url: String): NewsData? {
        val res :  Response?
        val data :  NewsData= NewsData()
        try {
            res  =   TrtArabi.request(SCRAP_PREFIX + url)
            if(res == null || res.body == null) {
            println("TrtArabi loadNewsHeadlines Error: null response");
            return null
            }

            val resBody : String = res.body!!.string()
            val jo : JSONObject = JSONObject(resBody);
            val content = jo.getJSONObject("content")
    

        data.header.title =  content.getString("title")
        data.header.author_link =   TRTARABI_NEWS + content.getJSONObject("fields").getJSONArray("authors").getJSONObject(0).getString("path")
        data.header.date =  content.getJSONObject("published").getString("date")
        data.header.img = content.getJSONObject("fields").getJSONObject("mainImage").getString("url")
        data.header.author =    { 
                val author = content.getJSONObject("fields").getJSONArray("authors").getJSONObject(0)
                author.getString("firstName") + " " + author.getString("lastName") 
            }()
        
        val body = content.getJSONArray("body")

        for(i in 0..body.length() - 1) {
            val elem = body.getJSONObject(i)
            val value : NewsContentElem = NewsContentElem()

            if(elem.getString("blockType") == "text") {
                value.type = NewsDataContentType.Paragraph
                value.addMeta("val", Jsoup.parse(elem.getString("value")).text())
            } else if(elem.getString("blockType") == "youtube") {
                value.type = NewsDataContentType.VidLink
                value.addMeta("val", elem.getJSONObject("metadata").getString("url"))
            }
            if(value.metadata.keys.size == 0) continue
            data.content.add(value)
        }


        val related  = jo.getJSONArray("related")
        for(i in 0..related.length()-1) {
            val o = related.getJSONObject(i)
            val title : String = o.getString("title")
            val date : String = o.getString("publishedDate")
            val imgURL : String = o.getString("mainImageUrl")
            val link : String = o.getString("path")
            data.related.add(NewsCard(title, date, imgURL, link))            
        }

    } catch(e : Exception) {
            println("TrtArabi loadNewsHeadlines Error: " + e.message);
        return null
    } 

        return data
    }
}

fun main() {
    val ext: TrtArabi = TrtArabi()
    println(ext.loadNewsHeadlines("now", 5, 0))
    //println(ext.scrapeUrl("/explainers/قوة-الدبلوماسية-التركية-كيف-أسهمت-أنقرة-بتبادل-السجناء-بين-روسيا-والغرب-18190838"))
}

