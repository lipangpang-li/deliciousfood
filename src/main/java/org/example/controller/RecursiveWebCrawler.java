package org.example.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RecursiveWebCrawler {
    // 存储已访问的URL，使用线程安全的ConcurrentHashMap
    private static final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
    // 最大爬取深度
    private static final int MAX_DEPTH = 3;
    // 线程池大小
    private static final int THREAD_POOL_SIZE = 5;
    // 爬取间隔时间（毫秒）
    private static final int CRAWL_DELAY = 1000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入起始网址: ");
        String startUrl = scanner.nextLine().trim();
        scanner.close();

        if (!startUrl.startsWith("http")) {
            startUrl = "https://" + startUrl;
        }

        System.out.println("\n开始爬取: " + startUrl);
        System.out.println("==================================");

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // 提交初始爬取任务
        String finalStartUrl = startUrl;
        executor.submit(() -> crawl(finalStartUrl, 0));

        // 关闭线程池并等待所有任务完成
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("爬取过程中断: " + e.getMessage());
        }

        System.out.println("\n==================================");
        System.out.println("爬取完成！总共访问了 " + visitedUrls.size() + " 个页面");
    }

    private static void crawl(String url, int depth) {
        // 检查深度限制和是否已访问
        if (depth > MAX_DEPTH || visitedUrls.contains(url)) {
            return;
        }

        // 标记为已访问
        visitedUrls.add(url);

        try {
            // 添加延迟以避免被封IP
            Thread.sleep(CRAWL_DELAY);

            // 连接并获取文档
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();

            // 提取页面标题
            String title = doc.title();
            System.out.println("\n页面标题: " + title);
            System.out.println("URL: " + url);
            System.out.println("深度: " + depth);

            // 提取页面文本内容
            String text = doc.text();
            System.out.println("\n文本内容摘要: ");
            System.out.println(text.substring(0, Math.min(500, text.length())) + (text.length() > 500 ? "..." : ""));
            System.out.println("----------------------------------");

            // 提取页面中的所有链接
            Elements links = doc.select("a[href]");
            System.out.println("发现链接: " + links.size());

            // 递归爬取新链接
            for (Element link : links) {
                String nextUrl = link.absUrl("href");

                // 过滤无效URL
                if (isValidUrl(nextUrl)) {
                    // 使用线程池提交新任务
                    Thread.sleep(100); // 小延迟避免过快提交
                    crawl(nextUrl, depth + 1);
                }
            }
        } catch (Exception e) {
            System.err.println("爬取 " + url + " 时出错: " + e.getMessage());
        }
    }

    // 验证URL是否有效
    private static boolean isValidUrl(String url) {
        return url.startsWith("http") &&
                !url.contains("mailto:") &&
                !url.contains("javascript:") &&
                !url.contains("#") &&
                !visitedUrls.contains(url);
    }
}