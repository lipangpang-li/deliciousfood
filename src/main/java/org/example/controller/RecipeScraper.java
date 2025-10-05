package org.example.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecipeScraper {

    private static final String BASE_URL = "https://www.xiachufang.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    public static void main(String[] args) {
        String searchKeyword = "红烧肉";
        int maxRecipes = 3;

        List<Recipe> recipes = scrapeRecipes(searchKeyword, maxRecipes);

        // 使用Gson将结果转换为JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(recipes);

        System.out.println(jsonOutput);
    }

    public static List<Recipe> scrapeRecipes(String searchKeyword, int maxRecipes) {
        List<Recipe> recipes = new ArrayList<>();
        String searchUrl = BASE_URL + "/search/?keyword=" + searchKeyword;

        try {
            // 获取搜索结果页
            Document searchPage = Jsoup.connect(searchUrl)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .get();

            // 提取菜谱卡片
            Elements recipeCards = searchPage.select(".normal-recipe-list li");
            int count = 0;

            for (Element card : recipeCards) {
                if (count >= maxRecipes) break;

                Element link = card.selectFirst("a[href*=/recipe/]");
                if (link == null) continue;

                String recipeUrl = BASE_URL + link.attr("href");
                Recipe recipe = scrapeRecipeDetail(recipeUrl);

                if (recipe != null) {
                    recipes.add(recipe);
                    count++;

                    // 添加延迟避免请求过快
                    Thread.sleep(1000);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return recipes;
    }

    public static Recipe scrapeRecipeDetail(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .get();

            // 提取基本信息
            String title = doc.selectFirst(".page-title") != null ?
                    doc.selectFirst(".page-title").text().trim() : "未知标题";

            String author = doc.selectFirst(".author a") != null ?
                    doc.selectFirst(".author a").text().trim() : "未知作者";

            String rating = doc.selectFirst(".score") != null ?
                    doc.selectFirst(".score").text().trim() : "无评分";

            // 提取食材
            List<String> ingredients = new ArrayList<>();
            Elements ingredientItems = doc.select(".ings tr");
            for (Element item : ingredientItems) {
                String name = item.selectFirst(".name").text().trim();
                String amount = item.selectFirst(".unit") != null ?
                        item.selectFirst(".unit").text().trim() : "";
                ingredients.add(name + amount);
            }

            // 提取步骤
            List<String> steps = new ArrayList<>();
            Elements stepElements = doc.select(".steps p.text");
            for (int i = 0; i < stepElements.size(); i++) {
                steps.add((i + 1) + ". " + stepElements.get(i).text().trim());
            }

            // 提取烹饪时间
            String cookTime = "";
            Elements infoElements = doc.select(".cooked .info");
            for (Element el : infoElements) {
                if (el.text().contains("耗时")) {
                    cookTime = el.text().replace("耗时", "").trim();
                    break;
                }
            }

            return new Recipe(title, url, author, rating, ingredients, steps, cookTime);

        } catch (IOException e) {
            System.err.println("Error scraping recipe: " + url);
            e.printStackTrace();
            return null;
        }
    }

    static class Recipe {
        private String title;
        private String url;
        private String author;
        private String rating;
        private List<String> ingredients;
        private List<String> steps;
        private String cookTime;
        private final String sourceSite = "下厨房";

        public Recipe(String title, String url, String author, String rating,
                      List<String> ingredients, List<String> steps, String cookTime) {
            this.title = title;
            this.url = url;
            this.author = author;
            this.rating = rating;
            this.ingredients = ingredients;
            this.steps = steps;
            this.cookTime = cookTime;
        }

        // Getters for JSON serialization
        public String getTitle() { return title; }
        public String getUrl() { return url; }
        public String getAuthor() { return author; }
        public String getRating() { return rating; }
        public List<String> getIngredients() { return ingredients; }
        public List<String> getSteps() { return steps; }
        public String getCookTime() { return cookTime; }
        public String getSourceSite() { return sourceSite; }
    }
}