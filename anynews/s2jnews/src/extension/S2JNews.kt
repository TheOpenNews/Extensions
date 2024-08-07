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
    constructor() {}

    override fun loadNewsHeadlines(type: NewsType, count: Int, page: Int): ArrayList<NewsCard>? {
        var mCount =  if (count < 1) 1 else count;
        var mPage =  if (page < 1) 1 else page;
        if (mCount > 100) {
            mPage += mCount / 100;
            mCount = 100;
        }
        // Politics : 74
        // Sport    : 70
        // General  : 79
        val  NewsTypeMapper : List<Int> = Arrays.asList(74,70,79)
            val list: ArrayList<NewsCard> = ArrayList<NewsCard>()
            val client = OkHttpClient()

        var request : Request? = null
        try {
            request = Request.Builder().url("https://s2jnews.com/wp-json/wp/v2/posts?page=" + mPage +
                    "&per_page=" + mCount +
                    "&categories="+NewsTypeMapper[type.ordinal]).build()
        } catch (e : Exception) {
            return null
        }

        var response : Response = client.newCall(request).execute()
        if(response.body == null) {
            return  null;
        }

        var resBody : String = response.body?.string().toString();

        // returns either a error telling me i have no more news
        // or the list of news news
        try {
            // if its parsed then there is a problem
            val tmp : JSONObject = JSONObject(resBody);
            return list;
        } catch(e : Exception) {}

        
        
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
        return  list;
    }

    override fun scrapeUrl(url: String) : NewsPage ? {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        var response : Response?
        try {
            response  = client.newCall(request).execute()

        } catch (e : Exception) {
            return  null
        }

        var header : HashMap<String,String> = HashMap()
        var content : ArrayList<HashMap<String,String>> = ArrayList();

        if(response.body == null) {
            return null
        }

        val resBody : String = response.body?.string() as String
        val doc: Document = Jsoup.parse(resBody)

        // header
        run {
            val headerElem = doc.select(".td-post-header")
            val titleElem = headerElem.select(".entry-title")
            val authorElem = headerElem.select(".td-post-author-name a")
            val dateElem = headerElem.select(".entry-date")
            header.put(HEADER_TITLE,StringEscapeUtils.unescapeHtml3(titleElem.text()))
            header.put(HEADER_AUTHOR,StringEscapeUtils.unescapeHtml3(authorElem.text()))
            header.put(HEADER_AUTHOR_LINK,authorElem.attr("href").toString())
            header.put(HEADER_DATE,dateElem.attr("datetime"))
        }

        // post content
        run {
            val imgElem =  doc.select(".td-post-featured-image a")
            val value : HashMap<String,String> = HashMap()
            value.put(CONTENT_IMAGE,imgElem.attr("href"))
            content.add(value)


            val postBodyElem =  doc.select(".td-post-content")
            for(pElem in postBodyElem.select("p")) {
                val value : HashMap<String,String> = HashMap()
                val text = StringEscapeUtils.unescapeHtml3(pElem.text())
                if(text.length == 0) continue
                value.put(CONTENT_PARAGRAPH,text)
                content.add(value)
            }
        }

        return  NewsPage(header,content);
    }

}

fun main() {
}
