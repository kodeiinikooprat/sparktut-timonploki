package kodeiiniryhma.sparktutorial;

import static spark.Spark.*;

import spark.Request;
import spark.Response;
import spark.Route;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerRoute;

public class HelloSpark { //Peterin kone on hidas

    /* Refactor */
    // Create a new ArticleDbService using the ArticleServletDao as the implmentation
    // Since all of our DAO classes will implement the same interface we can just swap
    // them out as we'll see later on
    public static ArticleDbService<Article> articleDbService = new ArticleMongoDao();
//    public static Deque<Article> articles = new ArrayDeque<Article>();

    
    public static void main(String[] args) {

        get(new FreeMarkerRoute("/") {
            @Override
            public ModelAndView handle(Request request, Response response) {
                Map<String, Object> viewObjects = new HashMap();
                /* Refactor */
                ArrayList<Article> articles = articleDbService.readAll();
                
                
                if (articles.isEmpty()) {
                    viewObjects.put("hasNoArticles", "Welcome, please click \"Write Article\" to begin.");
                } else {
                    Deque<Article> showArticles = new ArrayDeque();

                    for (Article article : articles) {
                        if (article.readable()) {
                            showArticles.addFirst(article);
                        }
                    }

                    viewObjects.put("articles", showArticles);
                }

                viewObjects.put("templateName", "articleList.ftl");

                return modelAndView(viewObjects, "layout.ftl");
            }
        });

       
        get(new FreeMarkerRoute("/article/create") {
            @Override
            public Object handle(Request request, Response response) {
                Map<String, Object> viewObjects = new HashMap();

                viewObjects.put("templateName", "articleForm.ftl");

                return modelAndView(viewObjects, "layout.ftl");
            }
        });

        post(new Route("/article/create") {
            @Override
            public Object handle(Request request, Response response) {
                String title = request.queryParams("article-title");
                String summary = request.queryParams("article-summary");
                String content = request.queryParams("article-content");

                Article article = new Article(title, summary, content, articleDbService.readAll().size());
                articleDbService.create(article);

                response.status(201);
                response.redirect("/");
                return "";
            }
        });

        
        get(new FreeMarkerRoute("/article/update/:id") {
            @Override
            public Object handle(Request request, Response response) {
                Integer id = Integer.parseInt(request.params(":id"));
                Map<String, Object> viewObjects = new HashMap();

                viewObjects.put("templateName", "articleForm.ftl");

                viewObjects.put("article", articleDbService.readOne(id));
                
                return modelAndView(viewObjects, "layout.ftl");
            }
        });

        
        post(new Route("/article/update/:id") {
            @Override
            public Object handle(Request request, Response response) {
                Integer id = Integer.parseInt(request.queryParams("article-id"));
                String title = request.queryParams("article-title");
                String summary = request.queryParams("article-summary");
                String content = request.queryParams("article-content");

                articleDbService.update(id, title, summary, content);
                
                response.status(200);
                response.redirect("/");
                return "";
            }
        });

        
        get(new FreeMarkerRoute("/article/read/:id") {
            @Override
            public Object handle(Request request, Response response) {
                Integer id = Integer.parseInt(request.params(":id"));
                Map<String, Object> viewObjects = new HashMap();

                viewObjects.put("templateName", "articleRead.ftl");

                viewObjects.put("article", articleDbService.readOne(id));

                return modelAndView(viewObjects, "layout.ftl");
            }
        });

        
        get(new Route("/article/delete/:id") {
            @Override
            public Object handle(Request request, Response response) {
                Integer id = Integer.parseInt(request.params(":id"));

                articleDbService.delete(id);
                
                response.status(200);
                response.redirect("/");
                return "";
            }
        });

        get(new FreeMarkerRoute("/freemarker") {
            @Override
            public ModelAndView handle(Request request, Response response) {
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("blogTitle", "My Ploki!");
                attributes.put("descriptionTitle", "We're using Twitter Bootstrap 3");
                attributes.put("descriptionBody1", "Special thanks to Twitter for being so dang awesome and helping us");
                attributes.put("descriptionBody2", "No seriously... the web would be so ugly without Bootstrap");

                // Place the freemarker template within src/test/resources/spark/template/freemarker
                return modelAndView(attributes, "layout.ftl");
            }
        });

    }
}
