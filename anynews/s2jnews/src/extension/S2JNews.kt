package anynews.extension.s2jnews
 
import anynews.extension.shared.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.text.StringEscapeUtils
import org.json.JSONArray
import org.json.JSONObject
import java.util.Arrays
import org.jsoup.*;
import org.jsoup.helper.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;


class S2JNews : ExtensionAbstract {

    var categoryMap : HashMap<String,Int> = HashMap();
    constructor() {
        categoryMap.put("Latest",-1)
        categoryMap.put("Politics",74)
        categoryMap.put("Sport",70)
        categoryMap.put("General",79)

        categories.add("Latest")
        categories.add("Politics")
        categories.add("Sport")
        categories.add("General")
    }

    companion object {
        fun request(url : String) : Response? {
            val client : OkHttpClient= OkHttpClient()
            val request : Request =
                    Request.Builder()
                            .url(url)
                            .build()
            val response : Response?  = client.newCall(request).execute()
            return response;
        }
    }

    override fun loadNewsHeadlines(type: String, count: Int, page: Int): ArrayList<NewsCard>? {
        var mCount =  if (count < 1) 1 else count;
        var mPage =  if (page < 1) 1 else page;
        if (mCount > 100) {
            mPage += mCount / 100;
            mCount = 100;
        }

        val res :  Response?
        val list: ArrayList<NewsCard> = ArrayList<NewsCard>()

        try {
            var URL : String = "https://s2jnews.com/wp-json/wp/v2/posts?page=" + mPage + "&per_page=" + mCount + "&categories="+categoryMap[type];
            if(type == "Latest") {
                URL =  "https://s2jnews.com/wp-json/wp/v2/posts?page=" + mPage +
                        "&per_page=" + mCount;
            }            
            res  =   S2JNews.request(URL)
            if(res == null || res.body == null) {
                return null
            }        

            var resBody : String = res.body?.string().toString();
            try {
                // if its parsed then there is a problem, its the server returning a error 
                JSONObject(resBody);
                return list;
            } catch(e : Exception) { }

            val jo : JSONObject = JSONObject("{DATA: $resBody }");
            val data : JSONArray = jo.getJSONArray("DATA");
            for(i in 0..data.length() - 1) {
                val newsInfo : JSONObject = data.getJSONObject(i);
                val title : String = StringEscapeUtils.unescapeHtml3(newsInfo.getJSONObject("title").getString("rendered"));
                val date : String = newsInfo.getString("date");
                val link : String = newsInfo.getString("link");
                val imgURL : String = newsInfo.getString("jetpack_featured_media_url");
                list.add(NewsCard(title, date, imgURL, link));
            }
        }        
        catch(e : Exception) { 
            println("Error: " + e)
        }
        
        return  list;
    }

    override fun scrapeUrl(url: String) : NewsData ? {
        val res :  Response?
        val data : NewsData = NewsData()
        try {
            res  =   S2JNews.request(url)
            if(res == null || res.body == null) {
                return null
            }

            val resBody : String = res.body?.string()!!
            val doc: Document = Jsoup.parse(resBody)
            
            val headerElem = doc.select(".td-post-header")
            data.header.title =  StringEscapeUtils.unescapeHtml3(headerElem.select(".entry-title").text())
            data.header.author =  StringEscapeUtils.unescapeHtml3(headerElem.select(".td-post-author-name a").text())
            data.header.author_link =   headerElem.select(".td-post-author-name a").attr("href").toString()
            data.header.date =  headerElem.select(".entry-date").attr("datetime")
            data.header.img =   doc.select(".td-post-featured-image a").attr("href")

            val postBodyElem =  doc.select(".td-post-content")
            var contentArea = false
            for(elem in postBodyElem.get(0).children()) {
                if(!contentArea) {
                    if(elem.hasClass("rt-reading-time")) {
                        contentArea = true;
                    } 
                    continue
                }
                val tagType = elem.tagName()
                val contentElem : NewsContentElem = NewsContentElem()

                if(tagType == "p" && elem.text().length != 0) {
                    contentElem.type = NewsDataContentType.Paragraph
                    contentElem.addMeta("val", elem.text())
                } else if(tagType == "div" && elem.select("img").size != 0) {
                    contentElem.type = NewsDataContentType.Img
                    contentElem.addMeta("val", elem.select("img").attr("src"))
                }

                if(contentElem.metadata.keys.size == 0) continue
                data.content.add(contentElem)
            }            

            for(elem in doc.select(".td-related-span4")) {
                data.related.add(NewsCard(StringEscapeUtils.unescapeHtml3(elem.select(".entry-title.td-module-title").text()), "", doc.select(".td-module-thumb a").get(0).attr("href"), elem.select(".entry-title.td-module-title").attr("href")))
            }

        } catch(e : Exception) {
            println("Error: " + e)
            return null
        } 

        return  data;
    }

}

fun main() {
    // val ext : S2JNews = S2JNews()
    // println(ext.loadNewsHeadlines("Politics", 10, 0))
    // println(ext.scrapeUrl("https://s2jnews.com/delta-airlines-apologizes-for-offensive-tweet-about-palestinian-flag-but-its-retraction-statement-is-not-any-better/"))
}
