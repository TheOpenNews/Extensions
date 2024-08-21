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


class Aljazeera : ExtensionAbstract {
    var categoryMap: HashMap<String, (Int, Int) -> String> = HashMap()
    val ALJAZEERA_LINK : String = "https://www.aljazeera.com"
    val IMG_RESIZE_OPTIONS_HEADLINES = "?resize=200,133&quality=50"
    constructor() {
        iconLink = "aljazeera.png";


        categoryMap.put("Israel palestine conflict",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"israel-palestine-conflict\",\"categoryType\":\"tags\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("US election",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"us-election-2024\",\"categoryType\":\"tags\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("Sport",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"sports\",\"categoryType\":\"categories\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("Opinion",{ _, _ -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoCuratedLandingPageQuery&variables={\"slug\":\"opinion\",\"postTypes\":[\"blog\",\"episode\",\"post\",\"opinion\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\"],\"preview\":\"\"}"})
        categoryMap.put("Middle East",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"middle-east\",\"categoryType\":\"where\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("Africa",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"africa\",\"categoryType\":\"where\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("Asia Pacific",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoTopicsFeedQuery&variables={\"slug\":\"asia-pacific\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("Europe",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"europe\",\"categoryType\":\"where\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("Latin America",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"latin-america\",\"categoryType\":\"where\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("Canada & US",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"us-canada\",\"categoryType\":\"where\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\"],\"quantity\":$count,\"offset\":$offset}"})
        categoryMap.put("Asia",{ count, offset -> "https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoAjeSectionPostsQuery&variables={\"category\":\"asia\",\"categoryType\":\"where\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\"],\"quantity\":$count,\"offset\":$offset}"})
    
    
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
        var list: ArrayList<NewsCard> = ArrayList<NewsCard>()
        try {
            println(categoryMap.get(type)!!(count,count * page));
            res  =   Aljazeera.request(categoryMap.get(type)!!(count,count * page))
            if(res == null || res.body == null) {
                errorHanlder.msg = "got a null for response or body"
                errorHanlder.type = ErrorType.Network
                return null
            }

            val resBody : String = res.body!!.string()
            val jo : JSONObject = JSONObject(resBody);
            val articles : JSONArray =  jo.getJSONObject("data").getJSONArray("articles")
            for(i in 0..articles.length() - 1) {
                val o : JSONObject = articles.getJSONObject(i)
                val title : String = o.getString("title")
                val date : String = o.getString("date")
                val imgURL : String = ALJAZEERA_LINK + o.getJSONObject("featuredImage").getString("sourceUrl") + IMG_RESIZE_OPTIONS_HEADLINES
                val link : String = ALJAZEERA_LINK + o.getString("link")
                val description : String = o.getString("replacementHeadline")
                list.add(NewsCard(title, date, imgURL, link))
            }
    
        } catch(e : Exception) {
            errorHanlder.msg =  e.message.toString()
            errorHanlder.type = ErrorType.Network
            println("Aljazzera Error: " + e)
            return null
        } 
        return list
    }

    override fun scrapeUrl(url: String): NewsData? {
        val res :  Response?
        var data : NewsData =  NewsData()

        try {
            res  =   Aljazeera.request(url)
            if(res == null || res.body == null) {
                errorHanlder.msg = "got a null for response or body"
                errorHanlder.type = ErrorType.Network
                return null
            }

            val resBody : String = res.body?.string()!!
            val doc :  Elements =  Jsoup.parse(resBody).select("#root main")


            data.header.title =  doc.select("header h1").text()
            data.header.author = doc.select(".author-link").text()
            data.header.author_link = ALJAZEERA_LINK + doc.select(".author-link").attr("href")
            data.header.date =  doc.select(".date-simple span").get(1).text()
            data.header.img =  ALJAZEERA_LINK + doc.select(".article-featured-image img").get(0).attr("src")
    
            val newsElem = doc.select(".wysiwyg").first()
            for(elem in newsElem!!.children()) {
                val tagType = elem.tag().toString() 
                val value : NewsContentElem = NewsContentElem()
                if(tagType == "p" && elem.text() != "") {
                    value.type = NewsDataContentType.Paragraph
                    value.addMeta("val",elem.text())
                } else if (tagType.contains("h") && elem.text() != "") {
                    value.type = NewsDataContentType.Header
                    value.addMeta("val",elem.text())
                } else if(tagType == "figure") {
                    value.type = NewsDataContentType.Img
                    value.addMeta("val",ALJAZEERA_LINK +  elem.select("img").attr("src"))
                    value.addMeta("caption",ALJAZEERA_LINK +  elem.select("img").attr("src"))
                    value.addMeta("caption",elem.select("figcaption").text())
                } 
    
                if(value.metadata.keys.size == 0) continue
                data.content.add(value)
            }

            try {
                val res  =  Aljazeera.request("https://www.aljazeera.com/graphql?wp-site=aje&operationName=ArchipelagoMoreFromTopic&variables={\"allPostTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\"],\"trendingCategory\":\"\",\"category\":\"news\",\"postTypes\":[\"blog\",\"episode\",\"opinion\",\"post\",\"video\",\"external-article\",\"gallery\",\"podcast\",\"longform\"],\"currentPostType\":[\"post\"]}")
                val jo  = JSONObject(res!!.body!!.string())
                val arr = jo.optJSONObject("data").getJSONArray("mostRecent")
                for(i in 0..arr.length()-1) {
                    val title : String = arr.getJSONObject(i).getString("title")
                    // val date : String = arr.getJSONObject(i).getString("date")
                    val imgURL : String = ALJAZEERA_LINK + arr.getJSONObject(i).getJSONObject("featuredImage").getString("sourceUrl")
                    val link : String = ALJAZEERA_LINK + arr.getJSONObject(i).getString("link")
                    val description : String = arr.getJSONObject(i).getString("replacementHeadline")

                    data.related.add(NewsCard(title, "", imgURL, link))
                }                
            } catch(e : Exception) {
                println("Aljazzera Error: " + e)


            }
        } catch(e : Exception) {
            errorHanlder.msg =  e.message.toString()
            errorHanlder.type = ErrorType.Network
            println("Aljazzera Error: " + e)
            return null
        } 


        return data
    }
}

fun main() {
    val ext: Aljazeera = Aljazeera()
    // var headlines = ext.loadNewsHeadlines("Sport", 5, 0)
    // println(headlines);
    // println(ext.scrapeUrl("https://www.aljazeera.com/sports/liveblog/2024/8/18/live-chelsea-vs-manchester-city-english-premier-league-football-match"))
}