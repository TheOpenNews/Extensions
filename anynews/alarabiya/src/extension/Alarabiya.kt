package anynews.extension.alarabiya

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

//TODO: this is the arabic website !!
//TODO: vidio support
class Alarabiya : ExtensionAbstract {
    var categoryToLink : HashMap<String,String> = HashMap()
    constructor() {
        categoryToLink.put("Latest", "https://www.alarabiya.net/latest-news/archive")
        categoryToLink.put("Saudi Today", "https://www.alarabiya.net/saudi-today/archive")
        categoryToLink.put("Aswaq", "https://www.alarabiya.net/aswaq/archive")
        categoryToLink.put("Sport", "https://www.alarabiya.net/sport/archive")
        categoryToLink.put("Variety", "https://www.alarabiya.net/variety/archive")
        categoryToLink.put("Latest General", "https://www.alarabiya.net/last-page/archive")

        categories.add("Latest")
        categories.add("Saudi Today")
        categories.add("Aswaq")
        categories.add("Sport")
        categories.add("Variety")
        categories.add("Latest General")
    }

    companion object {
        fun request(url : String) : Response? {
            val client : OkHttpClient= OkHttpClient()
            val request : Request =
                    Request.Builder()
                            .url(url)
                            .addHeader("user-agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36,gzip(gfe)")
                            .addHeader("authority", "www.alarabiya.net")
                            .build()
            val response : Response?  = client.newCall(request).execute()
            return response;
        }
    }

    override fun loadNewsHeadlines(type: String, count: Int, offset: Int): ArrayList<NewsCard>? {
        // TODO: add page offset
        val res :  Response?
        try {
            res  = Alarabiya.request(categoryToLink.get(type)!!)
            if(res == null || res.body == null) {
                return null
            }
        } catch(e : Exception) {
            return null
        } 

        val resBody : String = res.body?.string()!!
        val doc : Document = Jsoup.parse(resBody)

        val elems : Elements = doc.select(".latest_element")
        var list: ArrayList<NewsCard> = ArrayList<NewsCard>()
        for(elem in elems) {
            val title : String = elem.select(".latest_link").attr("title") 
            val date : String =   elem.select("services caption").text()
            val imgURL : String = elem.select(".latest_img img").attr("src")
            val link : String = elem.select(".latest_link").attr("href") 
            list.add(NewsCard(title, date, imgURL, link))
        } 

        return list
    }

    override fun scrapeUrl(url: String): NewsPage? {
        val res :  Response?
        try {
            res  = Alarabiya.request(url)
            if(res == null || res.body == null) {
                return null
            }
        } catch(e : Exception) {
            return null
        } 

        
        val resBody : String = res.body?.string()!!
        val doc : Document = Jsoup.parse(resBody)
        val body = doc.select("#body-text").first()!!


        var header: HashMap<String, String> = HashMap()
        header.put(HEADER_TITLE,doc.select(".headingInfo_title").get(0).text())
        header.put(HEADER_DATE,doc.select(".timeDate_element time").get(0).attr("datetime"))


        var content: ArrayList<HashMap<String, String>> = ArrayList()
        for(elem in body.children()) {
            val tagType = elem.tagName().toString()
            val value : HashMap<String,String> = HashMap()

            if(tagType == "p" && elem.text() != "") {
                value.put("type",CONTENT_PARAGRAPH)
                value.put("val",elem.text())
            } else if (tagType.contains("h") && elem.text() != "") {
                value.put("type",CONTENT_PARAGRAPH)
                value.put("val",elem.text())
            } else if(tagType == "div" && elem.classNames().contains("feed-card")) {
            value.put("type",CONTENT_IMAGE)
            value.put("val",elem.select("img").attr("src"))
            }
            else {
            } 

            if(value.keys.size == 0) continue
            content.add(value)
        }
        return NewsPage(header, content)
    }
}

fun main() {
    val ext: Alarabiya = Alarabiya()
    // ext.loadNewsHeadlines("Latest", 0, 0)
    println(ext.scrapeUrl("https://www.alarabiya.net/arab-and-world/2024/08/11/%D8%A7%D9%84%D9%85%D9%84%D9%83-%D8%B9%D8%A8%D8%AF%D8%A7%D9%84%D9%84%D9%87-%D8%A7%D9%84%D8%AB%D8%A7%D9%86%D9%8A-%D8%A7%D9%84%D8%A3%D8%B1%D8%AF%D9%86-%D9%84%D9%86-%D9%8A%D9%83%D9%88%D9%86-%D8%B3%D8%A7%D8%AD%D8%A9-%D8%AD%D8%B1%D8%A8"))
}

