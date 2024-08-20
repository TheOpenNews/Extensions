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

class Alarabiya : ExtensionAbstract {
    var categoryToLink : HashMap<String,String> = HashMap()

    // https://vid.alarabiya.net/images/2024/08/20/8f906da9-1a33-4bd8-81c9-0e582f5f58b3/8f906da9-1a33-4bd8-81c9-0e582f5f58b3_16x9_600x338.jpeg?width=,
    constructor() {
        iconLink = "alarabiya.jpg";


        categoryToLink.put("Saudi Today", "https://www.alarabiya.net/saudi-today/archive")
        categoryToLink.put("Aswaq", "https://www.alarabiya.net/aswaq/archive")
        categoryToLink.put("Sport", "https://www.alarabiya.net/sport/archive")
        categoryToLink.put("Variety", "https://www.alarabiya.net/variety/archive")
        categoryToLink.put("Latest General", "https://www.alarabiya.net/last-page/archive")

        categories.add("Latest General")
        categories.add("Saudi Today")
        categories.add("Aswaq")
        categories.add("Sport")
        categories.add("Variety")
    }
    companion object {
        fun request(url : String) : Response? {
            val client : OkHttpClient= OkHttpClient()
            val request : Request =
                    Request.Builder()
                            .url(url)
                            .addHeader("user-agent", "Mozilla/5.0 (PlayStation; PlayStation 5/2.26) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Safari/605.1.15")
                            .addHeader("authority", "www.alarabiya.net")
                            .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                            .build()
            val response : Response?  = client.newCall(request).execute()
            return response;
        }
    }

    override fun loadNewsHeadlines(type: String, count: Int, offset: Int): ArrayList<NewsCard>? {
        var list: ArrayList<NewsCard> = ArrayList<NewsCard>()
        val res :  Response?
        try {
            res  = Alarabiya.request(categoryToLink.get(type)!! + "?pageNo=${Math.max(offset, 1)}")
            if(res == null || res.body == null) {
                errorHanlder.msg =  "res == null || res.body == null"
                errorHanlder.type = ErrorType.Network
                println("Alarabiya Error: " + "res == null || res.body == null")
                return null
            }
            val resBody : String = res.body?.string()!!
            val doc : Document = Jsoup.parse(resBody)
            

            var elems : Elements = doc.select(".list-item")
            if(elems.size == 0) {
                elems = doc.select(".latest_element")
            }
            for(elem in elems) {
                val title : String = elem.select(".latest_link").attr("title") 
                val date : String =   elem.select(".caption").text()
                var imgURL : String = elem.select("img").attr("src")
                imgURL = imgURL.substring(0,(imgURL.indexOf("width=") + "width=".length)) + "200&format=jpg"
                val link : String = elem.select(".latest_link").attr("href") 
                list.add(NewsCard(title, date, imgURL, link))
            } 
        } catch(e : Exception) {
            errorHanlder.msg =  e.message.toString()
            errorHanlder.type = ErrorType.Network
            println("Alarabiya Error: " + e)
            return null
        } 
        return list
    }

    override fun scrapeUrl(url: String): NewsData? {
        val res :  Response?
        var data : NewsData =  NewsData()

        try {
            res  = Alarabiya.request(url)
            if(res == null || res.body == null) {
                errorHanlder.msg =  "res == null || res.body == null"
                errorHanlder.type = ErrorType.Network
                println("Alarabiya Error: " + "res == null || res.body == null")
                return null
            }
        
            val resBody : String = res.body?.string()!!
            val doc : Document = Jsoup.parse(resBody)
            val body = doc.select("#body-text").first()!!
            
            data.header.title = doc.select(".headingInfo_title").get(0).text() 
            data.header.date = doc.select(".timeDate_element time").get(0).attr("datetime")
            data.header.img = doc.select(".article-hero-img picture source").get(0).attr("srcset")

            for(elem in body.children()) {
                val tagType = elem.tagName().toString()
                val value : NewsContentElem = NewsContentElem()
    
                if(tagType == "p" && elem.text() != "") {
                    value.type = NewsDataContentType.Paragraph
                    value.addMeta("val", elem.text())
                } else if (tagType.contains("h") && elem.text() != "") {
                    value.type = NewsDataContentType.Header
                    value.addMeta("val", elem.text())
                } else if(tagType == "div" && elem.classNames().contains("feed-card")) {
                    value.type = NewsDataContentType.Img
                    value.addMeta("val", elem.select("img").attr("src"))
                }
                else if(tagType == "div" && elem.select("video-js").size != 0) {
                    value.type = NewsDataContentType.VidLink
                    value.addMeta("val", elem.select("source").get(1).attr("src"))
                }

                if(value.metadata.keys.size == 0) continue
                data.content.add(value)
            }   

            val relatedElems = doc.select(".latest .latest_element")
            for(elem in relatedElems) {
                data.related.add(NewsCard(
                    elem.select(".latest_news").text(),
                    "", // no date is provided
                    elem.select(".latest_img img").attr("src"),
                    elem.select(".latest_link").attr("href"),
                ))
            }
        } catch(e : Exception) {
            errorHanlder.msg =  e.message.toString()
            errorHanlder.type = ErrorType.Network
            println("Alarabiya Error: " + e.message)
            return null
        } 

        
        return data;
    }
}

fun main() {
    val ext: Alarabiya = Alarabiya()
    println(ext.loadNewsHeadlines("Aswaq", 5, 0))
    // println(ext.scrapeUrl("https://www.alarabiya.net/arab-and-world/2024/08/11/%D8%B1%D9%88%D8%B3%D9%8A%D8%A7-%D8%AA%D8%AA%D9%88%D8%B9%D8%AF-%D8%A3%D9%88%D9%83%D8%B1%D8%A7%D9%86%D9%8A%D8%A7-%D8%A8%D9%80-%D8%B1%D8%AF-%D9%82%D8%A7%D8%B3-%D9%84%D9%86-%D9%8A%D8%AA%D8%A3%D8%AE%D8%B1-"))
}

