package crawler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dao.Project;
import dao.ProjectDao;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Crawler {
    private OkHttpClient okHttpClient = new OkHttpClient();
    private Gson gson = new GsonBuilder().create();

    private HashSet<String> urlBlackList = new HashSet<>();

    {
        urlBlackList.add("https://github.com/events");
        urlBlackList.add("https://github.community");
        urlBlackList.add("https://github.com/about");
        urlBlackList.add("https://github.com/pricing");
        urlBlackList.add("https://github.com/contact");
    }

    public static void main(String[] args) throws IOException {
        Crawler crawler = new Crawler();

        long startTime = System.currentTimeMillis();

        // 1. 获取入口页面
        String html = crawler.getPage("https://github.com/akullpp/awesome-java/blob/master/README.md");
        // System.out.println(respBody);

        long finishTime = System.currentTimeMillis();
        System.out.println("获取入口时间： "+ (finishTime-startTime) +"ms");
        // 2. 解析入口页面, 获取项目列表
        List<Project> projects = crawler.parseProjectList(html);
        // System.out.println(projects);
        // 3. 遍历项目列表, 调用 github API 获取项目信息

        for (int i = 0; i < projects.size(); i++) {
            try {
                Project project = projects.get(i);
                System.out.println("crawing " + project.getName()+" ....");
                String repoName = crawler.getRepoName(project.getUrl());
                String jsonString = crawler.getRepoInfo(repoName);
                // System.out.println(jsonString);
                // 4. 解析每个仓库获取到的 JSON 数据, 得到需要的信息
                crawler.parseRepoInfo(jsonString, project);
                // 5. 在这里保存到数据库
                System.out.println("crawing " + project.getName()+" done!");
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        ProjectDao projectDao = new ProjectDao();
        for(int i = 0; i < projects.size(); i++){
            Project project = projects.get(i);
            projectDao.save(project);
        }
    }

    // 使用 Okhttp 获取github页面
    public String getPage(String url) throws IOException {
        // 1. 先创建一个 OkHttpClient 对象, OkHttpClient 只要一个程序中包含一个实例即可
        //    而像 Request, Call, Response 这些就需要每次请求都创建
        // 2. 创建一个 Request 对象
        //    Java 中实例化一个对象有很多方式.
        //    可以直接 new , 也可以使用某个静态的工厂方法来创建实例
        //    此处的 Builder 这个类是一个辅助构造 Request 对象的类.
        //    Builder 中提供的 url 方法能够设定当前请求的url
        //    这个代码中涉及到一系列的链式调用, 大家要注意体会~~
        Request request = new Request.Builder().url(url).build();
        // 3. 创建一个 Call 对象(这个对象负责进行一次网络访问操作)
        Call call = okHttpClient.newCall(request);
        // 4. 发送请求给服务器, 获取到 response 对象
        Response response = call.execute();
        // 5. 判定响应是否成功
        if (!response.isSuccessful()) {
            System.out.println("请求失败!");
            return null;
        }
        return response.body().string();
    }

    // 使用 Jsoup 分析页面
    public List<Project> parseProjectList(String html) {
        ArrayList<Project> result = new ArrayList<>();
        // 1. 创建 Document 对象
        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByTag("li");
        for (Element li : elements) {
            // 再去获取里面的 a 标签.
            Elements allLink = li.getElementsByTag("a");
            if (allLink.size() == 0) {
                // 当前的这个 li 标签中没有包含 a 标签. 直接忽略掉这个 li
                continue;
            }
            // 一个项目的 li 标签里, 只有一个 a 标签.
            Element link = allLink.get(0);
            String url = link.attr("href");
            if (!url.startsWith("https://github.com")) {
                // 如果当前这个项目的 url 不是以 https://github.com 开头的, 我们就直接丢弃掉
                continue;
            }
            if (urlBlackList.contains(url)) {
                continue;
            }
            Project project = new Project();
            project.setName(link.text());
            project.setUrl(link.attr("href"));
            project.setDescription(li.text());
            result.add(project);
        }
        return result;
    }

    // 调用 Github API 获取指定仓库的信息(官方允许)
    // repoName 形如 doov-io/doov
    public String getRepoInfo(String repoName) throws IOException {
        String userName = "Chakra-Z";
        String password = "awsedr76754";
        // 进行身份认证, 把用户名密码加密之后, 得到一个字符串, 把这个字符串放到 HTTP header 中.
        // 此处只是针对用户名密码进行了 base64 加密. 严格意义上说, 没啥卵用, 可以很容易解密.
        // 总是好过直接传输明文
        // credential 形如: Basic SEd0ejIyMjI6dHoyMjIyMjIyMjI=
        String credential = Credentials.basic(userName, password);

        String url = "https://api.github.com/repos/" + repoName;
        // OkHttpClient 对象前面已经创建过了, 不需要重复创建.
        // 请求对象, Call 对象, 响应对象, 还是需要重新创建的
        Request request = new Request.Builder().url(url).header("Authorization", credential).build();
        Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        if (!response.isSuccessful()) {
            System.out.println("访问 Github API 失败! url = " + url);
            return null;
        }
        return response.body().string();
    }

    // 这个方法的功能, 就是把项目的 url 提取出其中的仓库名字和作者名字
    // https://github.com/doov-io/doov => doov-io/doov
    public String getRepoName(String url) {
        int lastOne = url.lastIndexOf("/");
        int lastTwo = url.lastIndexOf("/", lastOne - 1);
        if (lastOne == -1 || lastTwo == -1) {
            System.out.println("当前 URL 不是一个标准的项目 url! url:" + url);
            return null;
        }
        return url.substring(lastTwo + 1);
    }

    // 通过这个方法, 获取到该仓库的相关信息
    // 第一个参数 jsonString 表示 Github API 获取到的结果.
    // 第二个参数 project 表示解析出的 star 数, fork 数, opened_issue 数保存到 project 对象中
    // 使用 Gson 这个库来进行解析
    public void parseRepoInfo(String jsonString, Project project) {
        Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
        HashMap<String, Object> hashMap = gson.fromJson(jsonString, type);
        // hashMap 中的 key 的名字都是源于 Github API 的返回值.
        Double starCount = (Double)hashMap.get("stargazers_count");
        project.setStarCount(starCount.intValue());
        Double forkCount = (Double)hashMap.get("forks_count");
        project.setForkCount(forkCount.intValue());
        Double openedIssueCount = (Double)hashMap.get("open_issues_count");
        project.setOpenedIssueCount(openedIssueCount.intValue());
    }
}
//package crawler;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.reflect.TypeToken;
//import dao.Project;
//import okhttp3.*;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import java.io.IOException;
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//
//public class Crawler {
//    private OkHttpClient okHttpClient = new OkHttpClient();
//    private Gson gson = new GsonBuilder().create();
//    private HashSet<String> urlBlackList = new HashSet<>();
//
//    {
//        urlBlackList.add("https://github.com/events");
//        urlBlackList.add("https://github.community");
//        urlBlackList.add("https://github.com/about");
//        urlBlackList.add("https://github.com/pricing");
//        urlBlackList.add("https://github.com/contact");
//    }
//
//    public static void main(String[] args) throws IOException {
//        Crawler crawler = new Crawler();
//        // 1. 获取入口页面
//        String html = crawler.getPage("https://github.com/akullpp/awesome-java/blob/master/README.md");
//        // System.out.println(html);
//        List<Project> projects = crawler.parseProjectList(html);
//        //System.out.println(projects);
//        for (int i = 0; i < projects.size() && i<5; i++){
//            Project project = projects.get(i);
//            String repoName = crawler.getRepoName(project.getUrl());
//            String jsonString = crawler.getRepoInfo(repoName);
//            //System.out.println(jsonString);
//            crawler.parseRepoInfo(jsonString, project);
//            System.out.println(project);
//            System.out.println("========================================");
//        }
//    }
//
//    // 使用 Okhttp 获取github页面
//    public String getPage(String url) throws IOException{
//        // 1. 先创建一个 OkHttpClient 对象,一个程序包含一个实例即可
//        //  okHttpClient = new OkHttpClient();
//        // 2. 创建一个 Request 对象
//        //    Java 中实例化一个对象有很多方式.
//        //    可以直接 new , 也可以使用某个静态的工厂方法来创建实例
//        //    此处的 Builder 这个类是一个辅助构造 Request 对象的类.
//        //    Builder 中提供的 url 方法能够设定当前请求的url
//        Request request = new Request.Builder().url(url).build();
//        // 3. 创建一个Call对象(负责进行一次网络访问操作)
//        Call call = okHttpClient.newCall(request);
//        // 4. 发送请求给服务器,获取到response对象
//        Response response = call.execute();
//        // 5. 判定响应对象是否成功
//        if(!response.isSuccessful()) {
//            System.out.println("请求失败！");
//            return null;
//        }
//        return response.body().string();
//    }
//
//    // 使用 Jsoup 分析页面
//    public List<Project> parseProjectList(String html) {
//        ArrayList<Project> result = new ArrayList<>();
//        // 1. 创建document 对象
//        Document document = Jsoup.parse(html);
//        Elements elements = document.getElementsByTag("li");
//        for(Element li : elements) {
//            Elements allLink = li.getElementsByTag("a");
//            if(allLink.size() == 0) {
//                continue;// 没有a,忽略li
//            }
//            Element link = allLink.get(0);
////            // 输出a标签的内容
////            System.out.println(link.text());
////            System.out.println(link.attr("href"));
////            System.out.println(li.text());
////            System.out.println("==========================================================");
//            String url = link.attr("href");
//            // 只统计github上的项目
//            if(!url.startsWith("https://github.com")) {
//                continue;
//            }
//            // 手动去掉不要的链接
////            if(url.equals("")){
////                continue;
////            }
//            if(urlBlackList.contains(url)) {
//                continue;
//            }
//            Project project = new Project();
//            project.setName(link.text());   // 输出a标签的内容
//            project.setUrl(link.attr("href"));  // 输出a href 的url,获取href属性
//            project.setDescription(li.text());  // 项目li标签的内容==>描述信息
//            result.add(project);
//        }
//        return result;
//    }
//
//    // 调动Github API获取指定仓库的信息
//    // repoName 形如doov-io/doov
//    public String getRepoInfo(String repoName) throws IOException {
//        // 身份认证之后 5,000 requests an hour
//        String username = "Chakra-Z";
//        String password = "awsedr76754";
//        // 此处对用户名密码进行了base64加密
//        String credential = Credentials.basic(username,password);
//
//        String url = "https://api.github.com/repos/" + repoName;
//        // OkHttpClient创建过了，创建
//        Request request = new Request.Builder().url(url).header("Authorization",credential).build();
//        Call call = okHttpClient.newCall(request);
//        Response response = call.execute();
//        if(!response.isSuccessful()) {
//            System.out.println("访问失败url"+url);
//            return  null;
//        }
//        return response.body().string();
//    }
//
//    // 在url中提取仓库name和作者name
//    //
//    public String getRepoName(String url){
//        int lastOne = url.lastIndexOf("/");
//        int lastTwo = url.lastIndexOf("/",lastOne-1);
//        if(lastOne == -1 || lastTwo == -1) {
//            System.out.println("url不是标准url"+url);
//            return null;
//        }
//        return url.substring(lastTwo+1);
//    }
//
//    // jsonString GithubAPI
//    // project star,fork,issue
//    public void parseRepoInfo(String jsonString, Project project){
//        // TypeToken 获取HashMap对应的一个类
//        Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
//        HashMap<String, Object> hashMap = gson.fromJson(jsonString, type);
//        Double startCount = (Double)hashMap.get("stargazers_count");
//        project.setStarCount(startCount.intValue());
//        Double forkCount = (Double)hashMap.get("forks_count");
//        project.setForkCount(forkCount.intValue());
//        Double openIssueCount = (Double)hashMap.get("open_issue_count");
//        project.setOpenedIssueCount(openIssueCount.intValue());
//    }
//}