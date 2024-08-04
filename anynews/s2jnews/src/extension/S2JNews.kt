package anynews.extensions

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.util.Arrays
import java.util.Collections

class S2JNews : ExtensionAbstract {
    constructor() {}

    override fun loadNewsHeadlines(type: NewsType, count: Int, page: Int): ArrayList<NewsCard> {
        var mCount =  if (count < 1) 1 else count;
        var mPage =  if (page < 1) 1 else page;
        if (mCount > 100) {
            mPage += mCount / 100;
            mCount = 100;
        }

        // Politics : 74
        // General  : 79
        // Sport    : 70
        val  NewsTypeMapper : List<Int> = Arrays.asList(74,79,70)
            val list: ArrayList<NewsCard> = ArrayList<NewsCard>()
            val client = OkHttpClient()
            val request = Request.Builder().url("https://s2jnews.com/wp-json/wp/v2/posts?page=" + mPage +
                                                                                    "&per_page=" + mCount +
                                                                                    "&categories="+NewsTypeMapper[type.ordinal]).build()
        var response : Response? = null
        Thread {
             response = client.newCall(request).execute()
        }.run()

        if(response == null) {
            return  list;
        } else if(response?.body == null) {
            return  list;
        }


         val responseStr : StringBuilder = StringBuilder();
        responseStr.append("{ DATA : ")
        responseStr.append(response?.body?.string())
        responseStr.append("}")
        val jo : JSONObject = JSONObject(responseStr.toString());
        val data : JSONArray = jo.getJSONArray("DATA");
        for(i in 0..data.length() - 1) {
            val newsInfo : JSONObject = data.getJSONObject(i);
            val title : String = newsInfo.getJSONObject("title").getString("rendered");
            val date : String = newsInfo.getString("date");
            val link : String = newsInfo.getString("link");
            val imgURL : String = newsInfo.getString("jetpack_featured_media_url");
            list.add(NewsCard(title, date, imgURL, link));
        }
        return  list;
    }
}

fun main() {
    val ext = S2JNews()
    val list = ext.loadNewsHeadlines(NewsType.Politics,10,1)
    println(list)
}
