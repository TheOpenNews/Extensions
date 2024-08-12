package anynews.extension.aljazeera

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


// TODO:
// add support for content header
// add support for content video
// add support recommended headlines

// scrape home page
class Aljazeera : ExtensionAbstract {
    var categoryMap: HashMap<String, (Int, Int) -> String> = HashMap()
    val ALJAZEERA_LINK : String = "https://www.aljazeera.com"
    constructor() {
        // the api sucks
        // i hardocde tags and urls for know
        // maybe there is a smarter solution using webview bs
        // but not now
        categoryMap.put("Israel palestine conflict",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"israel-palestine-conflict\",\"categoryType\":\"tags\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\",\"liveblog\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("US election",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"us-election-2024\",\"categoryType\":\"tags\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\",\"liveblog\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("Sport",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"sports\",\"categoryType\":\"categories\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\",\"liveblog\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("Opinion",{ _, _ -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoCuratedLandingPageQuery&variables={\"slug\":\"opinion\",\"postTypes\":[\"blog\",\"episode\",\"post\",\"opinion\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\",\"liveblog\"],\"preview\":\"\"}"})
        categoryMap.put("Middle East",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"middle-east\",\"categoryType\":\"where\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\",\"liveblog\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("Africa",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"africa\",\"categoryType\":\"where\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\",\"liveblog\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("Asia Pacific",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoTopicsFeedQuery&variables={\"slug\":\"asia-pacific\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\",\"liveblog\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("Europe",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"europe\",\"categoryType\":\"where\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\",\"liveblog\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("Latin America",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"latin-america\",\"categoryType\":\"where\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\",\"liveblog\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("Canada & US",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"us-canada\",\"categoryType\":\"where\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\",\"liveblog\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("Asia",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"asia\",\"categoryType\":\"where\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\",\"liveblog\"],\"quantity\":$count,\"offset\":$offset}"})
    
    
        categories.add("Israel palestine conflict")
        categories.add("US election")
        categories.add("Sport")
        categories.add("Opinion")
        categories.add("Middle East")
        categories.add("Africa")
        categories.add("Asia Pacific")
        categories.add("Europe")
        categories.add("Latin America")
        categories.add("Canada & US")
        categories.add("Asia")
    }


    companion object {
        fun request(url : String) : Response? {
            val client : OkHttpClient= OkHttpClient()
            val request : Request =
                    Request.Builder()
                            .url(url)
                            .addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                            .addHeader("wp-site", "aje")
                            .build()
            val response : Response?  = client.newCall(request).execute()
            return response;
        }
    
    }


    override fun loadNewsHeadlines(type: String, count: Int, page: Int): ArrayList<NewsCard>? {
        val res :  Response?
        try {
            // the api doesnt use page system but a offset system, so i just get the next page using the count
            // wont work if count is a dynamic number
            // but i use a constant in the app so no worries
            res  =   Aljazeera.request(categoryMap.get(type)!!(count,count * page))
            if(res == null || res.body == null) {
                return null
            }
        } catch(e : Exception) {
            return null
        } 

        val resBody : String = res.body!!.string()
        val jo : JSONObject = JSONObject(resBody);
        val articles : JSONArray =  jo.getJSONObject("data").getJSONArray("articles")
        var list: ArrayList<NewsCard> = ArrayList<NewsCard>()
        for(i in 0..articles.length() - 1) {
            val o : JSONObject = articles.getJSONObject(i)
            val title : String = o.getString("title")
            val date : String = o.getString("date")
            val imgURL : String = ALJAZEERA_LINK + o.getJSONObject("featuredImage").getString("sourceUrl")
            val link : String = ALJAZEERA_LINK + o.getString("link")
            list.add(NewsCard(title, date, imgURL, link))
        }

        return list
    }

    override fun scrapeUrl(url: String): NewsData? {
        val res :  Response?
        try {
            res  =   Aljazeera.request(url)
            if(res == null || res.body == null) {
                return null
            }
        } catch(e : Exception) {
            return null
        } 

        val resBody : String = res.body!!.string()
        val doc :  Elements =  Jsoup.parse(resBody).select("#root main")


        var header: HashMap<String, String> = HashMap()
        var content: ArrayList<HashMap<String, String>> = ArrayList()
        header.put(HEADER_TITLE,doc.select("header h1").text())
        header.put(HEADER_AUTHOR,doc.select(".author-link").text())
        header.put(HEADER_AUTHOR_LINK,ALJAZEERA_LINK + doc.select(".author-link").attr("href"))
        header.put(HEADER_DATE,doc.select(".date-simple span").get(1).text())

        val newsElem = doc.select(".wysiwyg").first()
        
        for(elem in newsElem!!.children()) {
            val tagType = elem.tag().toString() 
            val value : HashMap<String,String> = HashMap()
            if(tagType == "p" && elem.text() != "") {
                value.put("type",CONTENT_PARAGRAPH)
                value.put("val",elem.text())
            } else if (tagType.contains("h") && elem.text() != "") {
                value.put("type",CONTENT_PARAGRAPH)
                value.put("val",elem.text())
            } else if(tagType == "figure") {
                value.put("type",CONTENT_IMAGE)
                value.put("val",ALJAZEERA_LINK +  elem.select("img").attr("src"))
                value.put("caption",elem.select("figcaption").text())
            } 

            if(value.keys.size == 0) continue

            content.add(value)
        }
        return NewsData(header, content)
    }
}

fun main() {
    val news: Aljazeera = Aljazeera()
    // news.loadNewsHeadlines("Sport", 5, 0)
    // news.scrapeUrl("https://www.aljazeera.com/news/2024/8/6/who-is-tim-walz-kamala-harriss-vp-pick-in-us-election")
}